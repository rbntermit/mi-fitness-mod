package com.markus.facefix;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WearDiagnosticsTest {
    private static final Object API = new Object();
    private static final String SECRET = "stored-private-peer";
    private enum Error { UNKNOWN, API_UNAVAILABLE, UNEXPECTED_PRIVATE_VALUE }

    private static final class FakeAccess implements WearDiagnostics.Access {
        Object result = new Object();
        Object value = Collections.emptyList();
        Object error;
        Throwable failure;
        int reads;
        int peerReads;

        public Object pairedResult(Object api) throws ReflectiveOperationException {
            reads++;
            if (failure instanceof ReflectiveOperationException) throw (ReflectiveOperationException) failure;
            if (failure instanceof RuntimeException) throw (RuntimeException) failure;
            if (failure instanceof LinkageError) throw (LinkageError) failure;
            return result;
        }
        public Object value(Object result) { return value; }
        public Object error(Object value) { return error; }
        public String peerId(Object watch) {
            peerReads++;
            if (watch == null) throw new IllegalArgumentException(SECRET);
            return (String) watch;
        }
    }

    private static String describe(FakeAccess a) {
        String result = WearDiagnostics.describe(API, SECRET, a);
        assertFalse(result.contains(SECRET));
        return result;
    }

    @Test void missingApiDoesNotReadSdk() {
        FakeAccess a = new FakeAccess();
        assertEquals("api=missing; peer=present", WearDiagnostics.describe(null, SECRET, a));
        assertEquals(0, a.reads);
    }
    @Test void missingApiAndPeerAreSeparate() {
        assertEquals("api=missing; peer=missing", WearDiagnostics.describe(null, null));
        assertEquals("api=missing; peer=missing", WearDiagnostics.describe(null, ""));
    }
    @Test void pendingIsDistinctFromEmptyPairing() {
        FakeAccess a = new FakeAccess();
        a.result = null;
        assertEquals("api=available; peer=present; paired=pending", describe(a));
    }
    @Test void emptyPairingReportsNoMatch() {
        assertEquals("api=available; peer=present; paired=ready; count=0; match=missing", describe(new FakeAccess()));
    }
    @Test void otherPairedWatchIsNotTheBoundWatch() {
        FakeAccess a = new FakeAccess();
        a.value = Arrays.asList("other-private-peer");
        assertEquals("api=available; peer=present; paired=ready; count=1; match=missing", describe(a));
    }
    @Test void matchCanBeAfterAnotherWatch() {
        FakeAccess a = new FakeAccess();
        a.value = Arrays.asList("other-private-peer", SECRET);
        assertEquals("api=available; peer=present; paired=ready; count=2; match=present", describe(a));
    }
    @Test void comparisonIsExactAsInSdk() {
        FakeAccess a = new FakeAccess();
        a.value = Arrays.asList(SECRET.toUpperCase(java.util.Locale.ROOT));
        assertTrue(describe(a).endsWith("match=missing"));
    }
    @Test void missingSavedPeerDoesNotInspectIds() {
        FakeAccess a = new FakeAccess();
        a.value = Arrays.asList(new Object());
        assertEquals("api=available; peer=missing; paired=ready; count=1; match=not_checked",
                WearDiagnostics.describe(API, null, a));
        assertEquals(0, a.peerReads);
        assertTrue(WearDiagnostics.describe(API, "", a).endsWith("match=not_checked"));
    }
    @Test void sdkUnavailableIsDistinctFromPending() {
        FakeAccess a = new FakeAccess();
        a.error = Error.API_UNAVAILABLE;
        assertEquals("api=available; peer=present; paired=sdk_error; code=API_UNAVAILABLE", describe(a));
    }
    @Test void sdkUnknownIsReportedWithoutInventingCause() {
        FakeAccess a = new FakeAccess();
        a.error = Error.UNKNOWN;
        assertTrue(describe(a).endsWith("paired=sdk_error; code=UNKNOWN"));
    }
    @Test void undocumentedEnumNameIsNotCopied() {
        FakeAccess a = new FakeAccess();
        a.error = Error.UNEXPECTED_PRIVATE_VALUE;
        assertTrue(describe(a).endsWith("code=OTHER"));
        assertFalse(describe(a).contains("PRIVATE_VALUE"));
    }
    @Test void errorObjectIsNeverStringified() {
        FakeAccess a = new FakeAccess();
        a.error = new Object() { public String toString() { throw new AssertionError("do not stringify"); } };
        assertTrue(describe(a).endsWith("code=OTHER"));
    }
    @Test void invalidResultIsNotStringifiedOrCalledEmpty() {
        FakeAccess a = new FakeAccess();
        a.value = new Object() { public String toString() { throw new AssertionError("do not stringify"); } };
        assertTrue(describe(a).endsWith("paired=invalid_result"));
    }
    @Test void nullSuccessValueIsNotAnEmptyList() {
        FakeAccess a = new FakeAccess();
        a.value = null;
        assertTrue(describe(a).endsWith("paired=invalid_result"));
    }
    @Test void reflectionErrorIsNotReportedAsMissingPairing() {
        FakeAccess a = new FakeAccess();
        a.failure = new NoSuchMethodException(SECRET);
        assertTrue(describe(a).endsWith("diagnostic_error=NoSuchMethodException"));
    }
    @Test void runtimeErrorDoesNotEscapeOrExposeMessage() {
        FakeAccess a = new FakeAccess();
        a.failure = new IllegalStateException(SECRET);
        assertTrue(describe(a).endsWith("diagnostic_error=IllegalStateException"));
    }
    @Test void absentSdkClassDoesNotBreakCaller() {
        FakeAccess a = new FakeAccess();
        a.failure = new NoClassDefFoundError(SECRET);
        assertTrue(describe(a).endsWith("diagnostic_error=NoClassDefFoundError"));
    }
    @Test void invocationFailureReportsCauseTypeOnly() {
        FakeAccess a = new FakeAccess();
        a.failure = new InvocationTargetException(new IllegalStateException(SECRET));
        assertTrue(describe(a).endsWith("diagnostic_error=IllegalStateException"));
    }
    @Test void malformedListCannotBecomeFalseNoMatch() {
        FakeAccess a = new FakeAccess();
        a.value = Arrays.asList((Object) null);
        assertTrue(describe(a).endsWith("count=1; diagnostic_error=IllegalArgumentException"));
    }
    @Test void pairedIdsNeverAppearInSummary() {
        FakeAccess a = new FakeAccess();
        a.value = Arrays.asList("other-private-peer", SECRET);
        assertFalse(describe(a).contains("other-private-peer"));
    }

    @Test void firstObservationIsLogged() {
        assertTrue(new WearDiagnostics.RepeatGate().take("pending", 100));
    }
    @Test void rapidDuplicateIsSuppressed() {
        WearDiagnostics.RepeatGate g = new WearDiagnostics.RepeatGate();
        g.take("pending", 100);
        assertFalse(g.take("pending", 101));
        assertFalse(g.take("pending", 15_099));
    }
    @Test void unchangedStateIsRepeatedForLaterRecording() {
        WearDiagnostics.RepeatGate g = new WearDiagnostics.RepeatGate();
        g.take("pending", 100);
        assertTrue(g.take("pending", 15_100));
        assertFalse(g.take("pending", 15_101));
    }
    @Test void stateChangeIsNeverDelayed() {
        WearDiagnostics.RepeatGate g = new WearDiagnostics.RepeatGate();
        g.take("pending", 100);
        assertTrue(g.take("ready", 101));
        assertTrue(g.take("pending", 102));
    }
    @Test void exportResetMakesNextObservationVisibleAgain() {
        WearDiagnostics.RepeatGate g = new WearDiagnostics.RepeatGate();
        g.take("missing", 100);
        g.reset();
        assertTrue(g.take("missing", 101));
    }
    @Test void clockResetDoesNotHideDiagnostics() {
        WearDiagnostics.RepeatGate g = new WearDiagnostics.RepeatGate();
        g.take("missing", 100);
        assertTrue(g.take("missing", 50));
    }
}
