package DAO;

import Model.Customer;
import DAO.CustomerDAO;
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

public class CustomerDAOTests {
    
    private CustomerDAO customerDAO;

    public CustomerDAOTests() {
    }
    
    @BeforeEach
    public void setUp() {
        customerDAO = new CustomerDAO();
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer customer : customers) {
            if (customer.getName().startsWith("Test Customer")) {
                customerDAO.deleteCustomer(customer.getId());
            }
        }
    }

    @Test
    public void testRegisterAndGetCustomer() throws SQLException {
        Customer testCustomer = new Customer(0, "Test Customer", "Male",
                LocalDate.of(1990, 1, 1), "Test Address",
                "123456789V", "test@example.com", "0771234567", "ACC123",
                "Active", LocalDate.now(), LocalDate.now());
        
        int customerId = customerDAO.registerCustomer(testCustomer);
        assertTrue(customerId > 0, "Failed to register test customer");
        
        Customer savedCustomer = customerDAO.getCustomerById(customerId);
        assertNotNull(savedCustomer, "Customer not saved in database");
        assertEquals("Test Customer", savedCustomer.getName(), "Saved customer name mismatch");
        
        boolean deleted = customerDAO.deleteCustomer(customerId);
        assertTrue(deleted, "Failed to delete test customer");
        
        Customer result = customerDAO.getCustomerById(customerId);
        assertNull(result, "Customer should be deleted");
    }

    @Test
    public void testGetAllCustomers() throws SQLException {
        System.out.println("getAllCustomers");
        List<Customer> result = customerDAO.getAllCustomers();
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() >= 0, "Result size should be non-negative");
    }

    @Test
    public void testUpdateCustomer() throws SQLException {
        System.out.println("updateCustomer");
        Customer testCustomer = new Customer(0, "Test Customer Update", "Female",
                LocalDate.of(1985, 5, 15), "Original Address", "987654321V",
                "update@example.com", "0777654321", "ACC456", "Active", LocalDate.now(),
                LocalDate.now());
        
        int customerId = customerDAO.registerCustomer(testCustomer);
        assertTrue(customerId > 0, "Failed to register test customer");
        
        testCustomer.setId(customerId);
        testCustomer.setName("Updated Customer");
        testCustomer.setGender("Male");
        testCustomer.setDob(LocalDate.of(1986, 6, 20));
        testCustomer.setAddress("Updated Address");
        testCustomer.setNic("111222333V");
        testCustomer.setEmail("updated@example.com");
        testCustomer.setPhone("0771122334");
        
        boolean result = customerDAO.updateCustomer(testCustomer);
        assertTrue(result, "Failed to update customer");
        
        Customer updatedCustomer = customerDAO.getCustomerById(customerId);
        assertEquals("Updated Customer", updatedCustomer.getName(), "Name not updated");
        assertEquals("Male", updatedCustomer.getGender(), "Gender not updated");
        assertEquals(LocalDate.of(1986, 6, 20), updatedCustomer.getDob(), "DOB not updated");
        assertEquals("Updated Address", updatedCustomer.getAddress(), "Address not updated");
        assertEquals("111222333V", updatedCustomer.getNic(), "NIC not updated");
        assertEquals("updated@example.com", updatedCustomer.getEmail(), "Email not updated");
        assertEquals("0771122334", updatedCustomer.getPhone(), "Phone not updated");
        
        customerDAO.deleteCustomer(customerId);
    }

    @Test
    public void testDeleteCustomer() throws SQLException {
        System.out.println("deleteCustomer");
        Customer testCustomer = new Customer(0, "Test Customer Delete", "Male", LocalDate.of(1995, 3, 10),
                "Delete Address", "444555666V", "delete@example.com", "0774445556", "ACC789",
                "Inactive", LocalDate.now(), LocalDate.now());
        
        int customerId = customerDAO.registerCustomer(testCustomer);
        assertTrue(customerId > 0, "Failed to register test customer");
        
        boolean result = customerDAO.deleteCustomer(customerId);
        assertTrue(result, "Failed to delete customer");
        
        Customer deletedCustomer = customerDAO.getCustomerById(customerId);
        assertNull(deletedCustomer, "Customer should be deleted");
    }
    @Test
    public void testSearchCustomers() throws SQLException {
        System.out.println("searchCustomers");
        CustomerDAO instance = new CustomerDAO();
        System.out.println("Registering customer for search");
        Customer customer = new Customer(0, "Searchable Customer2", "Female", LocalDate.of(1991, 4, 30),
                "159 Search Rd", "147258369V", "search2@example.com", "0714567890", "ACC159",
                "Pending", LocalDate.now(), LocalDate.now());
        int newId = instance.registerCustomer(customer);
        assertNotEquals(-1, newId, "Failed to register customer for search test");

        System.out.println("Searching for customer with email: search2@example.com");
        Map<String, String> criteria = new HashMap<>();
        criteria.put("email", "search2@example.com");
        criteria.put("status", "Pending");
        List<Customer> customers = instance.searchCustomers(criteria);
        assertNotNull(customers, "Search result should not be null");
        System.out.println("Search completed: " + customers.size() + " customers found");
        assertFalse(customers.isEmpty(), "Search result should not be empty");
        System.out.println("Verified customer found: Email " + customers.get(0).getEmail());
        assertEquals("search2@example.com", customers.get(0).getEmail(), "Email mismatch in search result");

        System.out.println("Deleting customer with ID: " + newId);
        instance.deleteCustomer(newId);
    }

}