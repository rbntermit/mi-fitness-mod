package com.xiaomi.fitness.device.contact.export;
// Compile-only app interface; never packaged.
public interface OnSyncCallback {
    void onSuccess(String identity, int type, SyncResult result);
    void onError(String identity, int type, int error);
}
