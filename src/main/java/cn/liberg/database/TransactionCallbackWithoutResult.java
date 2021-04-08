package cn.liberg.database;

import cn.liberg.core.OperatorException;

@FunctionalInterface
public interface TransactionCallbackWithoutResult {
    public void execute() throws OperatorException;
}