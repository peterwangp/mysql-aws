package com.hp.onecloud.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Util {
    public static String getResult(final ResultSet result) throws SQLException {
        int cols = result.getMetaData().getColumnCount();
        StringBuilder builder = new StringBuilder();
        while (result.next()) {
            for (int i = 1; i <= cols; i++) {
                builder.append(result.getString(i)).append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}