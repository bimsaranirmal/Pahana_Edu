package Model;

import jakarta.json.bind.annotation.JsonbDateFormat;
import java.time.LocalDate;

/**
 * Represents a Category entity for the Online Billing System.
 * Stores category details for organizing items in the bookshop inventory as per assignment requirements.
 *
 * @author BIMSARA
 */
public class Category {
    private int id;
    private String name;
    private String description;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate updatedAt;

    // Default constructor
    public Category() {
    }

    // Full constructor
    public Category(int id, String name, String description, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
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