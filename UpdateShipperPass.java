import com.fruitmkt.util.HashUtil;
import java.sql.*;

public class UpdateShipperPass {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping;encrypt=false";
        String user = "sa";
        String pass = "123";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String newHash = HashUtil.hashPassword("123456");
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE role = 'DELIVERY'");
            stmt.setString(1, newHash);
            int rows = stmt.executeUpdate();
            System.out.println("Updated " + rows + " shipper accounts to password '123456'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
