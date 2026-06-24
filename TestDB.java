import java.sql.Connection;
import dao.system.ConnectionPool;

public class TestDB {
    public static void main(String[] args) {
        try {
            System.out.println("Testing DB connection...");
            Connection conn = ConnectionPool.getConnection();
            System.out.println("Connection OK: " + (conn != null));
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
