package com.mycompany.billingservices.resources;

import Model.Customer;
import DAO.CustomerDAO;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RESTful resource for managing Customer entities in the Pahana Edu Online Billing System.
 *
 * @author BIMSARA
 */
@Path("customers")
public class CustomerResource {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Retrieves a list of all customers.
     *
     * @return A Response containing a JSON array of Customer objects or an error.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers() {
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            return Response.ok(jsonb.toJson(customers)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer.
     * @return A Response containing the Customer object or a NOT_FOUND/error status.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerById(@PathParam("id") int id) {
        try {
            Customer customer = customerDAO.getCustomerById(id);
            if (customer != null) {
                return Response.ok(jsonb.toJson(customer)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Customer with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Registers a new customer with an automatically generated account number.
     *
     * @param customer The Customer object to register.
     * @param uriInfo Context for building the created URI.
     * @return A Response indicating success (201 Created) with the new customer's ID, or an error.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(Customer customer, @Context UriInfo uriInfo) {
        try {
            // Validate required fields, excluding accountNumber (generated automatically)
            if (customer.getName() == null || customer.getGender() == null || customer.getDob() == null ||
                customer.getAddress() == null || customer.getNic() == null || customer.getEmail() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required fields (name, gender, dob, address, nic, email) cannot be null")))
                               .build();
            }
            int id = customerDAO.registerCustomer(customer);
            UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id));
            return Response.created(builder.build()).entity(jsonb.toJson(id)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(jsonb.toJson(new ErrorResponse("Duplicate entry for NIC or email: " + e.getMessage())))
                               .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Approves a customer by their ID.
     *
     * @param id The ID of the customer to approve.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @PUT
    @Path("{id}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveCustomer(@PathParam("id") int id) {
        try {
            if (customerDAO.approveCustomer(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Customer approved successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Customer with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Rejects a customer by their ID.
     *
     * @param id The ID of the customer to reject.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @PUT
    @Path("{id}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectCustomer(@PathParam("id") int id) {
        try {
            if (customerDAO.rejectCustomer(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Customer rejected successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Customer with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Updates an existing customer's details, excluding account number.
     *
     * @param id The ID of the customer to update.
     * @param updatedCustomer The Customer object with updated details.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomer(@PathParam("id") int id, Customer updatedCustomer) {
        try {
            updatedCustomer.setId(id);
            // Validate required fields, excluding accountNumber
            if (updatedCustomer.getName() == null || updatedCustomer.getGender() == null || updatedCustomer.getDob() == null ||
                updatedCustomer.getAddress() == null || updatedCustomer.getNic() == null || updatedCustomer.getEmail() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required fields (name, gender, dob, address, nic, email) cannot be null")))
                               .build();
            }
            if (customerDAO.updateCustomer(updatedCustomer)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Customer updated successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Customer with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(jsonb.toJson(new ErrorResponse("Duplicate entry for NIC or email: " + e.getMessage())))
                               .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Deletes a customer by their ID.
     *
     * @param id The ID of the customer to delete.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@PathParam("id") int id) {
        try {
            if (customerDAO.deleteCustomer(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Customer deleted successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Customer with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Searches for customers based on various criteria.
     *
     * @param name Optional query parameter for customer name.
     * @param gender Optional query parameter for customer gender.
     * @param nic Optional query parameter for customer NIC.
     * @param email Optional query parameter for customer email.
     * @param phone Optional query parameter for customer phone.
     * @param accountNumber Optional query parameter for customer account number.
     * @param status Optional query parameter for customer status.
     * @param securityContext Security context for user role (currently not used for filtering).
     * @return A Response containing a JSON array of matching Customer objects or an error.
     */
    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchCustomers(@QueryParam("name") String name,
                                    @QueryParam("gender") String gender,
                                    @QueryParam("nic") String nic,
                                    @QueryParam("email") String email,
                                    @QueryParam("phone") String phone,
                                    @QueryParam("accountNumber") String accountNumber,
                                    @QueryParam("status") String status,
                                    @Context SecurityContext securityContext) {
        try {
            Map<String, String> criteria = new HashMap<>();
            if (name != null && !name.isEmpty()) criteria.put("name", name);
            if (gender != null && !gender.isEmpty()) criteria.put("gender", gender);
            if (nic != null && !nic.isEmpty()) criteria.put("nic", nic);
            if (email != null && !email.isEmpty()) criteria.put("email", email);
            if (phone != null && !phone.isEmpty()) criteria.put("phone", phone);
            if (accountNumber != null && !accountNumber.isEmpty()) criteria.put("accountNumber", accountNumber);
            if (status != null && !status.isEmpty()) criteria.put("status", status);

            List<Customer> customers = customerDAO.searchCustomers(criteria);
            return Response.ok(jsonb.toJson(customers)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Handles customer login.
     *
     * @param loginRequest Contains username and email for authentication.
     * @return A Response containing the authenticated Customer object or an unauthorized/forbidden/error status.
     */
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginCustomer(LoginRequest loginRequest) {
        try {
            Customer customer = customerDAO.authenticateCustomer(loginRequest.getUsername(), loginRequest.getEmail());
            if (customer == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                               .entity(jsonb.toJson(new ErrorResponse("Invalid username or email")))
                               .build();
            }
            if (!"approved".equals(customer.getStatus())) {
                return Response.status(Response.Status.FORBIDDEN)
                               .entity(jsonb.toJson(new ErrorResponse("Only approved customers can log in")))
                               .build();
            }
            return Response.ok(jsonb.toJson(customer)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Inner class for handling login requests.
     */
    public static class LoginRequest {
        private String username;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * Inner class for structured error responses.
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * Inner class for structured success responses.
     */
    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}