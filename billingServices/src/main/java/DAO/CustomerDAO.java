package DAO;

import Model.Customer;
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
 * Data Access Object for managing Customer entities in the Pahana Edu Online Billing System.
 * Uses stored procedures for all database operations.
 *
 * @author BIMSARA
 */
public class CustomerDAO {

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "{CALL sp_get_all_customers()}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql);
             ResultSet rs = cstmt.executeQuery()) {
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getDate("dob").toLocalDate(),
                    rs.getString("address"),
                    rs.getString("nic"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("account_number"),
                    rs.getString("status"),
                    rs.getDate("created_at").toLocalDate(),
                    rs.getDate("updated_at").toLocalDate()
                ));
            }
        }
        return customers;
    }

    public Customer getCustomerById(int id) throws SQLException {
        String sql = "{CALL sp_get_customer_by_id(?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getDate("dob").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("nic"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("account_number"),
                        rs.getString("status"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    public int registerCustomer(Customer customer) throws SQLException {
        String sql = "{CALL sp_register_customer(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, customer.getName());
            cstmt.setString(2, customer.getGender());
            cstmt.setDate(3, Date.valueOf(customer.getDob()));
            cstmt.setString(4, customer.getAddress());
            cstmt.setString(5, customer.getNic());
            cstmt.setString(6, customer.getEmail());
            cstmt.setString(7, customer.getPhone());
            cstmt.registerOutParameter(8, Types.INTEGER);
            cstmt.executeUpdate();
            return cstmt.getInt(8);
        }
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "{CALL sp_update_customer(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, customer.getId());
            cstmt.setString(2, customer.getName());
            cstmt.setString(3, customer.getGender());
            cstmt.setDate(4, Date.valueOf(customer.getDob()));
            cstmt.setString(5, customer.getAddress());
            cstmt.setString(6, customer.getNic());
            cstmt.setString(7, customer.getEmail());
            cstmt.setString(8, customer.getPhone());
            cstmt.registerOutParameter(9, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(9);
        }
    }

    public boolean deleteCustomer(int id) throws SQLException {
        String sql = "{CALL sp_delete_customer(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.BOOLEAN);
            cstmt.executeUpdate();
            return cstmt.getBoolean(2);
        }
    }

    public List<Customer> searchCustomers(Map<String, String> criteria) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "{CALL sp_search_customers(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, criteria != null ? criteria.get("name") : null);
            cstmt.setString(2, criteria != null ? criteria.get("gender") : null);
            cstmt.setString(3, criteria != null ? criteria.get("nic") : null);
            cstmt.setString(4, criteria != null ? criteria.get("email") : null);
            cstmt.setString(5, criteria != null ? criteria.get("phone") : null);
            cstmt.setString(6, criteria != null ? criteria.get("accountNumber") : null);
            cstmt.setString(7, criteria != null ? criteria.get("status") : null);
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getDate("dob").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("nic"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("account_number"),
                        rs.getString("status"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getDate("updated_at").toLocalDate()
                    ));
                }
            }
        }
        return customers;
    }
}