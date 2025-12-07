package com.coffee_shop.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbMigration {
    public static boolean columnExists(Connection conn, String tableName, String columnName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    public static void ensureColumnExists(Connection conn, String tableName, String columnName, String columnTypeDef) {
        try {
            if (!columnExists(conn, tableName, columnName)) {
                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnTypeDef);
                try (Statement st = conn.createStatement()) {
                    st.execute(sql);
                    System.out.println("Applied migration: " + sql);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not ensure column " + columnName + " on " + tableName + ": " + e.getMessage());
        }
    }
}
