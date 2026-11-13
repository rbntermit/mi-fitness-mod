package okhttp3;

public abstract class ResponseBody {
    public abstract MediaType contentType();
    public final String string() { throw new UnsupportedOperationException("stub"); }
    public static final ResponseBody create(MediaType contentType, String content) {
        throw new UnsupportedOperationException("stub");
    }
}
