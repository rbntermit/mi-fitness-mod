package com.markus.fw;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** What the app and the band report right now. Filled by MarkusFwBridge; plain data so the rules can be tested. */
public final class DeviceSnapshot {
    private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    public boolean hasDevice;
    public boolean connected;
    /** Model from the binding stored in the app (DeviceInfo.getModel). */
    public String boundModel;
    /** Firmware version cached by the app (DeviceInfo.getFirmwareVersion). */
    public String cachedVersion;
    /** readWatchInfo answered just now. */
    public boolean watchInfoOk;
    public String watchInfoError;
    /** Values the band itself reported through readWatchInfo. */
    public String reportedModel;
    public String liveVersion;
    public String productDevice;
    public Integer versionChannel;
    /** Battery read from the band just now (syncBattery); -1 when unknown. */
    public boolean batteryOk;
    public int battery = -1;
    public String batteryError;
    /** Product.ota of the bound model: 2 = general OTA through prepareOta/sendFile. */
    public int otaMode = -1;

    /** "4.200" for "4.200.078"; null when the version is not three numbers. */
    public static String branch(String version) {
        Matcher m = version == null ? null : VERSION.matcher(version.trim());
        return m != null && m.matches() ? Integer.parseInt(m.group(1)) + "." + m.group(2) : null;
    }

    /** Numeric comparison of "a.b.c"; throws when either is not a version. */
    public static int compareVersions(String x, String y) {
        Matcher a = VERSION.matcher(x.trim());
        Matcher b = VERSION.matcher(y.trim());
        if (!a.matches() || !b.matches()) {
            throw new IllegalArgumentException("not a version: " + x + " / " + y);
        }
        for (int i = 1; i <= 3; i++) {
            int c = Long.compare(Long.parseLong(a.group(i)), Long.parseLong(b.group(i)));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /**
     * "да" for q66tc, "нет" for gl/cn by the model suffix. For the hardware codes the band reports, the answer comes
     * from miwear_product.json of 4.200.078 (M2616B1 = no NFC, M2617B1 = NFC). Display only: the install rules read
     * the table of the file being installed, never this.
     */
    public static String nfcByModel(String model) {
        if ("M2617B1".equals(model)) {
            return "да (M2617B1, по таблице прошивки 4.200.078; только для показа)";
        }
        if ("M2616B1".equals(model)) {
            return "нет (M2616B1, по таблице прошивки 4.200.078; только для показа)";
        }
        String r = FwName.splitModel(model)[1];
        return r == null ? "неизвестно" : "tc".equals(r) ? "да (tc)" : "нет (" + r + ")";
    }

    /** Diagnostic block: every value the rules use, without serial number or device id. */
    public String diagnostics() {
        StringBuilder sb = new StringBuilder();
        line(sb, "Устройство подключено", hasDevice ? (connected ? "да" : "нет") : "нет устройства");
        line(sb, "Модель из привязки", boundModel);
        line(sb, "Модель от браслета", watchInfoOk ? reportedModel : "не получена: " + watchInfoError);
        line(sb, "productDevice", watchInfoOk ? productDevice : "—");
        line(sb, "versionChannel", watchInfoOk ? String.valueOf(versionChannel) : "—");
        line(sb, "Версия от браслета", watchInfoOk ? liveVersion : "—");
        line(sb, "Версия в приложении", cachedVersion);
        String br = branch(watchInfoOk ? liveVersion : null);
        line(sb, "Ветка", br == null ? "не определена" : br + ".x");
        line(sb, "NFC по модели из привязки", nfcByModel(boundModel));
        line(sb, "NFC по модели от браслета", watchInfoOk ? nfcByModel(reportedModel) : "—");
        line(sb, "Заряд (свежий)", batteryOk ? battery + " %" : "не получен: " + batteryError);
        line(sb, "otaMode", String.valueOf(otaMode));
        return sb.toString();
    }

    private static void line(StringBuilder sb, String k, String v) {
        sb.append(k).append(": ").append(v == null ? "нет данных" : v).append('\n');
    }
}
