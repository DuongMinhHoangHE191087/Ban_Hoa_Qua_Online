package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Category;
import java.sql.*;
import java.util.*;

/**
 * CategoryDAO — DAO cho entity Category.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class CategoryDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<Category> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findAll()
     */
    public List<Category> findAll() throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAll()");
    }

    /**
     * TODO: Implement — findAllActive()
     */
    public List<Category> findAllActive() throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAllActive()");
    }

    /**
     * TODO: Implement — save(Category category)
     */
    public int save(Category category) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Category category)");
    }

    /**
     * TODO: Implement — update(Category category)
     */
    public void update(Category category) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: update(Category category)");
    }

    /** Ánh xạ ResultSet -> Category — gọi trong mọi query SELECT */
    private Category mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
