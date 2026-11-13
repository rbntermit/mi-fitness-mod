package okhttp3;

public class Response {
    public final Request request() { throw new UnsupportedOperationException("stub"); }
    public final int code() { throw new UnsupportedOperationException("stub"); }
    public final ResponseBody peekBody(long byteCount) { throw new UnsupportedOperationException("stub"); }
    public final Builder newBuilder() { throw new UnsupportedOperationException("stub"); }

    public static class Builder {
        public Builder code(int code) { throw new UnsupportedOperationException("stub"); }
        public Builder message(String message) { throw new UnsupportedOperationException("stub"); }
        public Builder body(ResponseBody body) { throw new UnsupportedOperationException("stub"); }
        public Response build() { throw new UnsupportedOperationException("stub"); }
    }
}
