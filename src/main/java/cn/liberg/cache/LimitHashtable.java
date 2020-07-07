package cn.liberg.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class LimitHashtable<K, V> {
	private int mMAX_CACHE_COUNT = 200000;
	private int mCLEAR_CACHE_COUNT = 20000;
	public HashMap<K, ObjectCache<K, V>> mCacheMap;
	public ArrayList<ObjectCache<K, V>> mCacheOrderList = null;
	private long mLastAccessTime = 0;
	private boolean mCacheClear = false;
	private byte[] mLock = new byte[0];

	public LimitHashtable(int limitCount) {
		mMAX_CACHE_COUNT = limitCount;
		mCLEAR_CACHE_COUNT = mMAX_CACHE_COUNT / 10;
		mCacheMap = new HashMap<K, ObjectCache<K, V>>();
		mCacheOrderList = new ArrayList<ObjectCache<K, V>>();
	}

	private long getAdjustAccessTimeStamp() {
		long current = (new Date()).getTime();
		if (current <= mLastAccessTime) {
			current = mLastAccessTime + 1;
			mLastAccessTime = current;
		} else {
			mLastAccessTime = current;
		}
		return current;
	}

	public void put(K key, V value) {
		ObjectCache<K, V> item = new ObjectCache<K, V>();
		item.mKey = key;
		item.mValue = value;
		synchronized (mLock) {
			if (mCacheMap.containsKey(key) == false) {
				item.mLastAccessTimeStampe = getAdjustAccessTimeStamp();
				mCacheMap.put(key, item);
				mCacheOrderList.add(item);
				if (mCacheOrderList.size() > mMAX_CACHE_COUNT
						|| mCacheClear == true) {
					int clearCount = 0;
					if (mCacheClear == false)
						mCacheClear = true;
					while (clearCount < 5) {
						clearCount++;
						item = mCacheOrderList.remove(0);
						key = item.mKey;
						mCacheMap.remove(key);
					}
					if (mMAX_CACHE_COUNT - mCacheOrderList.size() >= mCLEAR_CACHE_COUNT) {
						mCacheClear = false;
					}
				}
			} else {
				item=mCacheMap.get(key);
				if(item!=null){
					item.mValue=value;
				}
			}
		}
	}

	public V get(K key) {
		V result = null;		
		synchronized (mLock) {
			ObjectCache<K, V> item = (ObjectCache<K, V>) (mCacheMap.get(key));
			if (item != null) {
				result = item.mValue;
				int index = getCacheIndex(item);
				item.mLastAccessTimeStampe = getAdjustAccessTimeStamp();
				if (index >= 0) {
					mCacheOrderList.remove(index);
					mCacheOrderList.add(item);
				}
			}
		}

		return result;
	}

	private int getCacheIndex(ObjectCache<K, V> item) {
		int start = 0;
		int end = mCacheOrderList.size() - 1;
		int index = -1;
		if (end >= start) {
			index = getCacheIndex(item.mLastAccessTimeStampe, start, end);
		}
		return index;
	}

	private int getCacheIndex(long timeStamp, int start, int end) {
		int index = -1;
		int mid = 0;
		if (start == end) {
			index = start;
		} else {
			mid = (start + end) / 2;
			if (mid == start || mid == end) {
				ObjectCache<K, V> startItem = mCacheOrderList.get(start);
				ObjectCache<K, V> endItem = mCacheOrderList.get(end);
				if (timeStamp == startItem.mLastAccessTimeStampe) {
					index = start;
				}
				if (timeStamp == endItem.mLastAccessTimeStampe) {
					index = end;
				}

			} else {
				ObjectCache<K, V> item = mCacheOrderList.get(mid);
				if (timeStamp == item.mLastAccessTimeStampe) {
					index = mid;
				} else if (timeStamp > item.mLastAccessTimeStampe) {
					index = getCacheIndex(timeStamp, mid, end);
				} else if (timeStamp < item.mLastAccessTimeStampe) {
					index = getCacheIndex(timeStamp, start, mid);
				}
			}
		}
		return index;
	}

	public int getCacheCount() {
		int count;
		synchronized (mLock) {
			count = mCacheMap.size();
		}
		return count;
	}

	public int getCacheListCount() {
		int count;
		synchronized (mLock) {
			count = mCacheOrderList.size();
		}
		return count;
	}
	
	public void clear() {
		synchronized (mLock) {
			mCacheMap.clear();
			mCacheOrderList.clear();
		}	
	}
	
	public void remove(K key) {
		synchronized (mLock) {
			ObjectCache<K, V> item = (ObjectCache<K, V>) (mCacheMap.remove(key));
			if (item != null) {
				int index = getCacheIndex(item);
				if (index >= 0) {
					mCacheOrderList.remove(index);
				}
			}
		}
	}
	
}
