package cn.liberg.cache;

public enum CacheType {
    dump, //对数据库表中的数据全量进行缓存，适用于数据总条数在预期受限范围内，总数据量不会很大的情况
    dump_lazy,//需要时加载到内存缓存，数量无上限，最终达到全量缓存
    lru_table, //对最近访问的热数据进行缓存，缓存总条数可设置（比如设为20万），采用LRU淘汰算法清除超过容量限制的记录
    //对最近访问过的热数据进行缓存，缓存总条数可设置（比如设为20万），先加入哈希表的数据先淘汰
    liberg_table,
    redis //缓存到redis，需要redis客户端支持，适用于小数据、分布式场景中的sessionId、分布式令牌等
}


