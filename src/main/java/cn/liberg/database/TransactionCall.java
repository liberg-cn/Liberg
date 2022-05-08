package cn.liberg.database;

import cn.liberg.core.OperatorException;

@FunctionalInterface
public interface TransactionCall {
    public void execute() throws OperatorException;
}