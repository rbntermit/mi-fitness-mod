package com.markus.facefix;

import android.util.Log;
import com.markus.OkCallback;
import com.markus.Rc4Codec;
import com.markus.a;
import com.xiaomi.fitness.watch.face.data.FaceManager;
import com.xiaomi.fitness.watch.face.export.data.WatchFace;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Watch faces installed from a file (patch 01) are unknown to Xiaomi's market. Since the 3.59 market bundle:
 * <ul>
 *   <li>«Установлено» asks /ops/v1/paidwatchface/v4/list, which the 00-core handler (only "paidwatchface/list") never
 *       saw, so these faces had no name and no preview;</li>
 *   <li>the detail screen gets an empty answer for such an id; OkCallback.handleWatchface could only edit an answer,
 *       failed on the empty body ("End of input at character 0"), and the screen stayed empty with
 *       «Произошла ошибка. Повторите попытку позже».</li>
 * </ul>
 * Called at the end of OkCallback.onResponse (hooks.txt). Only faces that have the mod's local files
 * (files/WatchFace/&lt;did&gt;/&lt;id&gt;/local) are answered here; every other answer is passed on untouched.
 */
public final class FaceFix {
    private static final String TAG = "MOD";

    private FaceFix() {
    }

    public static Response after(OkCallback cb, Response r) {
        if (cb == null || r == null) {
            return r;
        }
        try {
            HttpUrl url = r.request().url();
            switch (FaceRules.kind(url.toString())) {
                case LIST_V4:
                    return list(cb, r, url);
                case DETAIL:
                    return detail(cb, r, url);
                default:
                    return r;
            }
        } catch (Exception e) {
            Log.w(TAG, "face: answer left as it is", e);
            return r;
        }
    }

    /** The request parameters, decrypted the way the market sent them; null when they are not readable. */
    private static JSONObject request(HttpUrl url, String security) throws JSONException {
        String nonce = url.queryParameter("_nonce");
        String data = url.queryParameter("data");
        if (security == null || nonce == null || data == null) {
            return null;
        }
        return new JSONObject(Rc4Codec.decode(security, nonce, data));
    }

    /** Body of the answer; empty when there is none. */
    private static String body(Response r) {
        try {
            ResponseBody b = r.peekBody(Long.MAX_VALUE);
            return b == null ? "" : b.string();
        } catch (Exception e) {
            return "";
        }
    }

    /** The answer as JSON, decrypted when it came encrypted; null when it is empty or not JSON. */
    private static JSONObject answer(String body, String security, String nonce) {
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            String text = a.isBase64(body.trim()) ? Rc4Codec.decode(security, nonce, body.trim()) : body;
            return new JSONObject(text);
        } catch (Exception e) {
            return null;
        }
    }

    private static Response replace(Response r, String body, boolean encrypted, String security, String nonce,
            JSONObject json) {
        String text = json.toString();
        String out = encrypted ? Rc4Codec.encode(security, nonce, text) : text;
        MediaType type = null;
        try {
            ResponseBody b = r.peekBody(1);
            type = b == null ? null : b.contentType();
        } catch (Exception e) {
            // keep the type empty; the market reads the text anyway
        }
        return r.newBuilder().code(200).message("OK").body(ResponseBody.create(type, out)).build();
    }

    private static JSONObject emptyAnswer() throws JSONException {
        return new JSONObject().put("code", 0).put("message", "").put("result", new JSONObject());
    }

    // ---------------------------------------------------------------- «Установлено»

    private static Response list(OkCallback cb, Response r, HttpUrl url) throws JSONException {
        String security = a.getSecurity();
        JSONObject req = request(url, security);
        if (req == null) {
            return r;
        }
        JSONArray ids = req.optJSONArray("id_list");
        if (ids == null) {
            ids = req.optJSONArray("idList");
        }
        if (ids == null || ids.length() == 0) {
            return r;
        }
        String nonce = url.queryParameter("_nonce");
        String body = body(r);
        JSONObject json = answer(body, security, nonce);
        boolean encrypted = json == null || a.isBase64(body.trim());
        if (json == null) {
            json = emptyAnswer();
        }
        JSONObject result = json.optJSONObject("result");
        if (result == null) {
            result = new JSONObject();
            json.put("result", result);
        }
        JSONArray list = result.optJSONArray("watchface_list");
        if (list == null) {
            list = new JSONArray();
            result.put("watchface_list", list);
        }
        java.util.Set<String> answered = new java.util.HashSet<>();
        boolean changed = false;
        for (int i = 0; i < list.length(); i++) {
            JSONObject e = list.optJSONObject(i);
            String id = e == null ? null : e.optString("id", null);
            if (id != null) {
                answered.add(id);
                if (FaceRules.plausibleId(id) && cb.patchEntryLocal(id, e)) {
                    changed = true;
                }
            }
        }
        for (int i = 0; i < ids.length(); i++) {
            String id = ids.optString(i, null);
            if (!FaceRules.plausibleId(id) || answered.contains(id)) {
                continue;
            }
            JSONObject e = cb.emptyWatchface();
            if (cb.patchEntryLocal(id, e)) {
                list.put(e);
                changed = true;
            }
        }
        if (!changed) {
            return r;
        }
        Log.i(TAG, "face: installed list filled from the mod's files");
        return replace(r, body, encrypted, security, nonce, json);
    }

    // ---------------------------------------------------------------- detail screen

    private static Response detail(OkCallback cb, Response r, HttpUrl url) throws JSONException {
        String security = a.getSecurity();
        JSONObject req = request(url, security);
        String id = req == null ? null : req.optString("id", null);
        if (!FaceRules.plausibleId(id)) {
            return r;
        }
        String nonce = url.queryParameter("_nonce");
        String body = body(r);
        JSONObject json = answer(body, security, nonce);
        JSONObject result = json == null ? null : json.optJSONObject("result");
        JSONObject face = result == null ? null
                : result.optJSONObject("watchface") != null ? result.optJSONObject("watchface")
                : result.optJSONObject("watch_face");
        if (face != null && id.equals(face.optString("id"))) {
            // the market knows the face; OkCallback.handleWatchface has already put the local files in
            return r;
        }
        JSONObject e = cb.emptyWatchface();
        String how;
        if (cb.patchEntryLocal(id, e)) {
            how = "the mod's files";
        } else {
            // a face on the band without the mod's files (installed before band-3 or before the app was reinstalled):
            // open the screen with what the band knows, no preview; ble_ext.type stays 0, so «Изменить» is hidden
            WatchFace onBand = bandFace(id);
            if (onBand == null) {
                return r;
            }
            e.put("id", id);
            String name = onBand.getName();
            e.put("display_name", name == null ? "" : name);
            e.put("status", 101);
            how = "the band's list, no mod files";
        }
        if (json == null) {
            json = emptyAnswer();
        }
        if (result == null) {
            result = new JSONObject();
            json.put("result", result);
        }
        json.put("code", 0);
        result.put("watchface", e);
        Log.i(TAG, "face: detail answered from " + how);
        boolean encrypted = body.trim().isEmpty() || a.isBase64(body.trim());
        return replace(r, body, encrypted, security, nonce, json);
    }

    /** The face as the app knows it from the bound band; null when it is not on the band. */
    private static WatchFace bandFace(String id) {
        try {
            FaceManager m = FaceManager.INSTANCE;
            return m == null ? null : m.getFace(id);
        } catch (RuntimeException e) {
            Log.w(TAG, "face: band list not readable", e);
            return null;
        }
    }
}
