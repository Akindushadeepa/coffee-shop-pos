package com.coffee_shop.model;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class UsersRepository {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    public UsersRepository() {
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
            System.err.println("Error initializing users table: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    private void initDatabase() throws SQLException {
        String create = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(100) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL,"
                + "role VARCHAR(20) NOT NULL"
                + ")";

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute(create);
        }
    }

    public void addUser(Users user) throws SQLException {
        String insert = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
        }
    }

    public Users authenticate(String username, String password) throws SQLException {
        String sql = "SELECT username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        return new Admin(username, password);
                    } else if ("CASHIER".equalsIgnoreCase(role)) {
                        return new Cashier(username, password);
                    } else if ("KITCHEN".equalsIgnoreCase(role)) {
                        return new Kitchen(username, password);
                    } else {
                        return new Users(username, password) {
                            @Override
                            public String getRole() {
                                return role;
                            }
                        };
                    }
                }
            }
        }
        return null;
    }

    public boolean hasAnyUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void deleteAllUsers() throws SQLException {
        String sql = "DELETE FROM users";
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    public java.util.List<Users> getAllUsers() throws SQLException {
        java.util.List<Users> list = new java.util.ArrayList<>();
        String sql = "SELECT username, password, role FROM users ORDER BY id";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String role = rs.getString("role");
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        list.add(new Admin(username, password));
                    } else if ("CASHIER".equalsIgnoreCase(role)) {
                        list.add(new Cashier(username, password));
                    } else if ("KITCHEN".equalsIgnoreCase(role)) {
                        list.add(new Kitchen(username, password));
                    } else {
                        list.add(new Users(username, password) {
                            @Override
                            public String getRole() {
                                return role;
                            }
                        });
                    }
                }
        }
        return list;
    }

    public void replaceUsers(java.util.List<Users> newUsers) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("DELETE FROM users");
                }
                String insert = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    for (Users u : newUsers) {
                        ps.setString(1, u.getUsername());
                        ps.setString(2, u.getPassword());
                        ps.setString(3, u.getRole());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
