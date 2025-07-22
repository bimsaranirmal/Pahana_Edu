package com.mycompany.billingservices.resources;

import Model.Billing;
import DAO.BillingDAO;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * RESTful resource for managing Bill entities in the Pahana Edu Online Billing System.
 * Provides endpoints for creating and retrieving bills.
 *
 * @author BIMSARA
 */
@Path("bills")
public class BillingResource {
    private final BillingDAO billingDAO = new BillingDAO();
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Creates a new bill.
     *
     * @param bill The Billing object to create.
     * @param uriInfo Context for building the created URI.
     * @return A Response indicating success (201 Created) with the new bill's ID, or an error.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBill(Billing bill, @Context UriInfo uriInfo) {
        try {
            // Validate required fields
            if (bill.getCustomerId() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Customer ID is required")))
                               .build();
            }
            if (bill.getBillItems() == null || bill.getBillItems().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("At least one bill item is required")))
                               .build();
            }
            for (Billing.BillItem billItem : bill.getBillItems()) {
                if (billItem.getItemId() <= 0 || billItem.getQuantity() <= 0 || billItem.getUnitPrice() < 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                                   .entity(jsonb.toJson(new ErrorResponse("Invalid bill item: itemId, quantity, and unitPrice must be valid")))
                                   .build();
                }
            }
            int id = billingDAO.createBill(bill);
            if (id != -1) {
                UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id));
                return Response.created(builder.build()).entity(jsonb.toJson(id)).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity(jsonb.toJson(new ErrorResponse("Failed to create bill")))
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
     * Retrieves a bill by its ID.
     *
     * @param id The ID of the bill.
     * @return A Response containing the Billing object or a NOT_FOUND/error status.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBillById(@PathParam("id") int id) {
        try {
            Billing bill = billingDAO.getBillById(id);
            if (bill != null) {
                return Response.ok(jsonb.toJson(bill)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Bill with ID " + id + " not found")))
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
     * Retrieves all bills for a customer.
     *
     * @param customerId The ID of the customer.
     * @return A Response containing a JSON array of Billing objects or an error.
     */
    @GET
    @Path("customer/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBillsByCustomerId(@PathParam("customerId") int customerId) {
        try {
            List<Billing> bills = billingDAO.getBillsByCustomerId(customerId);
            return Response.ok(jsonb.toJson(bills)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Retrieves monthly billing statistics and total billing amount.
     *
     * @return A Response containing a JSON object with monthly statistics and total billing amount.
     */
    @GET
    @Path("statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBillingStatistics() {
        try {
            Map<String, Object> statistics = billingDAO.getBillingStatistics();
            return Response.ok(jsonb.toJson(statistics)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }
    
    @GET
    @Path("monthly-details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyBillDetails() {
        try {
            Map<String, List<Map<String, Object>>> monthlyBills = billingDAO.getMonthlyBillDetails();
            return Response.ok(jsonb.toJson(monthlyBills)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
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