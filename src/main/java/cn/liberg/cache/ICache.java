package cn.liberg.cache;

/**
 * 抽象缓存接口
 * @param <K>
 * @param <V>
 */
public interface ICache<K, V> {
	/**
	 * 以字符串key读取缓存对象
	 * @param key
	 * @return
	 */
	public V get(K key);

	/**
	 * 通过字符串key将对象写入缓存
	 * @param key
	 * @param obj
	 */
	public void put(K key, V obj);

	/**
	 * 通过字符串key移除缓存对象
	 * @param key
	 */
	public void remove(K key);

	/**
	 * 清空所有缓存对象
	 */
	public void clear();

	/**
	 * 缓存对象的总个数
	 * @return
	 */
	public int size();

	/**
	 * 缓存的最大容量
	 * @return
	 */
	public int capacity();
}
