/** Stock ZenUtils.postSyncZenMode command for bands without zen-rule capability. */
public final class LegacyZenRequest {
    private LegacyZenRequest() {}

    public static t3u create(boolean phoneOn) {
        t3u request = new t3u();
        request.c = 2;
        request.d = 23;
        xir mode = new xir();
        mode.a = phoneOn ? 0 : 2;
        request.U(new sir().s0(mode));
        return request;
    }
}
