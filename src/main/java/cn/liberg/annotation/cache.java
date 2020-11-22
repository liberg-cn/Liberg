package cn.liberg.annotation;


import cn.liberg.cache.CacheType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Liberg
 *
 * TODO DAO层的缓存
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface cache {
    CacheType type();

    /**
     * 一到多个(多个用;号隔开)key-value对，key和value是Entity实体的字段名称，每一对key-value可以是下面三种模式之一：
     * id=$;    {id}字段到实体对象本身的映射
     * syncId,id,code=value;    在同一个Map中分别创建sy_{syncId},id_{id},co_{code}到mValue字段的映射
     * syncId+id=$;    {syncId}_{id}作为组合key映射到实体对象
     */
    String rules();
}
