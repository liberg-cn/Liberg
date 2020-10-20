package cn.liberg.core;

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

    //发生了某些情况，需要给前端一个desc的内容提示
    public static IStatusCode def(String desc) {
        return def(999, desc);
    }

    private String desc;
    private int code;
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
        System.out.println("reset: "+ this.code+"(" + this.desc + ")");
        System.out.println("   to: "+ code + "(" + desc + ")");
        this.code = code;
        this.desc = desc;
    }

    public void reset(String desc) {
        System.out.println("reset: "+ this.code+"(" + this.desc + ")");
        System.out.println("   to: "+ this.code + "(" + desc + ")");
        this.desc = desc;
    }
}
