package com.markus.fw;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether a stored firmware file may be sent to the connected band. Every rule is a separate check
 * with a reason; installation is allowed only when all of them pass. Nothing here talks to the band.
 */
public final class FwPolicy {
    /** Branch of the global firmware (4.200.078-gl); the Chinese one is 4.100.x (4.100.179-cn). */
    static final String GLOBAL_BRANCH = "4.200";
    static final int MIN_BATTERY = 50;
    /** Product.ota value of the general OTA path (prepareOta + sendFile). Other modes are not supported. */
    static final int OTA_MODE_GENERAL = 2;

    public static final class Check {
        public final String title;
        public final boolean ok;
        public final String detail;

        Check(String title, boolean ok, String detail) {
            this.title = title;
            this.ok = ok;
            this.detail = detail;
        }

        @Override
        public String toString() {
            return (ok ? "OK  " : "НЕТ ") + title + (detail == null ? "" : ": " + detail);
        }
    }

    public static final class Verdict {
        public final List<Check> checks = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();

        public boolean allowed() {
            for (Check c : checks) {
                if (!c.ok) {
                    return false;
                }
            }
            return !checks.isEmpty();
        }

        /** First failed check, for the list screen. */
        public String firstProblem() {
            for (Check c : checks) {
                if (!c.ok) {
                    return c.title + (c.detail == null ? "" : ": " + c.detail);
                }
            }
            return null;
        }

        public boolean failed(String title) {
            for (Check c : checks) {
                if (c.title.equals(title)) {
                    return !c.ok;
                }
            }
            return false;
        }

        void add(String title, boolean ok, String detail) {
            checks.add(new Check(title, ok, detail));
        }
    }

    public static final String C_COPY = "Копия в приложении не изменена";
    public static final String C_ZIP = "Архив цел";
    public static final String C_NAME = "Имя файла";
    public static final String C_MD5 = "MD5 из имени";
    public static final String C_TYPE = "Полный пакет";
    public static final String C_VERSION = "Версия из ro.build.id";
    public static final String C_REGION = "Регион файла";
    public static final String C_FAMILY = "Семейство модели";
    public static final String C_LINK = "Браслет подключён";
    public static final String C_FRESH = "Свежие данные от браслета";
    public static final String C_SOURCES = "Источники согласованы";
    public static final String C_BRANCH = "Ветка прошивки";
    public static final String C_MODEL = "Модель и регион";
    public static final String C_NEWER = "Версия новее текущей";
    public static final String C_BATTERY = "Заряд";
    public static final String C_OTA = "Способ обновления";
    public static final String C_GATE = "Установка включена";

    /**
     * The one pair where the band reports a hardware code instead of a model id: an NFC Band 11 bound as q66tc says
     * "M2617B1". It counts as the band confirming q66tc only when the miwear_product.json of the file being installed
     * links exactly these two. No other pair is accepted this way.
     */
    static final String TC_MODEL = "miwear.watch.q66tc";
    static final String TC_HARDWARE = "M2617B1";
    static final String W_TC_GLOBAL = "китайская NFC-модель на глобальной прошивке, безопасность обновления не гарантируется";

    /** How the band's own report matches the bound model. */
    static final class ModelMatch {
        /** The band confirmed the bound model (directly or through the file's table). */
        final boolean confirmed;
        /** Confirmed through the q66tc / M2617B1 rule, not by an identical model id. */
        final boolean tcByHardware;
        final String detail;

        ModelMatch(boolean confirmed, boolean tcByHardware, String detail) {
            this.confirmed = confirmed;
            this.tcByHardware = tcByHardware;
            this.detail = detail;
        }
    }

    static ModelMatch matchModel(DeviceSnapshot d, FwPackage.Info file) {
        if (!d.watchInfoOk) {
            return new ModelMatch(false, false, "браслет не сообщил модель");
        }
        if (d.boundModel != null && d.boundModel.equals(d.reportedModel)) {
            return new ModelMatch(true, false, null);
        }
        if (!(TC_MODEL.equals(d.boundModel) && TC_HARDWARE.equals(d.reportedModel))) {
            return new ModelMatch(false, false, "привязка " + d.boundModel + ", браслет " + d.reportedModel
                    + " — эта пара не подтверждает модель");
        }
        String pair = "привязка q66tc, браслет M2617B1";
        if (!file.productTableFound) {
            return new ModelMatch(false, false, pair + ": в файле нет таблицы miwear_product.json, связь проверить нельзя");
        }
        if (file.productTableError != null) {
            return new ModelMatch(false, false, pair + ": " + file.productTableError);
        }
        if (!file.hasDeviceModel(TC_HARDWARE)) {
            return new ModelMatch(false, false, pair + ": в таблице файла нет M2617B1");
        }
        java.util.Set<String> codes = file.deviceModelsOf(TC_MODEL);
        if (codes.isEmpty()) {
            return new ModelMatch(false, false, pair + ": в таблице файла нет q66tc");
        }
        if (!codes.equals(java.util.Collections.singleton(TC_HARDWARE))) {
            return new ModelMatch(false, false, pair + ": в таблице файла q66tc связан с " + codes + ", а не только с M2617B1");
        }
        return new ModelMatch(true, true, pair + ": в таблице файла M2617B1 связан с q66tc");
    }

    private FwPolicy() {
    }

    public static Verdict evaluate(FwName name, FwPackage.Info file, DeviceSnapshot d, boolean copyIntact,
            boolean installEnabled) {
        Verdict v = new Verdict();

        v.add(C_COPY, copyIntact, copyIntact ? null : "SHA-256 копии не совпадает с записью, файл нужно добавить заново");
        v.add(C_ZIP, file.zipOk, file.zipOk ? null : file.zipError);

        boolean named = name.recognized();
        v.add(C_NAME, named, named
                ? (name.suffix.isEmpty() ? name.model : name.model + ", суффикс «" + name.suffix.trim() + "» не мешает")
                : "имя «" + name.original + "» изменено: из него не видно модель и регион");

        if (name.md5 != null) {
            boolean same = name.md5.equals(file.md5);
            v.add(C_MD5, same, same ? null : "в имени " + name.md5 + ", у файла " + file.md5);
        } else {
            v.add(C_MD5, true, "в имени нет MD5; целостность проверена по CRC записей ZIP");
        }

        boolean full = file.type == FwPackage.Type.FULL;
        v.add(C_TYPE, full, full ? null : file.type == FwPackage.Type.DIFF
                ? "дифф-пакеты отключены: по файлу нельзя подтвердить исходную версию"
                : "состав архива не похож на полный пакет прошивки");

        boolean hasVersion = full && file.version != null;
        v.add(C_VERSION, hasVersion, hasVersion ? file.version
                : file.problems.isEmpty() ? "версию из файла прочитать нельзя" : String.join("; ", file.problems));

        boolean regionOk = hasVersion && named && name.region.equals(file.region);
        v.add(C_REGION, regionOk, hasVersion && named
                ? "в имени " + name.region + ", в ro.build.id " + file.region
                : "нет данных для сравнения");

        ModelMatch match = matchModel(d, file);
        String boundFamily = FwName.splitModel(d.boundModel)[0];
        // a hardware code like M2617B1 has no family of its own; it inherits the bound one only when the file confirms the pair
        String reportedFamily = !d.watchInfoOk ? null
                : match.tcByHardware ? boundFamily : FwName.splitModel(d.reportedModel)[0];
        String imageFamily = file.imageFamily();
        boolean familyOk = named && imageFamily != null && boundFamily != null && reportedFamily != null
                && name.family.equals(imageFamily) && name.family.equals(boundFamily)
                && name.family.equals(reportedFamily);
        v.add(C_FAMILY, familyOk, "имя " + name.family + ", miwear_model " + imageFamily
                + ", привязка " + boundFamily + ", браслет " + reportedFamily);

        v.add(C_LINK, d.hasDevice && d.connected, d.hasDevice ? (d.connected ? null : "браслет не подключён")
                : "в приложении нет текущего устройства");

        boolean fresh = d.watchInfoOk && d.batteryOk;
        v.add(C_FRESH, fresh, fresh ? null : (d.watchInfoOk ? "" : "readWatchInfo: " + d.watchInfoError + "; ")
                + (d.batteryOk ? "" : "заряд: " + d.batteryError));

        boolean sameVersion = d.cachedVersion == null || d.cachedVersion.equals(d.liveVersion);
        boolean sources = match.confirmed && sameVersion;
        v.add(C_SOURCES, sources, sources ? match.detail : (match.confirmed ? "" : match.detail + "; ")
                + "версия в приложении " + d.cachedVersion + " / от браслета " + d.liveVersion);

        String current = d.watchInfoOk ? DeviceSnapshot.branch(d.liveVersion) : null;
        String target = hasVersion ? DeviceSnapshot.branch(file.version) : null;
        boolean branchOk = current != null && current.equals(target);
        v.add(C_BRANCH, branchOk, current == null ? "ветку браслета подтвердить нельзя"
                : target == null ? "ветка файла неизвестна"
                : branchOk ? current + ".x" : "переход " + current + ".x → " + target + ".x этой функцией запрещён");

        modelRule(v, name, d, current, match, file);

        boolean newer = false;
        String newerDetail = "нет данных для сравнения";
        if (hasVersion && d.watchInfoOk && DeviceSnapshot.branch(d.liveVersion) != null) {
            int c = DeviceSnapshot.compareVersions(file.version, d.liveVersion);
            newer = c > 0;
            newerDetail = file.version + (c > 0 ? " новее " : c == 0 ? " — та же версия, что " : " старше ") + d.liveVersion;
        }
        v.add(C_NEWER, newer, newerDetail);

        boolean battery = d.batteryOk && d.battery >= MIN_BATTERY;
        v.add(C_BATTERY, battery, d.batteryOk ? d.battery + " % (нужно не меньше " + MIN_BATTERY + " %)" : "заряд не получен");

        v.add(C_OTA, d.otaMode == OTA_MODE_GENERAL, "otaMode " + d.otaMode
                + (d.otaMode == OTA_MODE_GENERAL ? "" : ": поддерживается только " + OTA_MODE_GENERAL));

        v.add(C_GATE, installEnabled, installEnabled ? null
                : "выключена в этой сборке до подтверждения данных диагностики");
        return v;
    }

    /** Which file region fits which band, by the owner's rules. */
    private static void modelRule(Verdict v, FwName name, DeviceSnapshot d, String currentBranch, ModelMatch match,
            FwPackage.Info info) {
        String devRegion = FwName.splitModel(d.boundModel)[1];
        String reportedRegion = d.watchInfoOk ? FwName.splitModel(d.reportedModel)[1] : null;
        if (!name.recognized() || devRegion == null) {
            v.add(C_MODEL, false, "модель файла или браслета неизвестна");
            return;
        }
        String file = name.region;
        if ("tc".equals(devRegion) || "tc".equals(reportedRegion)) {
            if ("gl".equals(file)) {
                // the only exception: the band itself confirmed q66tc as M2617B1 through this file's table,
                // it already runs the global branch, and the file is a full q66gl package
                boolean exception = match.tcByHardware && GLOBAL_BRANCH.equals(currentBranch)
                        && "miwear.watch.q66gl".equals(name.model) && info.type == FwPackage.Type.FULL;
                if (exception) {
                    v.add(C_MODEL, true, "q66tc подтверждён браслетом (M2617B1 по таблице файла), на браслете уже ветка "
                            + GLOBAL_BRANCH + ".x");
                    v.warnings.add(W_TC_GLOBAL);
                } else {
                    v.add(C_MODEL, false, "NFC-модель q66tc: gl разрешён, только если браслет сам сообщает M2617B1, "
                            + "таблица файла связывает M2617B1 с q66tc, на браслете ветка " + GLOBAL_BRANCH
                            + ".x и файл — полный q66gl");
                }
                return;
            }
        }
        if ("gl".equals(file)) {
            if ("gl".equals(devRegion)) {
                v.add(C_MODEL, true, null);
            } else if ("cn".equals(devRegion) && GLOBAL_BRANCH.equals(currentBranch)) {
                v.add(C_MODEL, true, "q66cn, на браслете уже глобальная ветка " + GLOBAL_BRANCH + ".x");
                v.warnings.add("Китайский браслет на глобальной прошивке");
            } else {
                v.add(C_MODEL, false, "файл gl для браслета " + devRegion + " на ветке "
                        + (currentBranch == null ? "неизвестно" : currentBranch + ".x"));
            }
            return;
        }
        // cn or tc file
        if (GLOBAL_BRANCH.equals(currentBranch)) {
            v.add(C_MODEL, false, "файл " + file + " на браслете с глобальной веткой " + GLOBAL_BRANCH + ".x запрещён");
        } else if (file.equals(devRegion)) {
            v.add(C_MODEL, true, null);
        } else {
            v.add(C_MODEL, false, "файл " + file + " для браслета " + devRegion);
        }
    }
}
