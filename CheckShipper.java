import java.sql.*;

public class CheckShipper {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping;encrypt=false";
        String user = "sa";
        String pass = "123";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT email, password_hash FROM users WHERE role = 'DELIVERY'");
            while (rs.next()) {
                System.out.println("Shipper Account: " + rs.getString(1) + " | PassHash: " + rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
