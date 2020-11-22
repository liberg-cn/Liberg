package cn.liberg.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 该类定义内部状态响应码（1000以内）
 *
 * 业务系统在定义自己的错误码时，应该至少从1001开始。
 */
public enum StatusCode implements IStatusCode {
    OK(200, ""),
    ERROR_UNKNOW(0,"Unknown error"),
    ERROR_SERVER(501, "服务端出错"),
    ERROR_DB(502, "数据库出错"),
    ERROR_NET(503, "网络错误"),
    ERROR_JSON(504, "JSON解析错误"),
    OPERATION_ILLEGAL(505, "非法操作"),
    OPERATION_FAILED(506, "操作失败"),
    PARAMS_INVALID(507, "参数错误");


    public static IStatusCode def(int code, String desc) {
        return new IStatusCode() {
            @Override
            public int code() {
                return code;
            }
            @Override
            public String desc() {
                return desc;
            }
        };
    }

    /**
     * 发生了某些情况，临时给客户端一个desc的内容提示。
     * 采用特殊响应状态码999
     */
    public static IStatusCode def(String desc) {
        return def(999, desc);
    }

    private int code;
    private String desc;
    StatusCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    @Override
    public int code() {
        return code;
    }

    @Override
    public String desc() {
        return desc;
    }

    public void reset(int code, String desc) {
        Logger logger = LoggerFactory.getLogger(StatusCode.class);
        if (logger.isDebugEnabled()) {
            logger.debug("reset: {}({})", this.code, this.desc);
            logger.debug("   to: {}({})", code, desc);
        }
        this.code = code;
        this.desc = desc;
    }

    public void reset(String desc) {
        Logger logger = LoggerFactory.getLogger(StatusCode.class);
        if (logger.isDebugEnabled()) {
            logger.debug("reset: {}({})", code, this.desc);
            logger.debug("   to: {}({})", code, desc);
        }
        this.desc = desc;
    }
}
