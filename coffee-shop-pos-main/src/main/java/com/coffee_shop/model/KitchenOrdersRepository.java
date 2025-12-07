package com.coffee_shop.model;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KitchenOrdersRepository {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    public KitchenOrdersRepository() {
        Properties props = new Properties();
        String url = "jdbc:mysql://localhost:3306/restaurant?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "";

        try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
                url = props.getProperty("jdbc.url", url);
                user = props.getProperty("jdbc.user", user);
                pass = props.getProperty("jdbc.password", pass);
            }
        } catch (Exception e) {
            System.err.println("Could not load db.properties, using defaults: " + e.getMessage());
        }

        this.jdbcUrl = url;
        this.dbUser = user;
        this.dbPassword = pass;

        try {
            initDatabase();
        } catch (SQLException e) {
            System.err.println("Error initializing kitchen_orders table: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    private void initDatabase() throws SQLException {
        String create = "CREATE TABLE IF NOT EXISTS kitchen_orders ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "bill_id VARCHAR(64) NOT NULL,"
                + "food_id INT NOT NULL,"
                + "name VARCHAR(255) NOT NULL,"
                + "qty INT NOT NULL,"
            + "table_number VARCHAR(20) DEFAULT NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING',"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX (bill_id), INDEX (status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute(create);
            // ensure new columns exist for backward compatibility
            com.coffee_shop.model.DbMigration.ensureColumnExists(conn, "kitchen_orders", "table_number", "VARCHAR(20) DEFAULT NULL");
        }
    }

    /**
     * Insert kitchen order rows for a single bill.
     * Each item should be inserted with status = 'PENDING'.
     */
    public void insertKitchenOrder(String billId, int foodId, String name, int qty, String tableNumber) throws SQLException {
        String sql = "INSERT INTO kitchen_orders (bill_id, food_id, name, qty, table_number, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId);
            ps.setInt(2, foodId);
            ps.setString(3, name);
            ps.setInt(4, qty);
            ps.setString(5, tableNumber);
            ps.executeUpdate();
        }
    }

    public static class OrderItem {
        public final int foodId;
        public final String name;
        public final int qty;

        public OrderItem(int foodId, String name, int qty) {
            this.foodId = foodId;
            this.name = name;
            this.qty = qty;
        }
    }

    /**
     * Batch insert of kitchen orders in a single transaction.
     */
    public void insertKitchenOrders(String billId, java.util.List<OrderItem> items, String tableNumber) throws SQLException {
        String sql = "INSERT INTO kitchen_orders (bill_id, food_id, name, qty, table_number, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try {
                conn.setAutoCommit(false);
                for (OrderItem it : items) {
                    ps.setString(1, billId);
                    ps.setInt(2, it.foodId);
                    ps.setString(3, it.name);
                    ps.setInt(4, it.qty);
                    ps.setString(5, tableNumber);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Insert using caller's Connection (transactional) — does not commit or rollback the connection.
     */
    public void insertKitchenOrders(Connection conn, String billId, java.util.List<OrderItem> items, String tableNumber) throws SQLException {
        String sql = "INSERT INTO kitchen_orders (bill_id, food_id, name, qty, table_number, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItem it : items) {
                ps.setString(1, billId);
                ps.setInt(2, it.foodId);
                ps.setString(3, it.name);
                ps.setInt(4, it.qty);
                ps.setString(5, tableNumber);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<KitchenOrder> getPendingOrders() throws SQLException {
        String sql = "SELECT id, bill_id, food_id, name, qty, table_number, status, created_at FROM kitchen_orders WHERE status = 'PENDING' ORDER BY created_at ASC";
        List<KitchenOrder> out = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String billId = rs.getString("bill_id");
                int foodId = rs.getInt("food_id");
                String name = rs.getString("name");
                int qty = rs.getInt("qty");
                String status = rs.getString("status");
                String tableNumber = rs.getString("table_number");
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                out.add(new KitchenOrder(id, billId, foodId, name, qty, tableNumber, status, createdAt));
            }
        }
        return out;
    }

    public void markOrderCompleted(int orderId) throws SQLException {
        String sql = "UPDATE kitchen_orders SET status = 'COMPLETED' WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    public List<KitchenOrder> getOrdersByStatus(String status) throws SQLException {
        String sql = "SELECT id, bill_id, food_id, name, qty, table_number, status, created_at FROM kitchen_orders WHERE status = ? ORDER BY created_at ASC";
        List<KitchenOrder> out = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String billId = rs.getString("bill_id");
                    int foodId = rs.getInt("food_id");
                    String name = rs.getString("name");
                    int qty = rs.getInt("qty");
                    String st = rs.getString("status");
                    String tableNumber = rs.getString("table_number");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    out.add(new KitchenOrder(id, billId, foodId, name, qty, tableNumber, st, createdAt));
                }
            }
        }
        return out;
    }

    public List<KitchenOrder> getAllOrders() throws SQLException {
        String sql = "SELECT id, bill_id, food_id, name, qty, table_number, status, created_at FROM kitchen_orders ORDER BY created_at ASC";
        List<KitchenOrder> out = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String billId = rs.getString("bill_id");
                int foodId = rs.getInt("food_id");
                String name = rs.getString("name");
                int qty = rs.getInt("qty");
                String st = rs.getString("status");
                String tableNumber = rs.getString("table_number");
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                out.add(new KitchenOrder(id, billId, foodId, name, qty, tableNumber, st, createdAt));
            }
        }
        return out;
    }
}
