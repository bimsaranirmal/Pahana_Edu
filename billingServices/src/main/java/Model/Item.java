package Model;

import jakarta.json.bind.annotation.JsonbDateFormat;
import java.time.LocalDate;

/**
 * Represents an Item entity for the Pahana Edu Online Billing System.
 * Stores item details for the bookshop inventory as per assignment requirements.
 *
 * @author BIMSARA
 */
public class Item {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private Integer categoryId;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate updatedAt;

    // Default constructor
    public Item() {
    }

    // Full constructor
    public Item(int id, String name, String description, double price, int stockQuantity, Integer categoryId, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
}