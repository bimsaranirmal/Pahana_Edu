package Utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for managing Category entities in the Pahana Edu Online Billing System.
 * Provides methods for CRUD operations and searching categories in the database.
 *
 * @author BIMSARA
 */
public class CategoryDAO {

    /**
     * Retrieves a list of all categories from the database.
     *
     * @return A list of Category objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, description, created_at, updated_at FROM Category";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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

    /**
     * Retrieves a category by its ID.
     *
     * @param id The ID of the category to retrieve.
     * @return The Category object if found, otherwise null.
     * @throws SQLException If a database access error occurs.
     */
    public Category getCategoryById(int id) throws SQLException {
        String sql = "SELECT id, name, description, created_at, updated_at FROM Category WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
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

    /**
     * Adds a new category to the database.
     *
     * @param category The Category object containing the new category's details.
     * @return The generated ID of the new category, or -1 if addition fails.
     * @throws SQLException If a database access error occurs.
     */
    public int addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO Category (name, description, created_at, updated_at) VALUES (?, ?, NOW(), NOW())";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing category's details in the database.
     *
     * @param category The Category object with updated details.
     * @return True if the update was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean updateCategory(Category category) throws SQLException {
        String sql = "UPDATE Category SET name = ?, description = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a category from the database by its ID.
     *
     * @param id The ID of the category to delete.
     * @return True if the deletion was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean deleteCategory(int id) throws SQLException {
        String sql = "DELETE FROM Category WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Searches for categories based on provided criteria.
     *
     * @param criteria A Map where keys are column names (e.g., "name", "description") and values are search terms.
     * @return A list of Category objects matching the criteria.
     * @throws SQLException If a database access error occurs.
     */
    public List<Category> searchCategories(Map<String, String> criteria) throws SQLException {
        List<Category> categories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, name, description, created_at, updated_at FROM Category WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (criteria != null) {
            if (criteria.containsKey("name")) {
                sql.append(" AND name LIKE ?");
                params.add("%" + criteria.get("name") + "%");
            }
            if (criteria.containsKey("description")) {
                sql.append(" AND description LIKE ?");
                params.add("%" + criteria.get("description") + "%");
            }
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
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