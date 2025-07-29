package DAO;

import Model.Billing;
import Utils.DatabaseUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.PreparedStatement;

/**
 * Data Access Object for managing Billing entities in the Pahana Edu Online Billing System.
 * Uses stored procedures for all database operations.
 *
 * @author BIMSARA
 */
public class BillingDAO {

    public int createBill(Billing bill) throws SQLException {
        String sql = "{CALL sp_create_bill(?, ?, ?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Validate inputs
            if (bill.getBillItems() == null || bill.getBillItems().isEmpty()) {
                throw new IllegalArgumentException("Bill items cannot be empty");
            }
            for (Billing.BillItem item : bill.getBillItems()) {
                if (item.getSubtotal() != item.getQuantity() * item.getUnitPrice()) {
                    throw new IllegalArgumentException("Subtotal mismatch for item ID " + item.getItemId());
                }
            }
            
            // Convert bill items to JSON
            JsonArray billItemsJson = new JsonArray();
            for (Billing.BillItem billItem : bill.getBillItems()) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("item_id", billItem.getItemId());
                itemJson.addProperty("quantity", billItem.getQuantity());
                itemJson.addProperty("unit_price", billItem.getUnitPrice());
                itemJson.addProperty("subtotal", billItem.getSubtotal());
                billItemsJson.add(itemJson);
            }
            String jsonString = new Gson().toJson(billItemsJson);

            try (CallableStatement cstmt = conn.prepareCall(sql)) {
                cstmt.setInt(1, bill.getCustomerId());
                cstmt.setDouble(2, bill.getTotalAmount());
                cstmt.setString(3, jsonString);
                cstmt.registerOutParameter(4, Types.INTEGER);
                cstmt.executeUpdate();
                return cstmt.getInt(4);
            }
        }
    }

    public Billing getBillById(int id) throws SQLException {
        String billSql = "{CALL sp_get_bill_by_id(?)}";
        String billItemSql = "{CALL sp_get_bill_items_by_bill_id(?)}";

        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement billStmt = conn.prepareCall(billSql)) {
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
                try (CallableStatement billItemStmt = conn.prepareCall(billItemSql)) {
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
        String billSql = "{CALL sp_get_bills_by_customer_id(?)}";
        String billItemSql = "{CALL sp_get_bill_items_by_bill_id(?)}";

        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement billStmt = conn.prepareCall(billSql)) {
            billStmt.setInt(1, customerId);
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
                    try (CallableStatement billItemStmt = conn.prepareCall(billItemSql)) {
                        billItemStmt.setInt(1, bill.getId());
                        try (ResultSet billItemRs = billItemStmt.executeQuery()) {
                            while (billItemRs.next()) {
                                billItems.add(new Billing.BillItem(
                                    billItemRs.getInt("id"),
                                    billItemRs.getInt("item_id"),
                                    billItemRs.getInt("quantity"),
                                    billItemRs.getDouble("unit_price"),
                                    billItemRs.getDouble("subtotal")
                                )
                                );
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
        String billSql = "{CALL sp_get_monthly_bill_details()}";
        String billItemSql = "{CALL sp_get_bill_items_with_names(?)}";

        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement billStmt = conn.prepareCall(billSql)) {
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
                    try (CallableStatement billItemStmt = conn.prepareCall(billItemSql)) {
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

    public String getCustomerEmail(int customerId) throws SQLException {
        String sql = "{CALL sp_get_customer_email(?, ?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, customerId);
            cstmt.registerOutParameter(2, Types.VARCHAR);
            cstmt.execute();
            return cstmt.getString(2);
        }
    }

    public Map<String, Object> getBillContentForSending(int billId) throws SQLException {
        String sql = "{CALL sp_get_bill_content_for_sending(?)}";
        try (Connection conn = DatabaseUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, billId);
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> billContent = new HashMap<>();
                    billContent.put("billId", rs.getInt("bill_id"));
                    billContent.put("billNo", rs.getString("bill_no"));
                    billContent.put("customerId", rs.getInt("customer_id"));
                    billContent.put("customerName", rs.getString("customer_name"));
                    billContent.put("customerEmail", rs.getString("customer_email"));
                    billContent.put("totalAmount", rs.getDouble("total_amount"));
                    billContent.put("createdAt", rs.getDate("created_at").toLocalDate().toString());

                    String billItemsJson = rs.getString("bill_items");
                    List<Map<String, Object>> billItems = new ArrayList<>();
                    JsonArray jsonArray = new Gson().fromJson(billItemsJson, JsonArray.class);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                        Map<String, Object> item = new HashMap<>();
                        item.put("itemId", jsonObj.get("itemId").getAsInt());
                        item.put("quantity", jsonObj.get("quantity").getAsInt());
                        item.put("unitPrice", jsonObj.get("unitPrice").getAsDouble());
                        item.put("subtotal", jsonObj.get("subtotal").getAsDouble());
                        item.put("itemName", jsonObj.get("itemName").getAsString());
                        billItems.add(item);
                    }
                    billContent.put("billItems", billItems);
                    return billContent;
                }
            }
        }
        throw new SQLException("Bill with ID " + billId + " not found");
    }
}
    
    
      