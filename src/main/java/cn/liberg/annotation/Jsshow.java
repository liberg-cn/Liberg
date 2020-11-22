package cn.liberg.annotation;

/**
 * 自动生成前端代码
 *
 * @author Liberg
 */
public @interface Jsshow {
    public JsshowType type() default JsshowType.input;
    public String args() default "";
}
