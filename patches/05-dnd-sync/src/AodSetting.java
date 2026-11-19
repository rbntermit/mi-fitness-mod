/** The modes and two local times used by the stock Always-on display screen. */
final class AodSetting {
    static final int OFF = 1;
    static final int SMART = 2;
    static final int SCHEDULED = 3;
    static final int NO_TIME = -1;

    enum Action { NONE, PAUSE, RESTORE, FORGET }

    final int mode;
    final int start;
    final int end;

    private AodSetting(int mode, int start, int end) {
        this.mode = mode;
        this.start = start;
        this.end = end;
    }

    static boolean supported(String model) {
        return "miwear.watch.o66gl".equals(model);
    }

    static AodSetting of(int mode, int start, int end) {
        if (mode < OFF || mode > SCHEDULED || start < NO_TIME || start >= 1440 || end < NO_TIME || end >= 1440) {
            return null;
        }
        if (mode == SCHEDULED && (start == NO_TIME || end == NO_TIME)) return null;
        return new AodSetting(mode, start, end);
    }

    static Action decide(boolean pause, AodSetting observed, AodSetting saved) {
        if (observed == null) return Action.NONE;
        if (pause) return observed.mode == OFF ? Action.NONE : Action.PAUSE;
        if (saved == null) return Action.NONE;
        return observed.mode == OFF ? Action.RESTORE : Action.FORGET;
    }

    AodSetting off() {
        return new AodSetting(OFF, start, end);
    }

    String encode() {
        return mode + ":" + start + ":" + end;
    }

    static AodSetting decode(String stored) {
        if (stored == null) return null;
        String[] fields = stored.split(":", -1);
        if (fields.length != 3) return null;
        try {
            AodSetting setting = of(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
            return setting == null || setting.mode == OFF ? null : setting;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    boolean matches(AodSetting other) {
        if (other == null || mode != other.mode) return false;
        // The firmware can omit inactive schedule fields in its reply.
        return mode != SCHEDULED || (start == other.start && end == other.end);
    }
}
