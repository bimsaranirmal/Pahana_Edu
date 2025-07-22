/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.billingservices.resources;

/**
 *
 * @author BIMSARA
 */

import Utils.Cashier;
import Utils.CashierDAO;
import com.google.gson.Gson;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("cashiers")
public class CashierResource {
    private final CashierDAO cashierDAO = new CashierDAO();
    private final Jsonb jsonb = JsonbBuilder.create();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCashiers() {
        try {
            List<Cashier> cashiers = cashierDAO.getAllCashiers();
            return Response.ok(jsonb.toJson(cashiers)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCashierById(@PathParam("id") int id) {
        try {
            Cashier cashier = cashierDAO.getCashierById(id);
            if (cashier != null) {
                return Response.ok(jsonb.toJson(cashier)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Cashier with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCashier(Cashier cashier, @Context UriInfo uriInfo) {
        try {
            // Phone is nullable, so it's not included in the required fields check
            if (cashier.getName() == null || cashier.getGender() == null || cashier.getDob() == null ||
                cashier.getAddress() == null || cashier.getNic() == null || cashier.getEmail() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required fields (name, gender, dob, address, nic, email) cannot be null")))
                               .build();
            }
            int id = cashierDAO.registerCashier(cashier);
            UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id));
            return Response.created(builder.build()).entity(jsonb.toJson(id)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @PUT
    @Path("{id}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveCashier(@PathParam("id") int id) {
        try {
            if (cashierDAO.approveCashier(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Cashier approved successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Cashier with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @PUT
    @Path("{id}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectCashier(@PathParam("id") int id) {
        try {
            if (cashierDAO.rejectCashier(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Cashier rejected successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Cashier with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCashier(@PathParam("id") int id, Cashier updatedCashier) {
        try {
            updatedCashier.setId(id);
            if (cashierDAO.updateCashier(updatedCashier)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Cashier updated successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Cashier with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCashier(@PathParam("id") int id) {
        try {
            if (cashierDAO.deleteCashier(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Cashier deleted successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Cashier with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchCashiers(@QueryParam("name") String name,
                                  @QueryParam("gender") String gender,
                                  @QueryParam("nic") String nic,
                                  @QueryParam("email") String email,
                                  @QueryParam("phone") String phone,
                                  @QueryParam("status") String status,
                                  @Context SecurityContext securityContext) {
        try {
            String userRole = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "anonymous";
            
            Map<String, String> criteria = new HashMap<>();
            if (name != null && !name.isEmpty()) criteria.put("name", name);
            if (gender != null && !gender.isEmpty()) criteria.put("gender", gender);
            if (nic != null && !nic.isEmpty()) criteria.put("nic", nic);
            if (email != null && !email.isEmpty()) criteria.put("email", email);
            if (phone != null && !phone.isEmpty()) criteria.put("phone", phone);
            if (status != null && !status.isEmpty()) criteria.put("status", status);
            List<Cashier> cashiers = cashierDAO.searchCashiers(criteria);
            return Response.ok(jsonb.toJson(cashiers)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginCashier(LoginRequest loginRequest) {
        try {
            Cashier cashier = cashierDAO.authenticateCashier(loginRequest.getUsername(), loginRequest.getEmail());
            if (cashier == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                               .entity(jsonb.toJson(new ErrorResponse("Invalid username or email")))
                               .build();
            }
            if (!"approved".equals(cashier.getStatus())) {
                return Response.status(Response.Status.FORBIDDEN)
                               .entity(jsonb.toJson(new ErrorResponse("Only approved cashiers can log in")))
                               .build();
            }
            return Response.ok(jsonb.toJson(cashier)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    public static class LoginRequest {
        private String username;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ErrorResponse class for structured error messages
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    // SuccessResponse class for structured success messages
    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}