package DAO;

import Model.Cashier;
import Utils.DatabaseUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for managing Cashier entities in the Pahana Edu Online Billing System.
 * Uses stored procedures for all database operations.
 *
 * @author BIMSARA
 */
public class CashierDAO {

    public List<Cashier> getAllCashiers() throws SQLException {
        List<Cashier> cashiers = new ArrayList<>();
        String sql = "{CALL sp_get_all_cashiers()}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql);
             ResultSet rs = cstmt.executeQuery()) {
            while (rs.next()) {
                cashiers.add(new Cashier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getDate("dob").toLocalDate(),
                    rs.getString("address"),
                    rs.getString("nic"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("status"),
                    rs.getDate("created_at").toLocalDate(),
                    rs.getDate("updated_at").toLocalDate()
                ));
            }
        }
        return cashiers;
    }

    public Cashier getCashierById(int id) throws SQLException {
        String sql = "{CALL sp_get_cashier_by_id(?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return new Cashier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getDate("dob").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("nic"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    public int registerCashier(Cashier cashier) throws SQLException {
        String sql = "{CALL sp_register_cashier(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, cashier.getName());
            cstmt.setString(2, cashier.getGender());
            cstmt.setDate(3, Date.valueOf(cashier.getDob()));
            cstmt.setString(4, cashier.getAddress());
            cstmt.setString(5, cashier.getNic());
            cstmt.setString(6, cashier.getEmail());
            cstmt.setString(7, cashier.getPhone());
            cstmt.registerOutParameter(8, Types.INTEGER);
            cstmt.executeUpdate();
            return cstmt.getInt(8);
        }
    }

    public boolean updateCashier(Cashier cashier) throws SQLException {
        String sql = "{CALL sp_update_cashier(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, cashier.getId());
            cstmt.setString(2, cashier.getName());
            cstmt.setString(3, cashier.getGender());
            cstmt.setDate(4, Date.valueOf(cashier.getDob()));
            cstmt.setString(5, cashier.getAddress());
            cstmt.setString(6, cashier.getNic());
            cstmt.setString(7, cashier.getEmail());
            cstmt.setString(8, cashier.getPhone());
            cstmt.registerOutParameter(9, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(9);
        }
    }

    public boolean deleteCashier(int id) throws SQLException {
        String sql = "{CALL sp_delete_cashier(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(2);
        }
    }

    public boolean approveCashier(int id) throws SQLException {
        String sql = "{CALL sp_approve_cashier(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(2);
        }
    }

    public boolean rejectCashier(int id) throws SQLException {
        String sql = "{CALL sp_reject_cashier(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(2);
        }
    }

    public List<Cashier> searchCashiers(Map<String, String> criteria) throws SQLException {
        List<Cashier> cashiers = new ArrayList<>();
        String sql = "{CALL sp_search_cashiers(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, criteria != null ? criteria.get("name") : null);
            cstmt.setString(2, criteria != null ? criteria.get("gender") : null);
            cstmt.setString(3, criteria != null ? criteria.get("nic") : null);
            cstmt.setString(4, criteria != null ? criteria.get("email") : null);
            cstmt.setString(5, criteria != null ? criteria.get("phone") : null);
            cstmt.setString(6, criteria != null ? criteria.get("status") : null);
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    cashiers.add(new Cashier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getDate("dob").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("nic"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    ));
                }
            }
        }
        return cashiers;
    }

    public Cashier authenticateCashier(String username, String email) throws SQLException {
        String sql = "{CALL sp_authenticate_cashier(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, username);
            cstmt.setString(2, email);
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return new Cashier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getDate("dob").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("nic"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    );
                }
            }
        }
        return null;
    }
}