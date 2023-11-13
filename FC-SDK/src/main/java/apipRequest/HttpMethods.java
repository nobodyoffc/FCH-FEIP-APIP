package apipRequest;

public enum HttpMethods {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    PATCH("PATCH");

    HttpMethods(String str) {
        this.str = str;
    }

    private final String str;

    public String getStr() {
        return str;
    }
}
