import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping;encrypt=false";
        String user = "sa";
        String pass = "123";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("ALTER TABLE deliveries ADD proof_image_url NVARCHAR(500) NULL");
            stmt.executeUpdate("ALTER TABLE deliveries ADD estimated_delivery_time DATETIME NULL");
            System.out.println("ALTER TABLE success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
