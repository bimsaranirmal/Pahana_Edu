
package DAO;

import Model.Billing;
import Model.Billing.BillItem;
import Model.Customer;
import DAO.BillingDAO;
import DAO.CustomerDAO;
import Utils.DatabaseUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BillingDAOTests {
    
    private BillingDAO billingDAO;
    private CustomerDAO customerDAO;
    private int testCustomerId;

    @BeforeEach
    public void setUp() throws SQLException {
        billingDAO = new BillingDAO();
        customerDAO = new CustomerDAO();
        // Create a test customer
        Customer testCustomer = new Customer(0, "Test Customer", "Male", LocalDate.of(1990, 1, 1), "Test Address", "123456789V", "test@example.com", "0771234567", "ACC123", "Active", LocalDate.now(), LocalDate.now());
        testCustomerId = customerDAO.registerCustomer(testCustomer);
        assertTrue(testCustomerId > 0, "Failed to register test customer");
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        // Cleanup: Delete test bills and customer
        List<Billing> bills = billingDAO.getBillsByCustomerId(testCustomerId);
        for (Billing bill : bills) {
            if (bill.getBillNo().startsWith("BILL-")) {
                System.out.println("Bill " + bill.getId() + " marked for cleanup, no delete method available");
            }
        }
        if (testCustomerId > 0) {
            customerDAO.deleteCustomer(testCustomerId);
        }
    }

    @Test
    public void testCreateBill() throws SQLException {
        System.out.println("createBill");
        List<BillItem> billItems = new ArrayList<>();
        billItems.add(new BillItem(0, 1, 2, 10.0, 20.0)); 
        billItems.add(new BillItem(0, 2, 1, 15.0, 15.0)); 
        Billing testBill = new Billing(0, "BILL-20250729-0001", testCustomerId, 35.0, billItems, 
                LocalDate.now(), LocalDate.now());

        int billId = billingDAO.createBill(testBill);
        assertTrue(billId > 0, "Failed to create test bill");

        Billing savedBill = billingDAO.getBillById(billId);
        assertNotNull(savedBill, "Bill not saved in database");
        assertEquals("BILL-20250729-0001", savedBill.getBillNo(), "Saved bill number mismatch");
        assertEquals(testCustomerId, savedBill.getCustomerId(), "Saved customer ID mismatch");
        assertEquals(35.0, savedBill.getTotalAmount(), 0.01, "Saved total amount mismatch");
        assertEquals(2, savedBill.getBillItems().size(), "Number of bill items mismatch");

        // Verify bill items
        Map<Integer, BillItem> itemMap = new HashMap<>();
        for (BillItem item : savedBill.getBillItems()) {
            itemMap.put(item.getItemId(), item);
        }
        assertEquals(20.0, itemMap.get(1).getSubtotal(), 0.01, "Item 1 subtotal mismatch");
        assertEquals(15.0, itemMap.get(2).getSubtotal(), 0.01, "Item 2 subtotal mismatch");
    }

    @Test
    public void testGetBillById() throws SQLException {
        System.out.println("getBillById");
        List<BillItem> billItems = new ArrayList<>();
        billItems.add(new BillItem(0, 1, 2, 10.0, 20.0));
        Billing testBill = new Billing(0, "BILL-20250729-0001", testCustomerId, 20.0, billItems,
                LocalDate.now(), LocalDate.now());
        int billId = billingDAO.createBill(testBill);
        assertTrue(billId > 0, "Failed to create test bill");

        Billing retrievedBill = billingDAO.getBillById(billId);
        assertNotNull(retrievedBill, "Bill not found");
        assertEquals("BILL-20250729-0001", retrievedBill.getBillNo(), "Retrieved bill number mismatch");
        assertEquals(testCustomerId, retrievedBill.getCustomerId(), "Retrieved customer ID mismatch");
        assertEquals(20.0, retrievedBill.getTotalAmount(), 0.01, "Retrieved total amount mismatch");
        assertEquals(1, retrievedBill.getBillItems().size(), "Number of bill items mismatch");
        assertEquals(20.0, retrievedBill.getBillItems().get(0).getSubtotal(),
                0.01, "Bill item subtotal mismatch");
    }

    @Test
    public void testGetBillsByCustomerId() throws SQLException {
        System.out.println("getBillsByCustomerId");
        List<BillItem> billItems = new ArrayList<>();
        billItems.add(new BillItem(0, 1, 2, 10.0, 20.0));
        Billing testBill = new Billing(0, "BILL-20250729-0001", testCustomerId, 20.0, billItems,
                LocalDate.now(), LocalDate.now());
        int billId = billingDAO.createBill(testBill);
        assertTrue(billId > 0, "Failed to create test bill");

        List<Billing> bills = billingDAO.getBillsByCustomerId(testCustomerId);
        assertNotNull(bills, "Bills list should not be null");
        assertFalse(bills.isEmpty(), "No bills found for customer ID " + testCustomerId);
        boolean found = false;
        for (Billing bill : bills) {
            if (bill.getId() == billId) {
                found = true;
                assertEquals("BILL-20250729-0001", bill.getBillNo(), "Bill number mismatch");
                assertEquals(20.0, bill.getTotalAmount(), 0.01, "Total amount mismatch");
                assertEquals(1, bill.getBillItems().size(), "Number of bill items mismatch");
            }
        }
        assertTrue(found, "Created bill not found in customer bills");
    }

    
}
