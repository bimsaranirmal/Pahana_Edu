
package DAO;

import Model.Category;
import DAO.CategoryDAO;
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

public class CategoryDAOTests {
    
    private CategoryDAO categoryDAO;

    public CategoryDAOTests() {
    }
    
    @BeforeEach
    public void setUp() {
        categoryDAO = new CategoryDAO();
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category category : categories) {
            if (category.getName().startsWith("Test Category")) {
                categoryDAO.deleteCategory(category.getId());
            }
        }
    }

    @Test
    public void testAddAndGetCategory() throws SQLException {
        Category testCategory = new Category(0, "Test Category", "Test Description", LocalDate.now(), LocalDate.now());
        
        int categoryId = categoryDAO.addCategory(testCategory);
        assertTrue(categoryId > 0, "Failed to add test category");
        
        Category result = categoryDAO.getCategoryById(categoryId);
        assertNotNull(result, "Category not found");
        assertEquals(testCategory.getName(), result.getName(), "Name mismatch");
        assertEquals(testCategory.getDescription(), result.getDescription(), "Description mismatch");
        
        boolean deleted = categoryDAO.deleteCategory(categoryId);
        assertTrue(deleted, "Failed to delete test category");
        
        result = categoryDAO.getCategoryById(categoryId);
        assertNull(result, "Category should be deleted");
    }

    @Test
    public void testGetAllCategories() throws SQLException {
        System.out.println("getAllCategories");
        List<Category> result = categoryDAO.getAllCategories();
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() >= 0, "Result size should be non-negative");
    }

    @Test
    public void testUpdateCategory() throws SQLException {
        System.out.println("updateCategory");
        Category testCategory = new Category(0, "Test Category Update", "Original Description", LocalDate.now(), LocalDate.now());
        
        int categoryId = categoryDAO.addCategory(testCategory);
        assertTrue(categoryId > 0, "Failed to add test category");
        
        testCategory.setId(categoryId);
        testCategory.setName("Updated Category");
        testCategory.setDescription("Updated Description");
        
        boolean result = categoryDAO.updateCategory(testCategory);
        assertTrue(result, "Failed to update category");
        
        Category updatedCategory = categoryDAO.getCategoryById(categoryId);
        assertEquals("Updated Category", updatedCategory.getName(), "Name not updated");
        assertEquals("Updated Description", updatedCategory.getDescription(), "Description not updated");
        
        categoryDAO.deleteCategory(categoryId);
    }

    @Test
    public void testDeleteCategory() throws SQLException {
        System.out.println("deleteCategory");
        Category testCategory = new Category(0, "Test Category Delete", "Delete Description", LocalDate.now(), LocalDate.now());
        
        int categoryId = categoryDAO.addCategory(testCategory);
        assertTrue(categoryId > 0, "Failed to add test category");
        
        boolean result = categoryDAO.deleteCategory(categoryId);
        assertTrue(result, "Failed to delete category");
        
        Category deletedCategory = categoryDAO.getCategoryById(categoryId);
        assertNull(deletedCategory, "Category should be deleted");
    }

    @Test
    public void testSearchCategories() throws SQLException {
        System.out.println("searchCategories");
        Category testCategory = new Category(0, "Test Category Search", "Search Description", LocalDate.now(), LocalDate.now());
        
        int categoryId = categoryDAO.addCategory(testCategory);
        assertTrue(categoryId > 0, "Failed to add test category");
        
        Map<String, String> criteria = new HashMap<>();
        criteria.put("name", "Test Category Search");
        criteria.put("description", "Search Description");
        
        List<Category> results = categoryDAO.searchCategories(criteria);
        assertNotNull(results, "Search results should not be null");
        assertFalse(results.isEmpty(), "Search should return results");
        assertEquals("Test Category Search", results.get(0).getName(), "Search result name mismatch");
        
        categoryDAO.deleteCategory(categoryId);
    }
}
