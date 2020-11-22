package cn.liberg.core;

/**
 * 通用的异常类型，除了cause为一个Throwable外，
 * 还包含一个错误状态码{@link IStatusCode}
 *
 * @author Liberg
 * @see IStatusCode
 */
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

    public OperatorException(IStatusCode sc, String message) {
        super(message);
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
