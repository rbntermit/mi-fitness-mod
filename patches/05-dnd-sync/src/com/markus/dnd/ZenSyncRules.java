package com.markus.dnd;

/**
 * «Простая синхронизация „Не беспокоить“»: what to do when the phone's DND changes. Plain Java, tested.
 * Only phone -> band; only for bands that do not report zen rule sync (capability bit 4) — those get the stock rule
 * sync of patch 05 already; repeated or unknown states are not sent.
 */
public final class ZenSyncRules {
    public enum Decision { SEND_ON, SEND_OFF, SKIP_SWITCH_OFF, SKIP_NO_BAND, SKIP_HAS_RULE_SYNC, SKIP_UNKNOWN_STATE, SKIP_UNCHANGED }

    /** NotificationManager.INTERRUPTION_FILTER_ALL: DND off; NONE/PRIORITY/ALARMS: on; anything else unknown. */
    static final int FILTER_ALL = 1;

    private ZenSyncRules() {
    }

    public static boolean hasRuleSync(long capabilityV3) {
        return (capabilityV3 & 0x4L) != 0;
    }

    /** null when the filter does not say. */
    public static Boolean phoneOn(int interruptionFilter) {
        if (interruptionFilter == FILTER_ALL) {
            return Boolean.FALSE;
        }
        return interruptionFilter >= 2 && interruptionFilter <= 4 ? Boolean.TRUE : null;
    }

    public static Decision decide(boolean switchOn, boolean bandBound, boolean hasRuleSync, Boolean phoneOn,
            Boolean lastSent) {
        if (!switchOn) {
            return Decision.SKIP_SWITCH_OFF;
        }
        if (!bandBound) {
            return Decision.SKIP_NO_BAND;
        }
        if (hasRuleSync) {
            return Decision.SKIP_HAS_RULE_SYNC;
        }
        if (phoneOn == null) {
            return Decision.SKIP_UNKNOWN_STATE;
        }
        if (phoneOn.equals(lastSent)) {
            return Decision.SKIP_UNCHANGED;
        }
        return phoneOn ? Decision.SEND_ON : Decision.SEND_OFF;
    }

    /** The switch is offered only where it can matter. */
    public static boolean offerSwitch(boolean bandBound, boolean hasRuleSync) {
        return bandBound && !hasRuleSync;
    }
}
