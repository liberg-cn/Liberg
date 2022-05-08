package cn.liberg.database;

import cn.liberg.core.OperatorException;

@FunctionalInterface
public interface TransactionCallWithResult<R> {
    public R execute() throws OperatorException;
}
