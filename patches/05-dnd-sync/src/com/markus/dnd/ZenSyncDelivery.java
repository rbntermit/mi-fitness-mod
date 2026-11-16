package com.markus.dnd;

/** State of one-way DND delivery, owned by MarkusZenSync's serial worker. */
public final class ZenSyncDelivery {
    private String device;
    private Boolean acknowledged;
    private Boolean pending;
    private long generation;

    public void selectDevice(String identity) {
        if (identity == null || !identity.equals(device)) {
            reset();
            device = identity;
        }
    }

    public void reset() {
        device = null;
        acknowledged = null;
        pending = null;
        generation++;
    }

    public boolean isPending(boolean on) {
        return pending != null && pending.booleanValue() == on;
    }

    public Boolean acknowledged() {
        // An opposite request may already have reached the band before its callback.
        return pending == null ? acknowledged : null;
    }

    public long begin(boolean on) {
        pending = on;
        return ++generation;
    }

    public boolean complete(long ticket, boolean success) {
        if (ticket != generation || pending == null) return false;
        // An error/timeout leaves the device state unknown; allow sending it again.
        acknowledged = success ? pending : null;
        pending = null;
        return true;
    }
}
