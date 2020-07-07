package cn.liberg.database.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

interface IWhereMeta {
    default boolean isLeftBracket() {
        return false;
    }

    default boolean isRightBracket() {
        return false;
    }

    default boolean isCondition() {
        return false;
    }

    default boolean isOperation() {
        return false;
    }


    public String build();

}
