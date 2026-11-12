package com.markus.fw;

/**
 * Master switch for sending firmware. Enabled on 03.11 after the owner confirmed the diagnostic values
 * (q66tc bound, band reports M2617B1, branch 4.200.x). Every check in FwPolicy still has to pass;
 * set to false to build a version in which the send code is not compiled in at all.
 */
public final class FwGate {
    public static final boolean INSTALL_ENABLED = true;

    private FwGate() {
    }
}
