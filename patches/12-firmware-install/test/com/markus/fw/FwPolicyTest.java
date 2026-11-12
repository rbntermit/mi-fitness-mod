package com.markus.fw;

import static com.markus.fw.FwPolicy.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The install rules. Each case states exactly which checks must fail. */
class FwPolicyTest {
    static final String GL = "miwear.watch.q66gl";
    static final String CN = "miwear.watch.q66cn";
    static final String TC = "miwear.watch.q66tc";

    @TempDir
    Path tmp;

    FwPolicy.Verdict verdict(File f, DeviceSnapshot d, boolean gate) {
        return FwPolicy.evaluate(FwName.parse(f.getName()), FwPackage.inspect(f), d, true, gate);
    }

    static Set<String> failed(FwPolicy.Verdict v) {
        Set<String> s = new TreeSet<>();
        for (FwPolicy.Check c : v.checks) {
            if (!c.ok) {
                s.add(c.title);
            }
        }
        return s;
    }

    static void assertFails(FwPolicy.Verdict v, String... expected) {
        assertEquals(new TreeSet<>(Arrays.asList(expected)), failed(v), v.checks.toString());
        assertEquals(expected.length == 0, v.allowed());
    }

    File glFull(String version) throws Exception {
        return Fixtures.full(tmp, GL, version, "gl", CN);
    }

    @Test
    void globalBandGlobalFileNewerIsAllowed() throws Exception {
        FwPolicy.Verdict v = verdict(glFull("4.200.090"), Fixtures.band(GL, GL, "4.200.078", 80), true);
        assertFails(v);
        assertTrue(v.warnings.isEmpty());
    }

    @Test
    void gateOffBlocksEvenWhenEverythingFits() throws Exception {
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(GL, GL, "4.200.078", 80), false), C_GATE);
        assertTrue(FwGate.INSTALL_ENABLED, "this build has the send code enabled; every other check still applies");
    }

    // ---------------------------------------------------------------- q66tc that reports M2617B1

    static final String HW_NFC = "M2617B1";
    static final String HW_PLAIN = "M2616B1";

    String[][] rowsWith(String device, String miwear) {
        String[][] rows = Fixtures.REAL_ROWS.clone();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i][2].equals(TC)) {
                rows[i] = new String[] {rows[i][0], device, miwear, "true"};
            }
        }
        return rows;
    }

    File glFullWithTable(String version, String table) throws Exception {
        return Fixtures.full(tmp, GL, version, "gl", CN, table);
    }

    @Test
    void tcConfirmedByHardwareCodeGetsNewerGlobalFileWithWarning() throws Exception {
        FwPolicy.Verdict v = verdict(glFull("4.200.090"), Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertFails(v);
        assertEquals(Arrays.asList(FwPolicy.W_TC_GLOBAL), v.warnings);
    }

    @Test
    void currentDiagnosticsWithTheSample4200078AreBlocked() throws Exception {
        File f = Fixtures.sample("48dc");
        assumeTrue(f != null, "sample 48dc missing");
        // with enough charge only the version blocks
        assertFails(verdict(f, Fixtures.band(TC, HW_NFC, "4.200.078", 80), true), C_NEWER);
        // the values the owner sent on 03.11: 47 % also blocks
        DeviceSnapshot owner = Fixtures.band(TC, HW_NFC, "4.200.078", 47);
        owner.versionChannel = 0;
        owner.productDevice = "";
        FwPolicy.Verdict v = verdict(f, owner, true);
        assertFails(v, C_NEWER, C_BATTERY);
        assertTrue(v.checks.stream().anyMatch(c -> c.title.equals(C_NEWER) && c.detail.contains("та же версия")));
    }

    @Test
    void fileWithoutTableBlocks() throws Exception {
        FwPolicy.Verdict v = verdict(glFullWithTable("4.200.090", ""), Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertTrue(v.failed(C_SOURCES));
        assertTrue(detail(v, C_SOURCES).contains("нет таблицы"), detail(v, C_SOURCES));
        assertTrue(v.failed(C_MODEL));
        assertFalse(v.allowed());
    }

    @Test
    void tableWithoutM2617B1Blocks() throws Exception {
        String[][] rows = Fixtures.REAL_ROWS.clone();
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new String[] {rows[i][0], HW_PLAIN, rows[i][2], rows[i][3]};
        }
        FwPolicy.Verdict v = verdict(glFullWithTable("4.200.090", Fixtures.table(rows)),
                Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertTrue(detail(v, C_SOURCES).contains("нет M2617B1"), detail(v, C_SOURCES));
        assertFalse(v.allowed());
    }

    @Test
    void tableWithoutQ66tcBlocks() throws Exception {
        FwPolicy.Verdict v = verdict(glFullWithTable("4.200.090", Fixtures.table(rowsWith(HW_NFC, "miwear.watch.q66zz"))),
                Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertTrue(detail(v, C_SOURCES).contains("нет q66tc"), detail(v, C_SOURCES));
        assertFalse(v.allowed());
    }

    @Test
    void tableLinkingQ66tcElsewhereBlocks() throws Exception {
        FwPolicy.Verdict v = verdict(glFullWithTable("4.200.090", Fixtures.table(rowsWith(HW_PLAIN, TC))),
                Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertTrue(detail(v, C_SOURCES).contains("связан с [M2616B1]"), detail(v, C_SOURCES));
        assertFalse(v.allowed());
    }

    @Test
    void unreadableTableBlocks() throws Exception {
        String broken = "miwear_product.json\0{\n \"product_info\": [ {\"board_id\": 5, \"device_model\": \"M2617B1\" "
                + "garbage ]\n}";
        FwPolicy.Verdict v = verdict(glFullWithTable("4.200.090", broken), Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertTrue(detail(v, C_SOURCES).contains("неразборчива") || detail(v, C_SOURCES).contains("без device_model"),
                detail(v, C_SOURCES));
        assertFalse(v.allowed());
    }

    @Test
    void unterminatedTableBlocks() throws Exception {
        String cut = "miwear_product.json\0{\n \"product_info\": [ {\"board_id\": 5, \"device_model\": \"M2617B1\", "
                + "\"miwear_model\": \"miwear.watch.q66tc\"}";
        FwPolicy.Verdict v = verdict(glFullWithTable("4.200.090", cut), Fixtures.band(TC, HW_NFC, "4.200.078", 80), true);
        assertTrue(v.failed(C_SOURCES));
        assertFalse(v.allowed());
    }

    @Test
    void q66nfcBindingWithM2617B1IsNotAccepted() throws Exception {
        String nfc = "miwear.watch.q66nfc";
        FwPolicy.Verdict v = verdict(glFull("4.200.090"), Fixtures.band(nfc, HW_NFC, "4.200.078", 80), true);
        assertTrue(v.failed(C_SOURCES));
        assertTrue(detail(v, C_SOURCES).contains("эта пара не подтверждает модель"), detail(v, C_SOURCES));
        assertFalse(v.allowed());
    }

    @Test
    void q66tcBindingWithM2616B1IsNotAccepted() throws Exception {
        FwPolicy.Verdict v = verdict(glFull("4.200.090"), Fixtures.band(TC, HW_PLAIN, "4.200.078", 80), true);
        assertTrue(v.failed(C_SOURCES));
        assertTrue(v.failed(C_MODEL));
        assertFalse(v.allowed());
    }

    @Test
    void tcWithHardwareCodeOn4100CannotJumpToGlobal() throws Exception {
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(TC, HW_NFC, "4.100.179", 80), true), C_BRANCH, C_MODEL);
    }

    @Test
    void tcWithHardwareCodeStillRejectsCnAndTcFilesOnGlobalBranch() throws Exception {
        File cn = Fixtures.full(tmp, CN, "4.200.090", "cn", CN);
        assertTrue(verdict(cn, Fixtures.band(TC, HW_NFC, "4.200.078", 80), true).failed(C_MODEL));
        File tc = Fixtures.full(tmp, TC, "4.200.090", "tc", CN);
        assertTrue(verdict(tc, Fixtures.band(TC, HW_NFC, "4.200.078", 80), true).failed(C_MODEL));
    }

    @Test
    void tcWithHardwareCodeStillNeedsNewerVersionAndCharge() throws Exception {
        assertFails(verdict(glFull("4.200.070"), Fixtures.band(TC, HW_NFC, "4.200.078", 80), true), C_NEWER);
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(TC, HW_NFC, "4.200.078", 49), true), C_BATTERY);
    }

    static String detail(FwPolicy.Verdict v, String title) {
        for (FwPolicy.Check c : v.checks) {
            if (c.title.equals(title)) {
                return String.valueOf(c.detail);
            }
        }
        return "";
    }

    @Test
    void chineseBandOnGlobal4200GetsGlobalFileWithWarning() throws Exception {
        FwPolicy.Verdict v = verdict(glFull("4.200.090"), Fixtures.band(CN, CN, "4.200.078", 80), true);
        assertFails(v);
        assertEquals(Arrays.asList("Китайский браслет на глобальной прошивке"), v.warnings);
    }

    @Test
    void chineseBandOnGlobal4200WithTheSameSampleVersionIsBlocked() throws Exception {
        File f = Fixtures.sample("48dc");
        assumeTrue(f != null, "sample 48dc missing");
        assertFails(verdict(f, Fixtures.band(CN, CN, "4.200.078", 80), true), C_NEWER);
    }

    @Test
    void chineseBandOn4100CannotJumpToGlobal() throws Exception {
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(CN, CN, "4.100.179", 80), true), C_BRANCH, C_MODEL);
    }

    @Test
    void nfcBandRejectsGlobalFile() throws Exception {
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(TC, TC, "4.200.078", 80), true), C_MODEL);
    }

    @Test
    void chineseFileOnGlobalBranchIsBlocked() throws Exception {
        File f = Fixtures.full(tmp, CN, "4.100.200", "cn", CN);
        // 4.100.200 is also older than 4.200.078
        assertFails(verdict(f, Fixtures.band(CN, CN, "4.200.078", 80), true), C_BRANCH, C_MODEL, C_NEWER);
    }

    @Test
    void chineseFileOnChineseBranchIsAllowed() throws Exception {
        File f = Fixtures.full(tmp, CN, "4.100.200", "cn", CN);
        assertFails(verdict(f, Fixtures.band(CN, CN, "4.100.179", 80), true));
    }

    @Test
    void otherRegionFileForGlobalBand() throws Exception {
        File f = Fixtures.full(tmp, CN, "4.100.200", "cn", CN);
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.100.179", 80), true), C_MODEL);
    }

    @Test
    void contradictingModelsBlock() throws Exception {
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(CN, GL, "4.200.078", 80), true), C_SOURCES);
    }

    @Test
    void contradictingVersionsBlock() throws Exception {
        DeviceSnapshot d = Fixtures.band(GL, GL, "4.200.078", 80);
        d.cachedVersion = "4.200.070";
        assertFails(verdict(glFull("4.200.090"), d, true), C_SOURCES);
    }

    @Test
    void otherFamilyBandIsBlocked() throws Exception {
        String q77 = "miwear.watch.q77gl";
        assertFails(verdict(glFull("4.200.090"), Fixtures.band(q77, q77, "4.200.078", 80), true), C_FAMILY);
    }

    @Test
    void otherModelFileIsBlocked() throws Exception {
        File f = Fixtures.full(tmp, "miwear.watch.q77gl", "4.200.090", "gl", "miwear.watch.q77cn");
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true), C_FAMILY);
    }

    @Test
    void imageFamilyMustMatchName() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q77cn");
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true), C_FAMILY);
    }

    @Test
    void regionInNameMustMatchBuildId() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "cn", CN);
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true), C_REGION);
    }

    @Test
    void brokenZipIsBlocked() throws Exception {
        File f = tmp.resolve("00000000000000000000000000000000_upd_miwear.watch.q66gl.zip").toFile();
        Files.write(f.toPath(), "not a zip".getBytes());
        FwPolicy.Verdict v = verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true);
        assertTrue(v.failed(C_ZIP));
        assertTrue(v.failed(C_MD5));
        assertFalse(v.allowed());
    }

    @Test
    void wrongFileIsBlocked() throws Exception {
        File f = Fixtures.named(tmp, GL, Fixtures.zipEntries(tmp, new String[] {"readme.txt"}, new byte[] {1}));
        FwPolicy.Verdict v = verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true);
        assertTrue(v.failed(C_TYPE));
        assertTrue(v.failed(C_VERSION));
        assertFalse(v.allowed());
    }

    @Test
    void md5PrefixMismatchIsBlocked() throws Exception {
        File good = glFull("4.200.090");
        File renamed = tmp.resolve("ffffffffffffffffffffffffffffffff_upd_" + GL + ".zip").toFile();
        assertTrue(good.renameTo(renamed));
        assertFails(verdict(renamed, Fixtures.band(GL, GL, "4.200.078", 80), true), C_MD5);
    }

    @Test
    void renamedFileIsBlocked() throws Exception {
        File good = glFull("4.200.090");
        File renamed = tmp.resolve("band11_firmware.zip").toFile();
        assertTrue(good.renameTo(renamed));
        FwPolicy.Verdict v = verdict(renamed, Fixtures.band(GL, GL, "4.200.078", 80), true);
        assertTrue(v.failed(C_NAME));
        assertFalse(v.allowed());
    }

    @Test
    void browserSuffixIsAccepted() throws Exception {
        File good = glFull("4.200.090");
        String withSuffix = good.getName().replace(".zip", " (1).zip");
        File f = tmp.resolve(withSuffix).toFile();
        assertTrue(good.renameTo(f));
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true));
    }

    @Test
    void diffSampleIsBlocked() throws Exception {
        File f = Fixtures.sample("c01c");
        assumeTrue(f != null, "sample c01c missing");
        FwPolicy.Verdict v = verdict(f, Fixtures.band(GL, GL, "4.200.070", 80), true);
        assertTrue(v.failed(C_TYPE));
        assertTrue(v.failed(C_VERSION));
        assertFalse(v.allowed());
    }

    @Test
    void diffForAnotherVersionIsBlocked() throws Exception {
        File f = Fixtures.diff(tmp, GL);
        FwPolicy.Verdict v = verdict(f, Fixtures.band(GL, GL, "4.200.010", 80), true);
        assertTrue(v.failed(C_TYPE));
        assertFalse(v.allowed());
    }

    @Test
    void sameAndOlderVersionsAreBlocked() throws Exception {
        assertFails(verdict(glFull("4.200.078"), Fixtures.band(GL, GL, "4.200.078", 80), true), C_NEWER);
        assertFails(verdict(glFull("4.200.070"), Fixtures.band(GL, GL, "4.200.078", 80), true), C_NEWER);
    }

    @Test
    void batteryBelowFiftyIsBlocked() throws Exception {
        File f = glFull("4.200.090");
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.200.078", 49), true), C_BATTERY);
        assertFails(verdict(f, Fixtures.band(GL, GL, "4.200.078", 50), true));
    }

    @Test
    void staleDataBlocks() throws Exception {
        DeviceSnapshot d = Fixtures.band(GL, GL, "4.200.078", 80);
        d.batteryOk = false;
        d.battery = -1;
        d.batteryError = "браслет не ответил за 10 с";
        assertFails(verdict(glFull("4.200.090"), d, true), C_FRESH, C_BATTERY);
    }

    @Test
    void watchInfoFailureBlocksEverythingDeviceSide() throws Exception {
        DeviceSnapshot d = Fixtures.band(GL, GL, "4.200.078", 80);
        d.watchInfoOk = false;
        d.watchInfoError = "ошибка 5";
        FwPolicy.Verdict v = verdict(glFull("4.200.090"), d, true);
        assertTrue(v.failed(C_FRESH));
        assertTrue(v.failed(C_BRANCH));
        assertTrue(v.failed(C_NEWER));
        assertFalse(v.allowed());
    }

    @Test
    void disconnectedOrOtherOtaModeBlocks() throws Exception {
        File f = glFull("4.200.090");
        DeviceSnapshot d = Fixtures.band(GL, GL, "4.200.078", 80);
        d.connected = false;
        assertTrue(verdict(f, d, true).failed(C_LINK));
        DeviceSnapshot e = Fixtures.band(GL, GL, "4.200.078", 80);
        e.otaMode = 1;
        assertFails(verdict(f, e, true), C_OTA);
    }

    @Test
    void modifiedCopyBlocks() throws Exception {
        File f = glFull("4.200.090");
        FwPolicy.Verdict v = FwPolicy.evaluate(FwName.parse(f.getName()), FwPackage.inspect(f),
                Fixtures.band(GL, GL, "4.200.078", 80), false, true);
        assertFails(v, C_COPY);
    }

    @Test
    void unreadableVersionIsBlocked() throws Exception {
        File f = Fixtures.named(tmp, GL, Fixtures.zipEntries(tmp, new String[] {"ota.sh", "vela_ap.bin",
                "vela_ota.bin", "vela_resource.bin", "META-INF/MANIFEST.MF", "META-INF/CERT.SF", "META-INF/CERT.RSA"},
                Fixtures.image(1000, 0, "")));
        FwPolicy.Verdict v = verdict(f, Fixtures.band(GL, GL, "4.200.078", 80), true);
        assertTrue(v.failed(C_VERSION));
        assertFalse(v.allowed());
    }

    @Test
    void versionHelpers() {
        assertEquals("4.200", DeviceSnapshot.branch("4.200.078"));
        assertEquals(null, DeviceSnapshot.branch("4.200"));
        assertTrue(DeviceSnapshot.compareVersions("4.200.100", "4.200.078") > 0);
        assertTrue(DeviceSnapshot.compareVersions("4.100.999", "4.200.001") < 0);
        assertEquals(0, DeviceSnapshot.compareVersions("4.200.078", "4.200.78"));
    }
}
