package com.markus.fw;

/**
 * Access to the band. Implemented by MarkusFwBridge in the unnamed package, because the app's
 * obfuscated callback classes (rr7, jir, edj) live there and cannot be named from a package.
 */
public interface DeviceBridge {
    interface SnapshotCallback {
        /** Called once on the main thread. */
        void onSnapshot(DeviceSnapshot snapshot);
    }

    interface InstallListener {
        void onProgress(int percent);

        /** The file reached the band. This is not success: the band still has to install it and report the version. */
        void onTransferred();

        /** Final failure; nothing is retried automatically. */
        void onFailed(String reason);
    }

    /** Reads binding data, then asks the band for watch info and battery. Times out after 10 s. */
    void readSnapshot(SnapshotCallback callback);

    /** prepareOta with the real version, MD5 and size of the file, then sends the file unchanged. */
    void install(String path, String version, String md5, int size, InstallListener listener);

    final class Holder {
        private Holder() {
        }

        public static DeviceBridge get() {
            try {
                return (DeviceBridge) Class.forName("MarkusFwBridge").newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("MarkusFwBridge is missing", e);
            }
        }
    }
}
