package cn.liberg.core;

public interface ICacheEntity<E> {
    public void put(E entity);
    public void remove(E entity);
    public void clear();
}
