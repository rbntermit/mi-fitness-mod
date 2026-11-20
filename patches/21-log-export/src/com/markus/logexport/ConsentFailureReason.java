package com.markus.logexport;

import java.util.Locale;

/** Log only a fixed reason label, never a Google exception message or its arguments. */
final class ConsentFailureReason {
    enum Reason { SIGNATURE, CALLING_PACKAGE, PERMISSION, UNKNOWN }

    private ConsentFailureReason() {}

    static Reason classify(String message) {
        if (message == null) return Reason.UNKNOWN;
        String text = message.toLowerCase(Locale.ROOT);
        if (text.contains("signature mismatch") || text.contains("signature verification failed")
                || text.contains("signature check failed") || text.contains("invalid signature")
                || text.contains("certificate mismatch") || text.contains("untrusted certificate")
                || text.contains("not signed with") || text.contains("not signed by")) {
            return Reason.SIGNATURE;
        }
        if (text.contains("unknown calling package") || text.contains("invalid calling package")
                || text.contains("unrecognized calling package") || text.contains("does not belong to uid")
                || text.contains("not associated with uid") || text.contains("calling package is not allowed")) {
            return Reason.CALLING_PACKAGE;
        }
        if (text.contains("permission denial") || text.contains("permission denied")
                || text.contains("requires permission") || text.contains("missing permission")
                || text.contains("does not have permission") || text.contains("must have one of:")) {
            return Reason.PERMISSION;
        }
        return Reason.UNKNOWN;
    }
}
