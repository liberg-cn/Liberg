package cn.liberg.annotation;

public @interface Jsshow {
    public JsshowType type() default JsshowType.input;
    public String args() default "";
}
