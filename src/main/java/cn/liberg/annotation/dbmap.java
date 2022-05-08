package cn.liberg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于实体类，或实体类成员变量的注解
 *
 * @author Liberg
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface dbmap {
	/**
	 * 该注解被用于实体类时，若isMap()为false，则该实体类不映射为数据库的表
	 * 同理，被用于实体类的成员时，若isMap()为false，改成员不映射到数据表的列
	 */
	public boolean isMap() default true;

	/**
	 * 列的存储长度，String类型有效
	 *   长度小于4096，映射为数据库VARCHAR列
	 *   长度达到/超过4096时，映射为数据库TEXT列
	 */
	public int length() default 0;
	/**
	 * 是否为字段创建索引
	 */
	public boolean isIndex() default false;

}
