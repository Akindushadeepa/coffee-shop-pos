//package resturant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class tst {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/restaurant"; // ✅ Correct spelling and syntax
        String username = "root";
        String password = "";

        String sql = "INSERT INTO food (id, fName, fPrice) VALUES (?, ?, ?)";

        // ✅ You must include DriverManager import & load the driver if necessary
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, 2);
            pstmt.setString(2, "rools"); // fixed spelling from "cacke"
            pstmt.setInt(3, 100);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows inserted: " + rowsAffected);
        }
    }
}
