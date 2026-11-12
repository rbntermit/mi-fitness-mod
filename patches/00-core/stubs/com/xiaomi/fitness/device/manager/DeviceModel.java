// Compile-only stub of an app class; the real one is in the app. Not packaged.
package com.xiaomi.fitness.device.manager;
public interface DeviceModel {
    com.xiaomi.fitness.device.manager.bean.DeviceInfo getDeviceInfo();
    String getDid();
    com.xiaomi.fitness.device.manager.bean.Product getProduct();
    boolean isDeviceConnected();
}
