package cn.liberg.core;

/**
 * 目前支持的Column类型
 *
 * @author Liberg
 */
public enum ColumnType {
    UNKNOWN(0),
    Byte(1),
    Integer(4),
    Long(8),
    String(16);

    int value;

    private ColumnType(int value) {
        this.value = value;
    }
}
