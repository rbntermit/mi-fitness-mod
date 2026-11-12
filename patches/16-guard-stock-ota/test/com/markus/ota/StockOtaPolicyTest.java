package com.markus.ota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StockOtaPolicyTest {
    private static boolean allowed(String model, String band, String offer) {
        return StockOtaPolicy.decide(model, band, offer).allowed;
    }

    // --- the cases named in the task

    @Test
    void globalBandWithNewerGlobalIsAllowed() {
        assertTrue(allowed("miwear.watch.q66gl", "4.200.078", "4.200.090"));
    }

    @Test
    void tcOnGlobalBranchIsBlockedAsReflashed() {
        StockOtaPolicy.Decision d = StockOtaPolicy.decide("miwear.watch.q66tc", "4.200.078", "4.200.090");
        assertFalse(d.allowed);
        assertTrue(d.reason.contains("reflashed"));
    }

    @Test
    void tcOnGlobalBranchOfferedChineseIsBlocked() {
        // the offer the owner's band got from the server on 03.11
        assertFalse(allowed("miwear.watch.q66tc", "4.200.078", "4.100.139"));
    }

    @Test
    void chineseBandWithNewerChineseIsAllowed() {
        assertTrue(allowed("miwear.watch.q66cn", "4.100.139", "4.100.179"));
    }

    @Test
    void notNewerIsBlocked() {
        assertFalse(allowed("miwear.watch.q66cn", "4.100.179", "4.100.179"));
        assertFalse(allowed("miwear.watch.q66cn", "4.100.179", "4.100.139"));
        assertFalse(allowed("miwear.watch.q66gl", "4.200.090", "4.200.078"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "4.200", "latest", "4.200.x", "v4.200.090", "4.200.090-gl", "4..200.1", "4.200.090.1.2.3"})
    void unreadableOfferIsBlocked(String offer) {
        assertFalse(allowed("miwear.watch.q66gl", "4.200.078", offer));
    }

    @Test
    void unreadableBandVersionIsBlocked() {
        assertFalse(allowed("miwear.watch.q66gl", null, "4.200.090"));
        assertFalse(allowed("miwear.watch.q66gl", "user-4.200.078-gl", "4.200.090"));
        assertFalse(allowed("miwear.watch.q66gl", "4.200.078", null));
    }

    @Test
    void otherDeviceWithNormalUpdateIsAllowed() {
        assertTrue(allowed("xiaomi.watch.o66", "2.1.5.12", "2.1.6.3"));
        assertTrue(allowed("hmpace.bracelet.v8", "1.0.4", "1.0.5"));
    }

    @Test
    void otherDeviceNotNewerIsBlocked() {
        assertFalse(allowed("xiaomi.watch.o66", "2.1.6.3", "2.1.6.3"));
    }

    // --- more of the rule

    @Test
    void unknownModelIsBlocked() {
        assertFalse(allowed(null, "4.200.078", "4.200.090"));
        assertFalse(allowed("", "4.200.078", "4.200.090"));
    }

    @Test
    void chineseBandOfferedGlobalIsBlocked() {
        assertFalse(allowed("miwear.watch.q66cn", "4.100.139", "4.200.090"));
    }

    @Test
    void chineseModelReflashedToGlobalIsBlocked() {
        assertFalse(allowed("miwear.watch.q66cn", "4.200.078", "4.200.090"));
    }

    @Test
    void globalModelOnChineseBranchIsBlocked() {
        assertFalse(allowed("miwear.watch.q66gl", "4.100.139", "4.100.179"));
    }

    @ParameterizedTest
    @CsvSource({"miwear.watch.q66gln", "miwear.watch.q66glt", "miwear.watch.q66glu"})
    void globalNfcModelsFollowTheGlobalBranch(String model) {
        assertTrue(allowed(model, "4.200.078", "4.200.090"));
        assertFalse(allowed(model, "4.200.078", "4.100.179"));
    }

    @ParameterizedTest
    @CsvSource({"miwear.watch.q66nfc", "miwear.watch.q66u", "miwear.watch.q66"})
    void band11ModelsOfUnknownRegionAreBlocked(String model) {
        assertFalse(allowed(model, "4.200.078", "4.200.090"));
    }

    @Test
    void band11OnUnknownBranchIsBlocked() {
        assertFalse(allowed("miwear.watch.q66gl", "4.300.001", "4.300.002"));
    }

    @Test
    void modelCaseAndSpacesDoNotMatter() {
        assertTrue(allowed(" Miwear.Watch.Q66GL ", "4.200.078", "4.200.090"));
    }

    @Test
    void numericComparison() {
        assertTrue(StockOtaPolicy.compare("4.200.100", "4.200.090") > 0);
        assertTrue(StockOtaPolicy.compare("4.200.9", "4.200.10") < 0);
        assertEquals(0, StockOtaPolicy.compare("4.200.078", "4.200.78"));
        assertTrue(StockOtaPolicy.compare("4.200.078.1", "4.200.078") > 0);
    }

    @Test
    void branches() {
        assertEquals("4.200", StockOtaPolicy.branch("4.200.078"));
        assertEquals("4.100", StockOtaPolicy.branch("4.100.139"));
        assertNull(StockOtaPolicy.branch("4.300.001"));
        assertNull(StockOtaPolicy.branch("4.200"));
        assertNull(StockOtaPolicy.branch(null));
    }

    @Test
    void reasonsCarryNoDeviceIdentifiers() {
        String r = StockOtaPolicy.decide("miwear.watch.q66tc", "4.200.078", "4.100.139").toString();
        assertTrue(r.startsWith("blocked: "));
        assertFalse(r.matches(".*\\d{8,}.*")); // no did, no serial
        assertFalse(r.contains(":") && r.matches(".*([0-9A-F]{2}:){5}.*"));
    }
}
