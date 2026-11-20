package com.markus.logexport;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ConsentFailureReasonTest {
    @Test void classifiesOnlyExplicitReasons() {
        assertEquals(ConsentFailureReason.Reason.SIGNATURE,
                ConsentFailureReason.classify("Permission Denial: signature verification failed"));
        assertEquals(ConsentFailureReason.Reason.CALLING_PACKAGE,
                ConsentFailureReason.classify("Unknown calling package name 'some.example.package'"));
        assertEquals(ConsentFailureReason.Reason.PERMISSION,
                ConsentFailureReason.classify("Caller must have one of: [android.permission.EXAMPLE]"));
    }

    @Test void missingOrGenericErrorsDoNotGuess() {
        assertEquals(ConsentFailureReason.Reason.UNKNOWN, ConsentFailureReason.classify(null));
        assertEquals(ConsentFailureReason.Reason.UNKNOWN, ConsentFailureReason.classify("Caller not allowed"));
        assertEquals(ConsentFailureReason.Reason.UNKNOWN, ConsentFailureReason.classify("Google consent failed"));
    }
}
