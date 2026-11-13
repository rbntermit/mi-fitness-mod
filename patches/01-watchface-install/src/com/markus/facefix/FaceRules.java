package com.markus.facefix;

/** Which watch face market answer FaceFix looks at, and when. Plain Java, tested without a phone. */
public final class FaceRules {
    public enum Kind { NONE, LIST_V4, DETAIL }

    private FaceRules() {
    }

    /**
     * "Установлено" asks /ops/v1/paidwatchface/v4/list for the faces on the band (the old handler of 00-core only matches
     * "paidwatchface/list"); the detail screen asks /ops/v1/mall/(v4/)watchface/detail. Paid detail and other calls stay
     * with the stock answer.
     */
    public static Kind kind(String url) {
        if (url == null) {
            return Kind.NONE;
        }
        String path = url;
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        if (path.endsWith("/paidwatchface/v4/list")) {
            return Kind.LIST_V4;
        }
        if (path.endsWith("/watchface/detail") && !path.contains("paidwatchface")) {
            return Kind.DETAIL;
        }
        return Kind.NONE;
    }

    /** Ids of faces made by the mod's file install look like "120917346948"; anything else is not ours to answer. */
    public static boolean plausibleId(String id) {
        return id != null && id.matches("[0-9]{3,20}");
    }
}
