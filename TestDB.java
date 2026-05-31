import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping;encrypt=false";
        String user = "sa";
        String pass = "123";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shop_owner_profiles");
            if (rs.next()) {
                System.out.println("Shop Count: " + rs.getInt(1));
            }
            
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM reviews");
            if (rs2.next()) {
                System.out.println("Review Count: " + rs2.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
