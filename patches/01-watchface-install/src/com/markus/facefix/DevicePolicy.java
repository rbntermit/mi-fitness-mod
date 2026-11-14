package com.markus.facefix;

/** Product.type 1 includes DualCoreWearOS (o61w). Types 6/7 also include BLE bands, e.g. o66gl. */
public final class DevicePolicy {
    private DevicePolicy() {}
    public static boolean localFaces(Integer type) {
        return type != null && type > 0 && type != 1;
    }
    public static String corePackage(String stock, boolean localFaces, boolean lyraExists) {
        return "com.xiaomi.mi_connect_service".equals(stock) && localFaces && lyraExists
                ? "com.xiaomi.lyra" : stock;
    }
}