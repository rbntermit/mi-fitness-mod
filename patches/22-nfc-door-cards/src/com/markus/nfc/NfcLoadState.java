package com.markus.nfc;

/** One screen's load generations: a late SDK result must not finish a newer request. */
final class NfcLoadState {
    private long generation;
    private boolean finished, notified, destroyed;
    synchronized long begin() {
        if(destroyed) throw new IllegalStateException("screen destroyed");
        finished=false;notified=false;return ++generation;
    }
    synchronized boolean timeout(long id) {
        if(destroyed || finished || notified || id!=generation)return false;
        notified=true;return true;
    }
    synchronized boolean complete(long id) {
        if(destroyed || finished || id!=generation)return false;
        finished=true;return true;
    }
    synchronized void destroy(){destroyed=true;generation++;}
}
