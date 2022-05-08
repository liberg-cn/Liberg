package cn.liberg.annotation;

import cn.liberg.core.TenThousands;

/**
 * DAO中列缓存相关的设置
 * cap()>0时，会在本列进行缓存
 * 指定group和groupCap>0，也会在组上进行缓存
 */
public @interface cache {
    /**
     * 缓存容量，默认为0——即该列上不进行数据缓存
     *
     * 若cap()为0，必须指定group()在group上进行缓存，否则没有意义
     */
    public int cap() default 0;

    /**
     * 如果多个列值的组合具有唯一性，可归为一组
     * 组名可以用"g1","g2",...
     */
    public String group() default "";

    /**
     * 如果多个列值的组合具有唯一性，可归为一组
     * 组内的缓存容量
     * 同组内多个列均指定了groupCap()，则取最大值
     */
    public int groupCap() default TenThousands.X1;

    /**
     * 同一组内，seq值更小的列，排在更前面
     * 若seq相同，则在实体类中先出现的列排在更前面
     */
    public int seq() default 0;
}
