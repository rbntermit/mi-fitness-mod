package com.markus.facefix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FaceRulesTest {
    static final String HOST = "https://watch.example/ops/v1";

    @Test
    void installedListV4IsOurs() {
        assertEquals(FaceRules.Kind.LIST_V4, FaceRules.kind(HOST + "/paidwatchface/v4/list?_nonce=a&data=b"));
    }

    @Test
    void detailBothVersionsAreOurs() {
        assertEquals(FaceRules.Kind.DETAIL, FaceRules.kind(HOST + "/mall/v4/watchface/detail?_nonce=a&data=b"));
        assertEquals(FaceRules.Kind.DETAIL, FaceRules.kind(HOST + "/mall/watchface/detail?data=b"));
    }

    @Test
    void everythingElseStays() {
        String[] other = {
            HOST + "/paidwatchface/detail?data=b",
            HOST + "/paidwatchface/list?data=b",
            HOST + "/mall/v4/watchface/extend/info?data=b",
            HOST + "/mall/v4/watchface/tab?data=b",
            HOST + "/paidwatchface/v4/list/extra",
            HOST + "/x?next=/paidwatchface/v4/list",
            null,
        };
        for (String u : other) {
            assertEquals(FaceRules.Kind.NONE, FaceRules.kind(u), String.valueOf(u));
        }
    }

    @Test
    void faceIds() {
        assertTrue(FaceRules.plausibleId("120917346948"));
        assertTrue(FaceRules.plausibleId("167210123"));
        assertFalse(FaceRules.plausibleId(""));
        assertFalse(FaceRules.plausibleId(null));
        assertFalse(FaceRules.plausibleId("../x"));
        assertFalse(FaceRules.plausibleId("12a"));
    }
}
