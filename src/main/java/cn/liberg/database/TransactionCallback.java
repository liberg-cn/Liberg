package cn.liberg.database;

import cn.liberg.core.OperatorException;

@FunctionalInterface
public interface TransactionCallback<R> {
    public R execute() throws OperatorException;
}
