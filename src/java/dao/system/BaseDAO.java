package dao.system;

import config.AppConfig;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * BaseDAO — lớp cha cho tất cả DAO trong hệ thống.
 *
 * TRÁCH NHIỆM:
 *   - Cung cấp getConnection() từ pool
 *   - withTransaction() — xử lý begin/commit/rollback tự động
 *   - runInTransaction() — phiên bản void của withTransaction()
 *
 * QUY TẮC DAO (BẮT BUỘC):
 *   1. Chỉ viết SQL trong lớp DAO — không ở Service hay Servlet
 *   2. Luôn dùng PreparedStatement — KHÔNG nối chuỗi SQL
 *   3. Luôn đóng Connection/PreparedStatement trong try-with-resources
 *   4. Ném SQLException lên Service — không catch im lặng ở DAO
 *   5. Tên method: findById, findAll, findBy*, save, update, delete
 *
 * CÁCH DÙNG getConnection() — đơn lẻ:
 * <pre>
 *   public Product findById(int id) throws SQLException {
 *       try (Connection conn = getConnection();
 *            PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE product_id = ?")) {
 *           ps.setInt(1, id);
 *           try (ResultSet rs = ps.executeQuery()) {
 *               return rs.next() ? mapRow(rs) : null;
 *           }
 *       }
 *   }
 * </pre>
 *
 * CÁCH DÙNG withTransaction() — nhiều thao tác cần atomic:
 * <pre>
 *   public Order placeOrder(Order order, List<OrderItem> items) throws SQLException {
 *       return withTransaction(conn -> {
 *           int orderId = insertOrder(conn, order);
 *           insertItems(conn, orderId, items);
 *           deductStock(conn, items);
 *           order.setOrderId(orderId);
 *           return order;
 *       });
 *   }
 * </pre>
 */
public abstract class BaseDAO {

    static {
        try {
            Class.forName(AppConfig.DB_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "Không tìm thấy JDBC driver: " + AppConfig.DB_DRIVER_CLASS);
        }
    }

    // =========================================================
    // Functional interfaces cho transaction helper
    // =========================================================

    /**
     * Công việc trong transaction trả về kết quả kiểu T.
     * Ném SQLException để trigger rollback tự động.
     */
    @FunctionalInterface
    public interface TransactionalWork<T> {
        T execute(Connection conn) throws SQLException;
    }

    /**
     * Công việc trong transaction không cần trả về giá trị.
     * Ném SQLException để trigger rollback tự động.
     */
    @FunctionalInterface
    public interface TransactionalAction {
        void execute(Connection conn) throws SQLException;
    }

    // =========================================================
    // Connection access
    // =========================================================

    /**
     * Lấy kết nối từ pool. Caller PHẢI đóng connection trong try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        return ConnectionPool.getConnection();
    }

    // =========================================================
    // Transaction helpers
    // =========================================================

    /**
     * Thực thi {@code work} trong một transaction và trả về kết quả.
     *
     * - Commit tự động nếu work.execute() thành công
     * - Rollback tự động nếu work.execute() ném SQLException
     * - Connection được đóng bằng try-with-resources
     *
     * @param work lambda/method reference thực thi SQL trong transaction
     * @return kết quả trả về bởi work.execute()
     * @throws SQLException khi DB lỗi hoặc work ném exception
     */
    protected <T> T withTransaction(TransactionalWork<T> work) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Phiên bản void của {@link #withTransaction(TransactionalWork)}.
     * Dùng khi transaction không cần trả về giá trị.
     *
     * @param action lambda thực thi SQL trong transaction
     * @throws SQLException khi DB lỗi hoặc action ném exception
     */
    protected void runInTransaction(TransactionalAction action) throws SQLException {
        withTransaction(conn -> {
            action.execute(conn);
            return null;
        });
    }
}
