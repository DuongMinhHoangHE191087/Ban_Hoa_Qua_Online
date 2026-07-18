<%@ page import="java.sql.*, dao.system.*, java.io.*, java.util.*, config.*" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    out.println("<h3>Testing Checkout DB Insertion...</h3>");
    try (Connection conn = ConnectionPool.getConnection()) {
        out.println("Connection OK<br>");
        
        // Let's do a dummy insert to orders table to see what fails
        String sql = "INSERT INTO orders (customer_id, owner_id, parent_order_id, order_type, delivery_address, "
                + "recipient_name, recipient_phone, delivery_time_slot, notes, cancelled_at, cancelled_by, "
                + "cancellation_reason, status, total_amount, delivery_fee, discount_amount, system_discount_amount, "
                + "shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, "
                + "shop_acceptance_deadline, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
                
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 1); // customer_id = 1
            ps.setInt(2, 2); // owner_id = 2
            ps.setNull(3, Types.INTEGER);
            ps.setString(4, "CHILD");
            ps.setString(5, "15 Pasteur, Quận 3, TP. Hồ Chí Minh");
            ps.setString(6, "Trần Minh");
            ps.setString(7, "0900000005");
            ps.setString(8, "Giao hỏa tốc");
            ps.setString(9, "Test notes");
            ps.setNull(10, Types.TIMESTAMP);
            ps.setNull(11, Types.INTEGER);
            ps.setNull(12, Types.VARCHAR);
            ps.setString(13, "PENDING_PAYMENT");
            ps.setBigDecimal(14, new java.math.BigDecimal("60000"));
            ps.setBigDecimal(15, new java.math.BigDecimal("15000"));
            ps.setBigDecimal(16, new java.math.BigDecimal("0"));
            ps.setBigDecimal(17, new java.math.BigDecimal("0"));
            ps.setBigDecimal(18, new java.math.BigDecimal("0"));
            ps.setBigDecimal(19, new java.math.BigDecimal("0"));
            ps.setBigDecimal(20, new java.math.BigDecimal("75000"));
            ps.setString(21, "COD");
            ps.setString(22, "NONE");
            ps.setNull(23, Types.TIMESTAMP);
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    out.println("Insert OK, Order ID = " + rs.getInt(1) + "<br>");
                    // Rollback using delete
                    conn.createStatement().executeUpdate("DELETE FROM orders WHERE order_id = " + rs.getInt(1));
                    out.println("Rollback OK<br>");
                }
            }
        } catch (Exception e) {
            out.println("<b>OrderDAO.save failed:</b> " + e.getMessage() + "<br>");
            e.printStackTrace(new PrintWriter(out));
        }
    } catch (Exception e) {
        out.println("<b>Connection failed:</b> " + e.getMessage() + "<br>");
        e.printStackTrace(new PrintWriter(out));
    }
%>
