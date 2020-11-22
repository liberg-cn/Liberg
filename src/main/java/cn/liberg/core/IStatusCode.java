package cn.liberg.core;

/**
 * 错误状态码接口
 *
 * @author Liberg
 * @see OperatorException
 */
public interface IStatusCode {

    /**
     * @return 状态码
     */
    public int code();

    /**
     * @return 状态描述
     */
    public String desc();
}
