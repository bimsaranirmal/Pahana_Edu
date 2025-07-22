/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 *
 * @author BIMSARA
 */
public class CashierDAO {

    public List<Cashier> getAllCashiers() throws SQLException {
        List<Cashier> cashiers = new ArrayList<>();
        String sql = "SELECT * FROM cashier";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
        String sql = "SELECT * FROM cashier WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
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
        String sql = "INSERT INTO cashier (name, gender, dob, address, nic, email, phone, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, cashier.getName());
            pstmt.setString(2, cashier.getGender());
            pstmt.setDate(3, Date.valueOf(cashier.getDob()));
            pstmt.setString(4, cashier.getAddress());
            pstmt.setString(5, cashier.getNic());
            pstmt.setString(6, cashier.getEmail());
            pstmt.setString(7, cashier.getPhone());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean updateCashier(Cashier cashier) throws SQLException {
        String sql = "UPDATE cashier SET name = ?, gender = ?, dob = ?, address = ?, nic = ?, email = ?, phone = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cashier.getName());
            pstmt.setString(2, cashier.getGender());
            pstmt.setDate(3, Date.valueOf(cashier.getDob()));
            pstmt.setString(4, cashier.getAddress());
            pstmt.setString(5, cashier.getNic());
            pstmt.setString(6, cashier.getEmail());
            pstmt.setString(7, cashier.getPhone());
            pstmt.setInt(8, cashier.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCashier(int id) throws SQLException {
        String sql = "DELETE FROM cashier WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean approveCashier(int id) throws SQLException {
        return updateStatus(id, "approved");
    }

    public boolean rejectCashier(int id) throws SQLException {
        return updateStatus(id, "rejected");
    }

    private boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE cashier SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Cashier> searchCashiers(Map<String, String> criteria) throws SQLException {
        List<Cashier> cashiers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM cashier WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (criteria != null) {
            if (criteria.containsKey("name")) {
                sql.append(" AND name LIKE ?");
                params.add("%" + criteria.get("name") + "%");
            }
            if (criteria.containsKey("gender")) {
                sql.append(" AND gender = ?");
                params.add(criteria.get("gender"));
            }
            if (criteria.containsKey("nic")) {
                sql.append(" AND nic = ?");
                params.add(criteria.get("nic"));
            }
            if (criteria.containsKey("email")) {
                sql.append(" AND email = ?");
                params.add(criteria.get("email"));
            }
            if (criteria.containsKey("phone")) {
                sql.append(" AND phone = ?");
                params.add(criteria.get("phone"));
            }
            if (criteria.containsKey("status")) {
                sql.append(" AND status = ?");
                params.add(criteria.get("status"));
            }
        }
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
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
        String sql = "SELECT * FROM cashier WHERE name = ? AND email = ? AND status = 'approved'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            try (ResultSet rs = pstmt.executeQuery()) {
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
