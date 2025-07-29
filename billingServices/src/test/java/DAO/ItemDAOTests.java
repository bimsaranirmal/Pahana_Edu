package DAO;

import Model.Item;
import DAO.ItemDAO;
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

public class ItemDAOTests {
    
    private ItemDAO itemDAO;

    public ItemDAOTests() {
    }
    
    @BeforeEach
    public void setUp() {
        itemDAO = new ItemDAO();
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        List<Item> items = itemDAO.getAllItems();
        for (Item item : items) {
            if (item.getName().startsWith("Test Item")) {
                itemDAO.deleteItem(item.getId());
            }
        }
    }

    @Test
    public void testAddAndGetItem() throws SQLException {
        Item testItem = new Item(0, "Test Item", "Test Description", 99.99, 50, 1,
                LocalDate.now(), LocalDate.now());
        
        int itemId = itemDAO.addItem(testItem);
        assertTrue(itemId > 0, "Failed to add test item");
        
        Item result = itemDAO.getItemById(itemId);
        assertNotNull(result, "Item not found");
        assertEquals(testItem.getName(), result.getName(), "Name mismatch");
        assertEquals(testItem.getDescription(), result.getDescription(), "Description mismatch");
        assertEquals(testItem.getPrice(), result.getPrice(), 0.01, "Price mismatch");
        assertEquals(testItem.getStockQuantity(), result.getStockQuantity(), "Stock quantity mismatch");
        assertEquals(testItem.getCategoryId(), result.getCategoryId(), "Category ID mismatch");
        
        boolean deleted = itemDAO.deleteItem(itemId);
        assertTrue(deleted, "Failed to delete test item");
        
        result = itemDAO.getItemById(itemId);
        assertNull(result, "Item should be deleted");
    }

    @Test
    public void testGetAllItems() throws SQLException {
        System.out.println("getAllItems");
        List<Item> result = itemDAO.getAllItems();
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() >= 0, "Result size should be non-negative");
    }

    @Test
    public void testUpdateItem() throws SQLException {
        System.out.println("updateItem");
        Item testItem = new Item(0, "Test Item Update", "Original Description", 49.99, 25, 2,
                LocalDate.now(), LocalDate.now());
        
        int itemId = itemDAO.addItem(testItem);
        assertTrue(itemId > 0, "Failed to add test item");
        
        testItem.setId(itemId);
        testItem.setName("Updated Item");
        testItem.setDescription("Updated Description");
        testItem.setPrice(59.99);
        testItem.setStockQuantity(30);
        testItem.setCategoryId(3);
        
        boolean result = itemDAO.updateItem(testItem);
        assertTrue(result, "Failed to update item");
        
        Item updatedItem = itemDAO.getItemById(itemId);
        assertEquals("Updated Item", updatedItem.getName(), "Name not updated");
        assertEquals("Updated Description", updatedItem.getDescription(), "Description not updated");
        assertEquals(59.99, updatedItem.getPrice(), 0.01, "Price not updated");
        assertEquals(30, updatedItem.getStockQuantity(), "Stock quantity not updated");
        assertEquals(3, updatedItem.getCategoryId(), "Category ID not updated");
        
        itemDAO.deleteItem(itemId);
    }

    @Test
    public void testDeleteItem() throws SQLException {
        System.out.println("deleteItem");
        Item testItem = new Item(0, "Test Item Delete", "Delete Description", 29.99, 10, 1, 
                LocalDate.now(), LocalDate.now());   
        int itemId = itemDAO.addItem(testItem);
        assertTrue(itemId > 0, "Failed to add test item");
        
        boolean result = itemDAO.deleteItem(itemId);
        assertTrue(result, "Failed to delete item");
        
        Item deletedItem = itemDAO.getItemById(itemId);
        assertNull(deletedItem, "Item should be deleted");
    }

    @Test
    public void testSearchItems() throws SQLException {
        System.out.println("searchItems");
        Item testItem = new Item(0, "Test Item Search", "Search Description", 79.99, 100, 2,
                LocalDate.now(), LocalDate.now());
        
        int itemId = itemDAO.addItem(testItem);
        assertTrue(itemId > 0, "Failed to add test item");
        
        Map<String, String> criteria = new HashMap<>();
        criteria.put("name", "Test Item Search");
        criteria.put("stock_min", "50");
        criteria.put("stock_max", "150");
        
        List<Item> results = itemDAO.searchItems(criteria);
        assertNotNull(results, "Search results should not be null");
        assertFalse(results.isEmpty(), "Search should return results");
        assertEquals("Test Item Search", results.get(0).getName(), "Search result name mismatch");
        
        itemDAO.deleteItem(itemId);
    }

    @Test
    public void testGetStockRangeCounts() throws SQLException {
        System.out.println("getStockRangeCounts");
        Item testItem1 = new Item(0, "Test Item Low", "Low Stock", 19.99, 25, 1, LocalDate.now(), LocalDate.now());
        Item testItem2 = new Item(0, "Test Item Medium", "Medium Stock", 39.99, 75, 1, LocalDate.now(), LocalDate.now());
        Item testItem3 = new Item(0, "Test Item High", "High Stock", 59.99, 150, 1, LocalDate.now(), LocalDate.now());
        
        int id1 = itemDAO.addItem(testItem1);
        int id2 = itemDAO.addItem(testItem2);
        int id3 = itemDAO.addItem(testItem3);
        
        Map<String, Integer> counts = itemDAO.getStockRangeCounts();
        assertNotNull(counts, "Counts should not be null");
        assertTrue(counts.containsKey("0_50"), "Should contain 0_50 range");
        assertTrue(counts.containsKey("50_100"), "Should contain 50_100 range");
        assertTrue(counts.containsKey("100_plus"), "Should contain 100_plus range");
        assertTrue(counts.get("0_50") >= 1, "Should have at least one item in 0_50 range");
        assertTrue(counts.get("50_100") >= 1, "Should have at least one item in 50_100 range");
        assertTrue(counts.get("100_plus") >= 1, "Should have at least one item in 100_plus range");
        
        itemDAO.deleteItem(id1);
        itemDAO.deleteItem(id2);
        itemDAO.deleteItem(id3);
    }
}