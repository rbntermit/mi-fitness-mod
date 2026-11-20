/** Stock mailbox requests from AODSettingViewModel. */
final class AodRequest {
    static final int READ_SUBTYPE = 0x41;

    private AodRequest() {}

    static t3u read() {
        t3u packet = new t3u();
        packet.c = 2;
        packet.d = READ_SUBTYPE;
        return packet;
    }

    static t3u write(AodSetting setting) {
        t3u packet = read();
        packet.d = 0x42;
        xir timing = new xir();
        timing.a = setting.mode;
        timing.b = time(setting.start);
        timing.c = time(setting.end);
        return packet.U(new sir().s0(timing));
    }

    static AodSetting decode(xir timing) {
        return timing == null ? null : AodSetting.of(timing.a, minutes(timing.b), minutes(timing.c));
    }

    private static int minutes(vt4 time) {
        if (time == null) return AodSetting.NO_TIME;
        if (time.a < 0 || time.a > 23 || time.b < 0 || time.b > 59) return -2;
        return time.a * 60 + time.b;
    }

    private static vt4 time(int minutes) {
        if (minutes == AodSetting.NO_TIME) return null;
        vt4 time = new vt4();
        time.a = minutes / 60;
        time.b = minutes % 60;
        return time;
    }
}
