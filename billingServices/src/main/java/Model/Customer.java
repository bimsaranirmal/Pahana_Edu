/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import jakarta.json.bind.annotation.JsonbDateFormat;
import java.time.LocalDate;

/**
 *
 * @author BIMSARA
 */
public class Customer {
    private int id;
    private String name;
    private String gender;

    @JsonbDateFormat("yyyy-MM-dd")
    private LocalDate dob;

    private String address;
    private String nic;
    private String email;
    private String phone;
    private String accountNumber; // Added account number field
    private String status;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Customer() {
    }

    // Constructor
    public Customer(int id, String name, String gender, LocalDate dob, String address, String nic, String email, String phone, String accountNumber, String status, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.address = address;
        this.nic = nic;
        this.email = email;
        this.phone = phone;
        this.accountNumber = accountNumber; // Initialize in constructor
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAccountNumber() { return accountNumber; } // Getter for account number
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; } // Setter for account number
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}
