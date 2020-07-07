package cn.liberg.database.query;

public class Operation implements IWhereMeta {
    private final String op;

    public Operation(String op) {
        this.op = op;
    }

    @Override
    public boolean isOperation() {
        return true;
    }

    @Override
    public String build() {
        return op;
    }
}
