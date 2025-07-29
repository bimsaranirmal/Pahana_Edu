package Model;

import jakarta.json.bind.annotation.JsonbDateFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a Bill entity for the Pahana Edu Online Billing System.
 * Stores bill details including customer, items, and bill number as per assignment requirements.
 *
 * @author BIMSARA
 */
public class Billing {
    private int id;
    private String billNo;
    private int customerId;
    private double totalAmount;
    private List<BillItem> billItems;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate updatedAt;

    // Default constructor
    public Billing() {
    }

    // Full constructor
    public Billing(int id, String billNo, int customerId, double totalAmount, List<BillItem> billItems, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.billNo = billNo;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.billItems = billItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Inner class to represent items in a bill.
     */
    public static class BillItem {
        private int id;
        private int itemId;
        private int quantity;
        private double unitPrice;
        private double subtotal;

        public BillItem() {
        }

        public BillItem(int id, int itemId, int quantity, double unitPrice, double subtotal) {
            this.id = id;
            this.itemId = itemId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public BillItem(int aInt, int aInt0, int aInt1, double aDouble, double aDouble0, String string) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(double subtotal) {
            this.subtotal = subtotal;
        }
    }
}