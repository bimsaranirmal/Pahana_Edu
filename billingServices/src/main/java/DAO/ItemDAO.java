package DAO;

import Model.Item;
import Utils.DatabaseUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for managing Item entities in the Pahana Edu Online Billing System.
 * Uses stored procedures for CRUD operations, searching items, and fetching stock range counts in the database.
 *
 * @author BIMSARA
 */
public class ItemDAO {

    public List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "{CALL sp_get_all_items()}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql);
             ResultSet rs = cstmt.executeQuery()) {
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

    public Item getItemById(int id) throws SQLException {
        String sql = "{CALL sp_get_item_by_id(?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            try (ResultSet rs = cstmt.executeQuery()) {
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

    public int addItem(Item item) throws SQLException {
        String sql = "{CALL sp_add_item(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, item.getName());
            cstmt.setString(2, item.getDescription());
            cstmt.setDouble(3, item.getPrice());
            cstmt.setInt(4, item.getStockQuantity());
            if (item.getCategoryId() != null) {
                cstmt.setInt(5, item.getCategoryId());
            } else {
                cstmt.setNull(5, Types.INTEGER);
            }
            cstmt.registerOutParameter(6, Types.INTEGER);
            cstmt.executeUpdate();
            return cstmt.getInt(6);
        }
    }

    public boolean updateItem(Item item) throws SQLException {
        String sql = "{CALL sp_update_item(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, item.getId());
            cstmt.setString(2, item.getName());
            cstmt.setString(3, item.getDescription());
            cstmt.setDouble(4, item.getPrice());
            cstmt.setInt(5, item.getStockQuantity());
            if (item.getCategoryId() != null) {
                cstmt.setInt(6, item.getCategoryId());
            } else {
                cstmt.setNull(6, Types.INTEGER);
            }
            cstmt.registerOutParameter(7, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(7);
        }
    }

    public boolean deleteItem(int id) throws SQLException {
        String sql = "{CALL sp_delete_item(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(2);
        }
    }

    public List<Item> searchItems(Map<String, String> criteria) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "{CALL sp_search_items(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, criteria != null ? criteria.get("name") : null);
            cstmt.setString(2, criteria != null ? criteria.get("description") : null);
            if (criteria != null && criteria.containsKey("category_id") && !criteria.get("category_id").isEmpty()) {
                cstmt.setInt(3, Integer.parseInt(criteria.get("category_id")));
            } else {
                cstmt.setNull(3, Types.INTEGER);
            }
            if (criteria != null && criteria.containsKey("stock_min") && !criteria.get("stock_min").isEmpty()) {
                cstmt.setInt(4, Integer.parseInt(criteria.get("stock_min")));
            } else {
                cstmt.setNull(4, Types.INTEGER);
            }
            if (criteria != null && criteria.containsKey("stock_max") && !criteria.get("stock_max").isEmpty()) {
                cstmt.setInt(5, Integer.parseInt(criteria.get("stock_max")));
            } else {
                cstmt.setNull(5, Types.INTEGER);
            }
            try (ResultSet rs = cstmt.executeQuery()) {
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

    public Map<String, Integer> getStockRangeCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "{CALL sp_get_stock_range_counts(?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.registerOutParameter(2, Types.INTEGER);
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.execute();
            counts.put("0_50", cstmt.getInt(1));
            counts.put("50_100", cstmt.getInt(2));
            counts.put("100_plus", cstmt.getInt(3));
        }
        return counts;
    }
}