package com.markus.dnd;

import static com.markus.dnd.ZenSyncRules.Decision.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZenSyncRulesTest {
    @Test
    void capabilityBit4() {
        assertTrue(ZenSyncRules.hasRuleSync(604119053L), "q66tc from the owner's log");
        assertFalse(ZenSyncRules.hasRuleSync(0L));
        assertFalse(ZenSyncRules.hasRuleSync(0xFBL));
    }

    @Test
    void phoneFilter() {
        assertEquals(Boolean.FALSE, ZenSyncRules.phoneOn(1));
        assertEquals(Boolean.TRUE, ZenSyncRules.phoneOn(2));
        assertEquals(Boolean.TRUE, ZenSyncRules.phoneOn(3));
        assertEquals(Boolean.TRUE, ZenSyncRules.phoneOn(4));
        assertNull(ZenSyncRules.phoneOn(0));
        assertNull(ZenSyncRules.phoneOn(9));
    }

    @Test
    void sendsOnlyWhenAllowedAndChanged() {
        assertEquals(SEND_ON, ZenSyncRules.decide(true, true, false, true, null));
        assertEquals(SEND_OFF, ZenSyncRules.decide(true, true, false, false, true));
        assertEquals(SKIP_UNCHANGED, ZenSyncRules.decide(true, true, false, true, true));
        assertEquals(SKIP_SWITCH_OFF, ZenSyncRules.decide(false, true, false, true, null));
        assertEquals(SKIP_NO_BAND, ZenSyncRules.decide(true, false, false, true, null));
        assertEquals(SKIP_HAS_RULE_SYNC, ZenSyncRules.decide(true, true, true, true, null), "q66tc keeps patch 05's own sync");
        assertEquals(SKIP_UNKNOWN_STATE, ZenSyncRules.decide(true, true, false, null, null));
    }

    @Test
    void switchOnlyForBandsWithoutRuleSync() {
        assertTrue(ZenSyncRules.offerSwitch(true, false));
        assertFalse(ZenSyncRules.offerSwitch(true, true), "not shown on q66tc");
        assertFalse(ZenSyncRules.offerSwitch(false, false));
    }
}
