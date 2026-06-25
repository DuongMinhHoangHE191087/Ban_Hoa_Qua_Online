import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import dao.system.ConnectionPool;

public class TestDB {
    public static void main(String[] args) {
        util.DotEnvLoader.load(".");
        try {
            System.out.println("Connecting to DB to check all webhook logs...");
            Connection conn = ConnectionPool.getConnection();
            
            // Check dedup logs
            String dedupSql = "SELECT * FROM sepay_webhook_dedup ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(dedupSql);
                 ResultSet rs = ps.executeQuery()) {
                System.out.println("ALL DEDUP LOGS:");
                while (rs.next()) {
                    System.out.printf("  TxID: %s | Ref: %s | Result: %s | Date: %s%n",
                        rs.getString("sepay_transaction_id"),
                        rs.getString("order_code"),
                        rs.getString("process_result"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
            
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
