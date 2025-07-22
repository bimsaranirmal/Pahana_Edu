package DAO;

import Model.Customer;
import Utils.DatabaseUtil;
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
 * Data Access Object for managing Customer entities in the database.
 *
 * @author BIMSARA
 */
public class CustomerDAO {

    /**
     * Generates a unique account number in the format ACC{number}.
     *
     * @param conn Database connection
     * @return A unique account number (e.g., ACC001)
     * @throws SQLException If a database access error occurs
     */
    private String generateAccountNumber(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WHERE account_number LIKE 'ACC%'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("ACC%03d", count);
            }
        }
        return "ACC001"; // Default if no accounts exist
    }

    /**
     * Retrieves a list of all customers from the database.
     *
     * @return A list of Customer objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT id, name, gender, dob, address, nic, email, phone, account_number, status, created_at, updated_at FROM customer";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return The Customer object if found, otherwise null.
     * @throws SQLException If a database access error occurs.
     */
    public Customer getCustomerById(int id) throws SQLException {
        String sql = "SELECT id, name, gender, dob, address, nic, email, phone, account_number, status, created_at, updated_at FROM customer WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
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

    /**
     * Registers a new customer in the database with an automatically generated account number.
     *
     * @param customer The Customer object containing the new customer's details.
     * @return The generated ID of the new customer, or -1 if registration fails.
     * @throws SQLException If a database access error occurs.
     */
    public int registerCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer (name, gender, dob, address, nic, email, phone, account_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String accountNumber = generateAccountNumber(conn);
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getGender());
            pstmt.setDate(3, Date.valueOf(customer.getDob()));
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getNic());
            pstmt.setString(6, customer.getEmail());
            pstmt.setString(7, customer.getPhone());
            pstmt.setString(8, accountNumber);
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
     * Updates an existing customer's details in the database, excluding account_number.
     *
     * @param customer The Customer object with updated details.
     * @return True if the update was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customer SET name = ?, gender = ?, dob = ?, address = ?, nic = ?, email = ?, phone = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getGender());
            pstmt.setDate(3, Date.valueOf(customer.getDob()));
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getNic());
            pstmt.setString(6, customer.getEmail());
            pstmt.setString(7, customer.getPhone());
            pstmt.setInt(8, customer.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a customer from the database by their ID.
     *
     * @param id The ID of the customer to delete.
     * @return True if the deletion was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customer WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Approves a customer by setting their status to 'approved'.
     *
     * @param id The ID of the customer to approve.
     * @return True if the status update was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean approveCustomer(int id) throws SQLException {
        return updateStatus(id, "approved");
    }

    /**
     * Rejects a customer by setting their status to 'rejected'.
     *
     * @param id The ID of the customer to reject.
     * @return True if the status update was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean rejectCustomer(int id) throws SQLException {
        return updateStatus(id, "rejected");
    }

    /**
     * Helper method to update the status of a customer.
     *
     * @param id The ID of the customer.
     * @param status The new status to set ('approved' or 'rejected').
     * @return True if the status update was successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    private boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE customer SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Searches for customers based on provided criteria.
     *
     * @param criteria A Map where keys are column names (e.g., "name", "gender") and values are search terms.
     * @return A list of Customer objects matching the criteria.
     * @throws SQLException If a database access error occurs.
     */
    public List<Customer> searchCustomers(Map<String, String> criteria) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, name, gender, dob, address, nic, email, phone, account_number, status, created_at, updated_at FROM customer WHERE 1=1");
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
            if (criteria.containsKey("accountNumber")) {
                sql.append(" AND account_number = ?");
                params.add(criteria.get("accountNumber"));
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

    /**
     * Authenticates a customer based on username (name) and email, and an 'approved' status.
     *
     * @param username The customer's name.
     * @param email The customer's email.
     * @return The Customer object if authenticated and approved, otherwise null.
     * @throws SQLException If a database access error occurs.
     */
    public Customer authenticateCustomer(String username, String email) throws SQLException {
        String sql = "SELECT id, name, gender, dob, address, nic, email, phone, account_number, status, created_at, updated_at FROM customer WHERE name = ? AND email = ? AND status = 'approved'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            try (ResultSet rs = pstmt.executeQuery()) {
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
}