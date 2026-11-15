package com.markus.nfc;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NfcLoadStateTest {
    @Test void aLateOldResultCannotFinishTheNewLoad(){NfcLoadState s=new NfcLoadState();long old=s.begin(),now=s.begin();assertFalse(s.complete(old));assertTrue(s.timeout(now));assertTrue(s.complete(now));}
    @Test void timeoutStillAcceptsTheCurrentSdkResult(){NfcLoadState s=new NfcLoadState();long id=s.begin();assertTrue(s.timeout(id));assertTrue(s.complete(id));}
    @Test void timeoutIsReportedOnce(){NfcLoadState s=new NfcLoadState();long id=s.begin();assertTrue(s.timeout(id));assertFalse(s.timeout(id));}
    @Test void successCancelsTheTimeout(){NfcLoadState s=new NfcLoadState();long id=s.begin();assertTrue(s.complete(id));assertFalse(s.timeout(id));assertFalse(s.complete(id));}
    @Test void anOldDeadlineCannotDismissTheNewLoad(){NfcLoadState s=new NfcLoadState();long old=s.begin(),now=s.begin();assertFalse(s.timeout(old));assertTrue(s.timeout(now));}
    @Test void leavingTheScreenSuppressesCallbacks(){NfcLoadState s=new NfcLoadState();long id=s.begin();s.destroy();assertFalse(s.timeout(id));assertFalse(s.complete(id));assertThrows(IllegalStateException.class,s::begin);}
}
