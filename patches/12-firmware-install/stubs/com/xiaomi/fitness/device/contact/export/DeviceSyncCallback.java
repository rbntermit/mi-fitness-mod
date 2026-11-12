// Compile-only stub of an app class; the real one is in the app. Not packaged.
package com.xiaomi.fitness.device.contact.export;
public interface DeviceSyncCallback {
    void onSyncError(String did, int type, String msg);
    void onSyncSuccess(String did, int type, Object data);
}
