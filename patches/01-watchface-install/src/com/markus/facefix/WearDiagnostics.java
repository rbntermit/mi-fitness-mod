package com.markus.facefix;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** Read-only view of the SDK cache. Output never includes a watch, peer id or exception message. */
final class WearDiagnostics {
    private WearDiagnostics() {}

    // Members accessed through public SDK interfaces, not obfuscated implementation classes.
    static final String[] REFLECTED = {
        "Lcom/google/android/libraries/wear/companion/init/CompanionSdk;->INSTANCE:Lcom/google/android/libraries/wear/companion/init/CompanionSdk;",
        "Lcom/google/android/libraries/wear/companion/init/CompanionSdk;->getApiProvider()Ltw4;",
        "Lcom/google/android/libraries/wear/companion/watch/WatchApiProvider;->getWatchApi()Lcom/google/android/libraries/wear/companion/watch/WatchApi;",
        "Lcom/google/android/libraries/wear/companion/watch/WatchApi;->getPairedWatches()Lcom/google/android/libraries/wear/common/stream/ValueStream;",
        "Lcom/google/android/libraries/wear/common/stream/ValueStream;->getCurrentValue()Ljava/lang/Object;",
        "Lcom/google/android/libraries/wear/companion/watch/Watch;->getPeerId()Ljava/lang/String;",
        "Lcom/google/android/libraries/wear/common/result/Result;"
    };

    interface Access {
        Object pairedResult(Object api) throws ReflectiveOperationException;
        Object value(Object result) throws ReflectiveOperationException;
        Object error(Object value) throws ReflectiveOperationException;
        String peerId(Object watch) throws ReflectiveOperationException;
    }

    static Object currentApi() throws ReflectiveOperationException {
        Class<?> sdk = Class.forName("com.google.android.libraries.wear.companion.init.CompanionSdk");
        Object provider = sdk.getMethod("getApiProvider").invoke(sdk.getField("INSTANCE").get(null));
        return Class.forName("com.google.android.libraries.wear.companion.watch.WatchApiProvider")
                .getMethod("getWatchApi").invoke(provider);
    }

    static String describe(Object api, String peerId) {
        if (api == null) return prefix(false, peerId);
        try {
            return describe(api, peerId, new ReflectionAccess());
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return prefix(true, peerId) + "; " + failure(e);
        }
    }

    static String describe(Object api, String peerId, Access access) {
        String base = prefix(api != null, peerId);
        if (api == null) return base;
        try {
            Object result = access.pairedResult(api);
            if (result == null) return base + "; paired=pending";
            Object value = access.value(result);
            Object error = access.error(value);
            if (error != null) {
                // Only documented enum constants; never error.toString() or a server/exception message.
                String code = "OTHER";
                if (error instanceof Enum) {
                    String name = ((Enum<?>) error).name();
                    if ("UNKNOWN".equals(name) || "API_UNAVAILABLE".equals(name)) code = name;
                }
                return base + "; paired=sdk_error; code=" + code;
            }
            if (!(value instanceof List)) return base + "; paired=invalid_result";
            List<?> watches = (List<?>) value;
            base += "; paired=ready; count=" + watches.size();
            if (peerId == null || peerId.isEmpty()) return base + "; match=not_checked";
            boolean match = false;
            for (Object watch : watches) {
                if (peerId.equals(access.peerId(watch))) {
                    match = true;
                    break;
                }
            }
            return base + "; match=" + (match ? "present" : "missing");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return base + "; " + failure(e);
        }
    }

    private static String prefix(boolean api, String peerId) {
        return "api=" + (api ? "available" : "missing") + "; peer="
                + (peerId == null || peerId.isEmpty() ? "missing" : "present");
    }

    static String failure(Throwable e) {
        for (int i = 0; i < 8 && e instanceof InvocationTargetException && e.getCause() != null; i++) {
            e = e.getCause();
        }
        return "diagnostic_error=" + e.getClass().getSimpleName();
    }

    private static final class ReflectionAccess implements Access {
        private final Method paired;
        private final Method current;
        private final Method unbox;
        private final Method error;
        private final Method peer;

        ReflectionAccess() throws ReflectiveOperationException {
            paired = Class.forName("com.google.android.libraries.wear.companion.watch.WatchApi").getMethod("getPairedWatches");
            current = Class.forName("com.google.android.libraries.wear.common.stream.ValueStream").getMethod("getCurrentValue");
            Class<?> result = Class.forName("com.google.android.libraries.wear.common.result.Result");
            // Kotlin inline Result uses these JVM names in 3.60.0i; Java cannot call them directly.
            unbox = result.getMethod("unbox-impl");
            error = result.getMethod("errorOrNull-impl", Object.class);
            peer = Class.forName("com.google.android.libraries.wear.companion.watch.Watch").getMethod("getPeerId");
        }

        public Object pairedResult(Object api) throws ReflectiveOperationException {
            return current.invoke(paired.invoke(api));
        }
        public Object value(Object result) throws ReflectiveOperationException { return unbox.invoke(result); }
        public Object error(Object value) throws ReflectiveOperationException { return error.invoke(null, value); }
        public String peerId(Object watch) throws ReflectiveOperationException { return (String) peer.invoke(watch); }
    }

    /** Repeats unchanged observations so a later recording includes activity from the device process too. */
    static final class RepeatGate {
        static final long REPEAT_MS = 15_000;
        private String previous;
        private long at;

        boolean take(String state, long now) {
            if (state.equals(previous) && now >= at && now - at < REPEAT_MS) return false;
            previous = state;
            at = now;
            return true;
        }

        void reset() { previous = null; }
    }
}
