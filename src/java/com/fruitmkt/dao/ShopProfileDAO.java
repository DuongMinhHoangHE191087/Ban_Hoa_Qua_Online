package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ShopProfile;
import java.sql.*;
import java.util.*;

/**
 * ShopProfileDAO — DAO cho entity ShopProfile.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ShopProfileDAO extends BaseDAO {

    /**
     * TODO: Implement — findByUserId(int userId)
     */
    public List<ShopProfile> findByUserId(int userId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByUserId(int userId)");
    }

    /**
     * TODO: Implement — findAll(String approvalStatus)
     */
    public List<ShopProfile> findAll(String approvalStatus) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAll(String approvalStatus)");
    }

    /**
     * TODO: Implement — save(ShopProfile profile)
     */
    public int save(ShopProfile profile) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(ShopProfile profile)");
    }

    /**
     * TODO: Implement — update(ShopProfile profile)
     */
    public void update(ShopProfile profile) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: update(ShopProfile profile)");
    }

    /**
     * TODO: Implement — updateApprovalStatus(int profileId, String status, String rejectionReason)
     */
    public void updateApprovalStatus(int profileId, String status, String rejectionReason) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateApprovalStatus(int profileId, String status, String rejectionReason)");
    }

    /** Ánh xạ ResultSet -> ShopProfile — gọi trong mọi query SELECT */
    private ShopProfile mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
