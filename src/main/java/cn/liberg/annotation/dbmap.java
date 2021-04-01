package cn.liberg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于实体类成员变量的注解
 *
 * @author Liberg
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface dbmap {
	/**
	 * 列的存储长度，String类型有效
	 */
	public int length() default 0;
	/**
	 * 是否为字段创建索引
	 */
	public boolean isIndex() default false;

	/**
	 * 被注解的entity字段是否映射为数据表的列
	 */
	public boolean isMap() default true;
}
