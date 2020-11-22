package cn.liberg.cache;

/**
 * 抽象缓存接口
 * @param <T>
 *
 * @author Liberg
 */
public interface ICache<T> {
	/**
	 * 以字符串key读取缓存对象
	 * @param key
	 * @return
	 */
	public T get(String key);

	/**
	 * 通过字符串key将对象写入缓存
	 * @param key
	 * @param obj
	 */
	public void put(String key, T obj);

	/**
	 * 通过字符串key移除缓存对象
	 * @param key
	 */
	public void remove(String key);

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
