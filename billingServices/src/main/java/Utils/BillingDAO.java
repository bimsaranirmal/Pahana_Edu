package Utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingDAO {

    private String generateBillNo(Connection conn) throws SQLException {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT COUNT(*) FROM Bill WHERE bill_no LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "BILL-" + datePart + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    return String.format("BILL-%s-%04d", datePart, count);
                }
            }
        }
        return "BILL-" + datePart + "-0001";
    }

    public int createBill(Billing bill) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            String billNo = generateBillNo(conn);

            String billSql = "INSERT INTO Bill (bill_no, customer_id, total_amount, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
            int billId = -1;
            try (PreparedStatement billStmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                billStmt.setString(1, billNo);
                billStmt.setInt(2, bill.getCustomerId());
                billStmt.setDouble(3, bill.getTotalAmount());
                billStmt.executeUpdate();
                try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getInt(1);
                    }
                }
            }

            if (billId == -1) {
                conn.rollback();
                return -1;
            }

            String checkStockSql = "SELECT stock_quantity FROM Item WHERE id = ? FOR UPDATE";
            String updateStockSql = "UPDATE Item SET stock_quantity = ?, updated_at = NOW() WHERE id = ?";
            String billItemSql = "INSERT INTO BillItem (bill_id, item_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

            for (Billing.BillItem billItem : bill.getBillItems()) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                    checkStmt.setInt(1, billItem.getItemId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            int currentStock = rs.getInt("stock_quantity");
                            if (currentStock < billItem.getQuantity()) {
                                conn.rollback();
                                throw new SQLException("Insufficient stock for item ID " + billItem.getItemId());
                            }
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                                updateStmt.setInt(1, currentStock - billItem.getQuantity());
                                updateStmt.setInt(2, billItem.getItemId());
                                updateStmt.executeUpdate();
                            }
                        } else {
                            conn.rollback();
                            throw new SQLException("Item ID " + billItem.getItemId() + " not found");
                        }
                    }
                }

                try (PreparedStatement billItemStmt = conn.prepareStatement(billItemSql)) {
                    billItemStmt.setInt(1, billId);
                    billItemStmt.setInt(2, billItem.getItemId());
                    billItemStmt.setInt(3, billItem.getQuantity());
                    billItemStmt.setDouble(4, billItem.getUnitPrice());
                    billItemStmt.setDouble(5, billItem.getSubtotal());
                    billItemStmt.executeUpdate();
                }
            }

            conn.commit();
            return billId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Billing getBillById(int id) throws SQLException {
        String billSql = "SELECT id, bill_no, customer_id, total_amount, created_at, updated_at FROM Bill WHERE id = ?";
        String billItemSql = "SELECT id, bill_id, item_id, quantity, unit_price, subtotal FROM BillItem WHERE bill_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement billStmt = conn.prepareStatement(billSql)) {
            billStmt.setInt(1, id);
            Billing bill = null;
            try (ResultSet billRs = billStmt.executeQuery()) {
                if (billRs.next()) {
                    bill = new Billing(
                        billRs.getInt("id"),
                        billRs.getString("bill_no"),
                        billRs.getInt("customer_id"),
                        billRs.getDouble("total_amount"),
                        null,
                        billRs.getDate("created_at").toLocalDate(),
                        billRs.getDate("updated_at").toLocalDate()
                    );
                }
            }

            if (bill != null) {
                List<Billing.BillItem> billItems = new ArrayList<>();
                try (PreparedStatement billItemStmt = conn.prepareStatement(billItemSql)) {
                    billItemStmt.setInt(1, id);
                    try (ResultSet billItemRs = billItemStmt.executeQuery()) {
                        while (billItemRs.next()) {
                            billItems.add(new Billing.BillItem(
                                billItemRs.getInt("id"),
                                billItemRs.getInt("item_id"),
                                billItemRs.getInt("quantity"),
                                billItemRs.getDouble("unit_price"),
                                billItemRs.getDouble("subtotal")
                            ));
                        }
                    }
                }
                bill.setBillItems(billItems);
            }

            return bill;
        }
    }

    public List<Billing> getBillsByCustomerId(int customerId) throws SQLException {
        List<Billing> bills = new ArrayList<>();
        String billSql = customerId == 0 
            ? "SELECT id, bill_no, customer_id, total_amount, created_at, updated_at FROM Bill"
            : "SELECT id, bill_no, customer_id, total_amount, created_at, updated_at FROM Bill WHERE customer_id = ?";
        String billItemSql = "SELECT id, bill_id, item_id, quantity, unit_price, subtotal FROM BillItem WHERE bill_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement billStmt = conn.prepareStatement(billSql)) {
            if (customerId != 0) {
                billStmt.setInt(1, customerId);
            }
            try (ResultSet billRs = billStmt.executeQuery()) {
                while (billRs.next()) {
                    Billing bill = new Billing(
                        billRs.getInt("id"),
                        billRs.getString("bill_no"),
                        billRs.getInt("customer_id"),
                        billRs.getDouble("total_amount"),
                        null,
                        billRs.getDate("created_at").toLocalDate(),
                        billRs.getDate("updated_at").toLocalDate()
                    );

                    List<Billing.BillItem> billItems = new ArrayList<>();
                    try (PreparedStatement billItemStmt = conn.prepareStatement(billItemSql)) {
                        billItemStmt.setInt(1, bill.getId());
                        try (ResultSet billItemRs = billItemStmt.executeQuery()) {
                            while (billItemRs.next()) {
                                billItems.add(new Billing.BillItem(
                                    billItemRs.getInt("id"),
                                    billItemRs.getInt("item_id"),
                                    billItemRs.getInt("quantity"),
                                    billItemRs.getDouble("unit_price"),
                                    billItemRs.getDouble("subtotal")
                                ));
                            }
                        }
                    }
                    bill.setBillItems(billItems);
                    bills.add(bill);
                }
            }
        }
        return bills;
    }
    
    public Map<String, Object> getBillingStatistics() throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        double totalBilling = 0.0;

        String monthlySql = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, " +
                           "SUM(total_amount) AS total_amount, " +
                           "COUNT(*) AS bill_count " +
                           "FROM Bill " +
                           "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                           "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                           "ORDER BY month DESC";
        String totalSql = "SELECT SUM(total_amount) AS total_billing FROM Bill";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement monthlyStmt = conn.prepareStatement(monthlySql);
             PreparedStatement totalStmt = conn.prepareStatement(totalSql)) {

            // Fetch monthly statistics
            try (ResultSet rs = monthlyStmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("month", rs.getString("month"));
                    stat.put("totalAmount", rs.getDouble("total_amount"));
                    stat.put("billCount", rs.getInt("bill_count"));
                    monthlyStats.add(stat);
                }
            }

            // Fetch total billing amount
            try (ResultSet rs = totalStmt.executeQuery()) {
                if (rs.next()) {
                    totalBilling = rs.getDouble("total_billing");
                }
            }

            result.put("monthlyStats", monthlyStats);
            result.put("totalBilling", totalBilling);
        }

        return result;
    }
    
    public Map<String, List<Map<String, Object>>> getMonthlyBillDetails() throws SQLException {
        Map<String, List<Map<String, Object>>> monthlyBills = new HashMap<>();
        String billSql = "SELECT b.id, b.bill_no, b.customer_id, c.name AS customer_name, b.total_amount, " +
                        "b.created_at, b.updated_at, DATE_FORMAT(b.created_at, '%Y-%m') AS month " +
                        "FROM Bill b JOIN Customer c ON b.customer_id = c.id " +
                        "WHERE b.created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                        "ORDER BY b.created_at DESC";
        String billItemSql = "SELECT bi.id, bi.bill_id, bi.item_id, bi.quantity, bi.unit_price, bi.subtotal, i.name AS item_name " +
                            "FROM BillItem bi JOIN Item i ON bi.item_id = i.id WHERE bi.bill_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement billStmt = conn.prepareStatement(billSql)) {
            try (ResultSet billRs = billStmt.executeQuery()) {
                while (billRs.next()) {
                    String month = billRs.getString("month");
                    Map<String, Object> billDetails = new HashMap<>();
                    billDetails.put("id", billRs.getInt("id"));
                    billDetails.put("billNo", billRs.getString("bill_no"));
                    billDetails.put("customerId", billRs.getInt("customer_id"));
                    billDetails.put("customerName", billRs.getString("customer_name"));
                    billDetails.put("totalAmount", billRs.getDouble("total_amount"));
                    billDetails.put("createdAt", billRs.getDate("created_at").toLocalDate().toString());
                    billDetails.put("updatedAt", billRs.getDate("updated_at").toLocalDate().toString());

                    List<Map<String, Object>> billItems = new ArrayList<>();
                    try (PreparedStatement billItemStmt = conn.prepareStatement(billItemSql)) {
                        billItemStmt.setInt(1, billRs.getInt("id"));
                        try (ResultSet billItemRs = billItemStmt.executeQuery()) {
                            while (billItemRs.next()) {
                                Map<String, Object> item = new HashMap<>();
                                item.put("id", billItemRs.getInt("id"));
                                item.put("itemId", billItemRs.getInt("item_id"));
                                item.put("quantity", billItemRs.getInt("quantity"));
                                item.put("unitPrice", billItemRs.getDouble("unit_price"));
                                item.put("subtotal", billItemRs.getDouble("subtotal"));
                                item.put("itemName", billItemRs.getString("item_name"));
                                billItems.add(item);
                            }
                        }
                    }
                    billDetails.put("billItems", billItems);
                    monthlyBills.computeIfAbsent(month, k -> new ArrayList<>()).add(billDetails);
                }
            }
        }
        return monthlyBills;
    }
}