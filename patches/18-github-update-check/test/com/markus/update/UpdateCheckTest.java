package com.markus.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateCheckTest {
    private static final ModVersion INSTALLED = ModVersion.local("3.60.0i", 1);

    // --- tags

    @Test
    void tagIsParsed() {
        ModVersion v = ModVersion.parseTag("v3.60.0i-band-12");
        assertNotNull(v);
        assertEquals(3, v.major);
        assertEquals(60, v.minor);
        assertEquals(0, v.patch);
        assertEquals("i", v.suffix);
        assertEquals(12, v.band);
        assertEquals("v3.60.0i-band-12", v.tag());
        assertEquals("3.60.0i, сборка 12", v.display());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "3.60.0i-band-1", "v3.60.0i", "v3.60.0i-band-", "v3.60.0i-band-x", "v3.60i-band-1",
            "v3.60.0i-band-1-beta", "v3.60.0i-mod-1", "V3.60.0i-band-1", "v3.60.0I-band-1", "v3.60.0i-band-1\n",
            "v3.60.0i band-1", "v99999.0.0i-band-1", "v3.60.0i-band-9999999"})
    void otherTagsAreRejected(String tag) {
        if (tag.endsWith("\n")) {
            assertNotNull(ModVersion.parseTag(tag)); // surrounding white space is trimmed
            return;
        }
        assertNull(ModVersion.parseTag(tag));
    }

    @Test
    void nullTagIsRejected() {
        assertNull(ModVersion.parseTag(null));
    }

    @Test
    void localVersion() {
        assertNotNull(ModVersion.local("3.60.0i", 1));
        assertNull(ModVersion.local("3.60.0i", -1));
        assertNull(ModVersion.local("3.60", 1));
        assertNull(ModVersion.local(null, 1));
    }

    // --- comparison

    @Test
    void higherBandOfSameAppIsNewer() {
        assertTrue(ModVersion.isNewer(ModVersion.parseTag("v3.60.0i-band-2"), INSTALLED));
    }

    @Test
    void sameOrLowerBandIsNotNewer() {
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v3.60.0i-band-1"), INSTALLED));
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v3.60.0i-band-0"), INSTALLED));
    }

    @Test
    void higherAppVersionIsNewerWhateverTheBand() {
        assertTrue(ModVersion.isNewer(ModVersion.parseTag("v3.61.0i-band-1"), INSTALLED));
        assertTrue(ModVersion.isNewer(ModVersion.parseTag("v3.60.1i-band-1"), INSTALLED));
        assertTrue(ModVersion.isNewer(ModVersion.parseTag("v4.0.0i-band-1"), INSTALLED));
        assertTrue(ModVersion.isNewer(ModVersion.parseTag("v3.100.0i-band-1"), INSTALLED)); // numbers, not text
    }

    @Test
    void lowerAppVersionIsNotNewerWhateverTheBand() {
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v3.59.2i-band-9"), INSTALLED));
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v2.99.9i-band-9"), INSTALLED));
    }

    @Test
    void otherDistributionIsIgnored() {
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v3.61.0-band-1"), INSTALLED));
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v3.61.0c-band-1"), INSTALLED));
    }

    @Test
    void unknownInstalledBuildNeverAnnounces() {
        assertFalse(ModVersion.isNewer(ModVersion.parseTag("v9.0.0i-band-1"), null));
        assertFalse(ModVersion.isNewer(null, INSTALLED));
    }

    // --- schedule and switch

    @Test
    void checkAtMostOnceInSixHours() {
        long now = 1_700_000_000_000L;
        assertTrue(UpdateCheck.due(true, 0, now));
        assertFalse(UpdateCheck.due(true, now - 60_000, now));
        assertFalse(UpdateCheck.due(true, now - 6L * 60 * 60 * 1000 + 1, now));
        assertTrue(UpdateCheck.due(true, now - 6L * 60 * 60 * 1000, now));
        assertTrue(UpdateCheck.due(true, now - 12L * 60 * 60 * 1000, now)); // previous daily schedule must not delay the upgrade
        assertTrue(UpdateCheck.due(true, now + 60_000, now)); // clock set back
    }

    @Test
    void switchedOffNeverChecks() {
        long now = 1_700_000_000_000L;
        assertFalse(UpdateCheck.due(false, 0, now));
        assertFalse(UpdateCheck.due(false, now - 24L * 60 * 60 * 1000, now));
    }

    @Test
    void onlyTheRepositoryApiIsAsked() {
        assertEquals("https://api.github.com/repos/rbntermit/mi-fitness-mod/releases/latest", UpdateCheck.API_URL);
    }

    // --- answers

    private static String release(String tag, String url) {
        return "{\"url\":\"https://api.github.com/x\",\"tag_name\":\"" + tag + "\",\"name\":\"Сборка\","
                + "\"draft\":false,\"prerelease\":false,\"author\":{\"login\":\"rbntermit\",\"id\":1,"
                + "\"x\":[1,2,{\"a\":\"}\"}]},\"assets\":[],\"html_url\":\"" + url + "\","
                + "\"body\":\"Что нового:\\n- пункт \\\"один\\\"\\u0021\",\"id\":123}";
    }

    @Test
    void newerReleaseIsAnnounced() {
        String url = "https://github.com/rbntermit/mi-fitness-mod/releases/tag/v3.60.0i-band-2";
        UpdateCheck.Result r = UpdateCheck.classify(200, release("v3.60.0i-band-2", url), INSTALLED);
        assertEquals(UpdateCheck.State.NEWER, r.state);
        assertEquals(2, r.version.band);
        assertEquals(url, r.pageUrl);
        assertEquals("Что нового:\n- пункт \"один\"!", r.body);
        assertEquals("Сборка", r.title);
    }

    @Test
    void sameReleaseIsUpToDate() {
        String url = "https://github.com/rbntermit/mi-fitness-mod/releases/tag/v3.60.0i-band-1";
        assertEquals(UpdateCheck.State.UP_TO_DATE,
                UpdateCheck.classify(200, release("v3.60.0i-band-1", url), INSTALLED).state);
    }

    @Test
    void emptyRepositoryMeansNoUpdates() {
        // GitHub answers 404 for /releases/latest while a repository has no release
        assertEquals(UpdateCheck.State.NO_RELEASE, UpdateCheck.classify(404, "{\"message\":\"Not Found\"}", INSTALLED).state);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 301, 403, 429, 500, 502, 503})
    void networkErrorsAndRateLimitsAreQuietFailures(int code) {
        assertEquals(UpdateCheck.State.FAILED, UpdateCheck.classify(code, null, INSTALLED).state);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<html>", "[]", "{", "{\"tag_name\":", "{\"tag_name\":\"v3.61.0i-band-1\"", "null",
            "{\"body\":\"\\u12\"}"})
    void brokenAnswersAreQuietFailures(String json) {
        assertEquals(UpdateCheck.State.FAILED, UpdateCheck.classify(200, json, INSTALLED).state);
    }

    @Test
    void foreignTagMeansNoUpdates() {
        assertEquals(UpdateCheck.State.NO_RELEASE,
                UpdateCheck.classify(200, release("release-7", UpdateCheck.RELEASES_PAGE), INSTALLED).state);
    }

    @Test
    void preReleaseIsNotAnnounced() {
        String j = release("v3.61.0i-band-1", "x").replace("\"prerelease\":false", "\"prerelease\":true");
        assertEquals(UpdateCheck.State.NO_RELEASE, UpdateCheck.classify(200, j, INSTALLED).state);
    }

    // --- the page that "Скачать" opens

    @ParameterizedTest
    @ValueSource(strings = {"https://evil.example/releases/", "https://github.com/someone/else/releases/tag/x",
            "javascript:alert(1)", "https://github.com/rbntermit/mi-fitness-mod/releases/tag/a b",
            "intent://github.com/rbntermit/mi-fitness-mod/releases/"})
    void foreignPageFallsBackToTheTagPage(String url) {
        UpdateCheck.Result r = UpdateCheck.classify(200, release("v3.61.0i-band-1", url), INSTALLED);
        assertEquals("https://github.com/rbntermit/mi-fitness-mod/releases/tag/v3.61.0i-band-1", r.pageUrl);
    }

    @Test
    void longReleaseTextIsCut() {
        String body = "x".repeat(UpdateCheck.BODY_MAX + 500);
        String s = UpdateCheck.shortBody(body);
        assertTrue(s.length() <= UpdateCheck.BODY_MAX + 1);
        assertTrue(s.endsWith("…"));
        assertEquals("", UpdateCheck.shortBody(null));
        assertEquals("a\nb", UpdateCheck.shortBody(" a\r\nb "));
    }
}
