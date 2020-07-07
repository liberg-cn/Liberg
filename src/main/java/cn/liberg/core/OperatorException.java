package cn.liberg.core;

public class OperatorException extends Exception {
    private IStatusCode sc = StatusCode.ERROR_SERVER;

    public OperatorException(IStatusCode sc) {
        super();
        this.sc = sc;
    }

    public OperatorException(IStatusCode sc, Throwable throwable) {
        super(throwable);
        this.sc = sc;
    }

    public OperatorException(Throwable throwable) {
        super(throwable);
    }

    public IStatusCode statusCode() {
        return sc;
    }

    public int code() {
        return sc.code();
    }

    public String desc() {
        return sc.desc();
    }
}
