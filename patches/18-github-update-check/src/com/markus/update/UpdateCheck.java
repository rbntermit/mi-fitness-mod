package com.markus.update;

/** What a check of the mod's GitHub releases means for the installed build. Pure logic, no Android. */
final class UpdateCheck {
    /** Metadata endpoint only; APK download uses the explicit GitHub release asset and validated GitHub CDN redirects. */
    static final String API_URL = "https://api.github.com/repos/rbntermit/mi-fitness-mod/releases/latest";
    static final String RELEASES_PAGE = "https://github.com/rbntermit/mi-fitness-mod/releases/";
    static final long AUTO_INTERVAL_MS = 6L * 60 * 60 * 1000;
    /** The release text shown in the dialog is cut to this many characters. */
    static final int BODY_MAX = 1500;

    enum State { NEWER, UP_TO_DATE, NO_RELEASE, FAILED }

    static final class Result {
        final State state;
        final ModVersion version;
        final String title;
        final String body;
        final String pageUrl;
        final UpdateAsset asset;

        Result(State state, ModVersion version, String title, String body, String pageUrl) {
            this(state,version,title,body,pageUrl,null);
        }

        Result(State state,ModVersion version,String title,String body,String pageUrl,UpdateAsset asset) {
            this.asset=asset;
            this.state = state;
            this.version = version;
            this.title = title;
            this.body = body;
            this.pageUrl = pageUrl;
        }

        static Result of(State s) {
            return new Result(s, null, null, null, null);
        }
    }

    private UpdateCheck() {
    }

    /** Automatic check: switched on and at most once in six hours on resume (a clock set back counts as due). */
    static boolean due(boolean enabled, long lastCheck, long now) {
        return enabled && (lastCheck <= 0 || now - lastCheck >= AUTO_INTERVAL_MS || now < lastCheck);
    }

    /**
     * httpCode < 0 = no answer (network). 404 = the repository has no release yet. Rate limits (403/429) and
     * server errors are FAILED; callers stay quiet about them.
     */
    static Result classify(int httpCode, String json, ModVersion installed) {
        if (httpCode == 404) {
            return Result.of(State.NO_RELEASE);
        }
        if (httpCode != 200) {
            return Result.of(State.FAILED);
        }
        ReleaseJson r = ReleaseJson.parse(json);
        if (r == null) {
            return Result.of(State.FAILED);
        }
        ModVersion v = ModVersion.parseTag(r.tag);
        if (v == null || r.draft || r.prerelease) {
            return Result.of(State.NO_RELEASE);
        }
        if (!ModVersion.isNewer(v, installed)) {
            return new Result(State.UP_TO_DATE, v, null, null, null);
        }
        return new Result(State.NEWER, v, r.name, shortBody(r.body), pageUrl(r.htmlUrl, v), UpdateAsset.select(r));
    }

    /** Only a page of this repository's releases is ever opened; anything else falls back to the tag's page. */
    static String pageUrl(String htmlUrl, ModVersion v) {
        if (htmlUrl != null && htmlUrl.startsWith(RELEASES_PAGE) && htmlUrl.length() < 300
                && htmlUrl.matches("[A-Za-z0-9:/._~%-]+")) {
            return htmlUrl;
        }
        return RELEASES_PAGE + "tag/" + v.tag();
    }

    static String shortBody(String body) {
        if (body == null) {
            return "";
        }
        String b = body.replace("\r\n", "\n").trim();
        return b.length() <= BODY_MAX ? b : b.substring(0, BODY_MAX).trim() + "…";
    }
}
