package Utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for managing Item entities in the Pahana Edu Online Billing System.
 * Provides methods for CRUD operations, searching items, and fetching stock range counts in the database.
 *
 * @author BIMSARA
 */
public class ItemDAO {

    /**
     * Retrieves a list of all items from the database.
     *
     * @return A list of Item objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT id, name, description, price, stock_quantity, category_id, created_at, updated_at FROM Item";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity"),
                    rs.getObject("category_id", Integer.class),
                    rs.getDate("created_at").toLocalDate(),
                    rs.getDate("updated_at").toLocalDate()
                ));
            }
        }
        return items;
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id The ID of the item to retrieve.
     * @return The Item object if found, otherwise null.
     * @throws SQLException If a database access error occurs.
     */
    public Item getItemById(int id) throws SQLException {
        String sql = "SELECT id, name, description, price, stock_quantity, category_id, created_at, updated_at FROM Item WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getObject("category_id", Integer.class),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    /**
     * Adds a new item to the database.
     *
     * @param item The Item object containing the new item's details.
     * @return The generated ID of the new item, or -1 if addition fails.
     * @throws SQLException If a database access error occurs.
     */
    public int addItem(Item item) throws SQLException {
        String sql = "INSERT INTO Item (name, description, price, stock_quantity, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setInt(4, item.getStockQuantity());
            if (item.getCategoryId() != null) {
                pstmt.setInt(5, item.getCategoryId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
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
     * Updates an existing item's details in the database.
     *
     * @param item The Item object with updated details.
     * @return True if the update was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean updateItem(Item item) throws SQLException {
        String sql = "UPDATE Item SET name = ?, description = ?, price = ?, stock_quantity = ?, category_id = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setInt(4, item.getStockQuantity());
            if (item.getCategoryId() != null) {
                pstmt.setInt(5, item.getCategoryId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            pstmt.setInt(6, item.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an item from the database by its ID.
     *
     * @param id The ID of the item to delete.
     * @return True if the deletion was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean deleteItem(int id) throws SQLException {
        String sql = "DELETE FROM Item WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Searches for items based on provided criteria.
     *
     * @param criteria A Map where keys are column names (e.g., "name", "description", "category_id", "stock_min", "stock_max") and values are search terms.
     * @return A list of Item objects matching the criteria.
     * @throws SQLException If a database access error occurs.
     */
    public List<Item> searchItems(Map<String, String> criteria) throws SQLException {
        List<Item> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, name, description, price, stock_quantity, category_id, created_at, updated_at FROM Item WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (criteria != null) {
            if (criteria.containsKey("name")) {
                sql.append(" AND name LIKE ?");
                params.add("%" + criteria.get("name") + "%");
            }
            if (criteria.containsKey("description")) {
                sql.append(" AND description LIKE ?");
                params.add("%" + criteria.get("description") + "%");
            }
            if (criteria.containsKey("category_id")) {
                sql.append(" AND category_id = ?");
                params.add(Integer.parseInt(criteria.get("category_id")));
            }
            if (criteria.containsKey("stock_min")) {
                sql.append(" AND stock_quantity >= ?");
                params.add(Integer.parseInt(criteria.get("stock_min")));
            }
            if (criteria.containsKey("stock_max")) {
                sql.append(" AND stock_quantity <= ?");
                params.add(Integer.parseInt(criteria.get("stock_max")));
            }
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new Item(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getObject("category_id", Integer.class),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    ));
                }
            }
        }
        return items;
    }

    /**
     * Retrieves counts of items in specified stock ranges (0-50, 50-100, 100+).
     *
     * @return A Map with keys "0_50", "50_100", and "100_plus" containing the count of items in each range.
     * @throws SQLException If a database access error occurs.
     */
    public Map<String, Integer> getStockRangeCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM Item WHERE stock_quantity BETWEEN 0 AND 50) AS count_0_50, " +
                     "(SELECT COUNT(*) FROM Item WHERE stock_quantity BETWEEN 51 AND 100) AS count_50_100, " +
                     "(SELECT COUNT(*) FROM Item WHERE stock_quantity > 100) AS count_100_plus";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                counts.put("0_50", rs.getInt("count_0_50"));
                counts.put("50_100", rs.getInt("count_50_100"));
                counts.put("100_plus", rs.getInt("count_100_plus"));
            }
        }
        return counts;
    }
}