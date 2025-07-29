
package DAO;

import Model.Cashier;
import DAO.CashierDAO;
import Utils.DatabaseUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CashierDAOTests {
    
    private CashierDAO cashierDAO;

    @BeforeEach
    public void setUp() {
        cashierDAO = new CashierDAO();
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        List<Cashier> cashiers = cashierDAO.getAllCashiers();
        for (Cashier cashier : cashiers) {
            if (cashier.getName().startsWith("Test Cashier")) {
                cashierDAO.deleteCashier(cashier.getId());
            }
        }
    }

    @Test
    public void testRegisterCashier() throws SQLException {
        System.out.println("registerCashiers");
        Cashier testCashier = new Cashier(0, "Test Cashier", "Male", LocalDate.of(1990, 1, 1), "Test Address", "123456789V", "test@example.com", "0771234567", "Active", LocalDate.now(), LocalDate.now());
        
        int cashierId = cashierDAO.registerCashier(testCashier);
        assertTrue(cashierId > 0, "Failed to register test cashier");
        
        Cashier savedCashier = cashierDAO.getCashierById(cashierId);
        assertNotNull(savedCashier, "Cashier not saved in database");
        assertEquals("Test Cashier", savedCashier.getName(), "Saved cashier name mismatch");
        assertEquals("Male", savedCashier.getGender(), "Saved cashier gender mismatch");
        assertEquals("test@example.com", savedCashier.getEmail(), "Saved cashier email mismatch");
        
        boolean deleted = cashierDAO.deleteCashier(cashierId);
        assertTrue(deleted, "Failed to delete test cashier");
        
        Cashier result = cashierDAO.getCashierById(cashierId);
        assertNull(result, "Cashier should be deleted");
    }

    @Test
    public void testGetAllCashiers() throws SQLException {
        System.out.println("getAllCashiers");
        List<Cashier> result = cashierDAO.getAllCashiers();
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() >= 0, "Result size should be non-negative");
    }

    @Test
    public void testUpdateCashier() throws SQLException {
        System.out.println("updateCashier");
        Cashier testCashier = new Cashier(0, "Test Cashier Update", "Female", LocalDate.of(1985, 5, 15), "Original Address", "987654321V", "update@example.com", "0777654321", "Active", LocalDate.now(), LocalDate.now());
        
        int cashierId = cashierDAO.registerCashier(testCashier);
        assertTrue(cashierId > 0, "Failed to register test cashier");
        
        testCashier.setId(cashierId);
        testCashier.setName("Updated Cashier");
        testCashier.setGender("Male");
        testCashier.setDob(LocalDate.of(1986, 6, 20));
        testCashier.setAddress("Updated Address");
        testCashier.setNic("111222333V");
        testCashier.setEmail("updated@example.com");
        testCashier.setPhone("0771122334");
        
        boolean result = cashierDAO.updateCashier(testCashier);
        assertTrue(result, "Failed to update cashier");
        
        Cashier updatedCashier = cashierDAO.getCashierById(cashierId);
        assertEquals("Updated Cashier", updatedCashier.getName(), "Name not updated");
        assertEquals("Male", updatedCashier.getGender(), "Gender not updated");
        assertEquals(LocalDate.of(1986, 6, 20), updatedCashier.getDob(), "DOB not updated");
        assertEquals("Updated Address", updatedCashier.getAddress(), "Address not updated");
        assertEquals("111222333V", updatedCashier.getNic(), "NIC not updated");
        assertEquals("updated@example.com", updatedCashier.getEmail(), "Email not updated");
        assertEquals("0771122334", updatedCashier.getPhone(), "Phone not updated");
        
        cashierDAO.deleteCashier(cashierId);
    }

    @Test
    public void testDeleteCashier() throws SQLException {
        System.out.println("deleteCashier");
        Cashier testCashier = new Cashier(0, "Test Cashier Delete", "Male", LocalDate.of(1995, 3, 10), "Delete Address", "444555666V", "delete@example.com", "0774445556", "Inactive", LocalDate.now(), LocalDate.now());
        
        int cashierId = cashierDAO.registerCashier(testCashier);
        assertTrue(cashierId > 0, "Failed to register test cashier");
        
        boolean result = cashierDAO.deleteCashier(cashierId);
        assertTrue(result, "Failed to delete cashier");
        
        Cashier deletedCashier = cashierDAO.getCashierById(cashierId);
        assertNull(deletedCashier, "Cashier should be deleted");
    }
}
