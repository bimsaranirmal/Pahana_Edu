package DAO;

import Model.Category;
import Utils.DatabaseUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for managing Category entities in the Pahana Edu Online Billing System.
 * Uses stored procedures for CRUD operations and searching categories in the database.
 *
 * @author BIMSARA
 */
public class CategoryDAO {

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "{CALL sp_get_all_categories()}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql);
             ResultSet rs = cstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("created_at").toLocalDate(),
                    rs.getDate("updated_at").toLocalDate()
                ));
            }
        }
        return categories;
    }

    public Category getCategoryById(int id) throws SQLException {
        String sql = "{CALL sp_get_category_by_id(?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    public int addCategory(Category category) throws SQLException {
        String sql = "{CALL sp_add_category(?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, category.getName());
            cstmt.setString(2, category.getDescription());
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.executeUpdate();
            return cstmt.getInt(3);
        }
    }

    public boolean updateCategory(Category category) throws SQLException {
        String sql = "{CALL sp_update_category(?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, category.getId());
            cstmt.setString(2, category.getName());
            cstmt.setString(3, category.getDescription());
            cstmt.registerOutParameter(4, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(4);
        }
    }

    public boolean deleteCategory(int id) throws SQLException {
        String sql = "{CALL sp_delete_category(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(2);
        }
    }

    public List<Category> searchCategories(Map<String, String> criteria) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "{CALL sp_search_categories(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            String name = criteria != null ? criteria.get("name") : null;
            String description = criteria != null ? criteria.get("description") : null;
            cstmt.setString(1, name);
            cstmt.setString(2, description);
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    ));
                }
            }
        }
        return categories;
    }
}