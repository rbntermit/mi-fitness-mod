package com.markus.facefix;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public final class FaceInstallFeedback {
    private FaceInstallFeedback() {}

    public static void rejected(Activity activity, Integer code) {
        Log.w("MOD", "local watch face: preparation failed" + (code == null ? "" : " code=" + code));
        String suffix = code == null ? "." : " (код " + code + ").";
        activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(),
                "Не удалось подготовить циферблат" + suffix + " Проверьте совместимость с точной моделью часов.",
                Toast.LENGTH_LONG).show());
    }
}
