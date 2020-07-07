package cn.liberg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface dbmap {
	//数据库表列的存储长度，String类型有效
	public int length() default 0;
	//是否为字段创建索引
	public boolean isIndex() default false;
	//是否映射为表字段
	public boolean isMap() default true;
}
