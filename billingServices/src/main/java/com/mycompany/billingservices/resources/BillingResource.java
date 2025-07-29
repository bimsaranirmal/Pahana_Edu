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
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

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

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "bimsaranirmal123@gmail.com"; 
    private static final String SMTP_PASSWORD = "sbocjncbzyujbkun"; 

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

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    
    @POST
    @Path("{id}/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendBill(@PathParam("id") int billId, Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(jsonb.toJson(new ErrorResponse("Email address is required")))
                        .build();
            }

            Map<String, Object> billContent = billingDAO.getBillContentForSending(billId);
            sendEmail(email, billContent);

            return Response.ok(jsonb.toJson(new SuccessResponse("Bill sent successfully"))).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                    .build();
        } catch (MessagingException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(jsonb.toJson(new ErrorResponse("Failed to send email: " + e.getMessage())))
                    .build();
        }
    }

    private void sendEmail(String toEmail, Map<String, Object> billContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true"); // Enforce TLS
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"); // Specify TLS versions
        props.put("mail.smtp.ssl.trust", SMTP_HOST); // Trust Gmail's SMTP server
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your Bill from Pahana Edu - Bill No: " + billContent.get("billNo"));

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h2>Pahana Edu - Bill</h2>")
                .append("<p><strong>Bill No:</strong> ").append(billContent.get("billNo")).append("</p>")
                .append("<p><strong>Bill ID:</strong> ").append(billContent.get("billId")).append("</p>")
                .append("<p><strong>Customer:</strong> ").append(billContent.get("customerName")).append("</p>")
                .append("<p><strong>Total Amount:</strong> LKR ").append(String.format("%.2f", billContent.get("totalAmount"))).append("</p>")
                .append("<p><strong>Created At:</strong> ").append(billContent.get("createdAt")).append("</p>")
                .append("<h4>Items</h4>")
                .append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
                .append("<tr><th>Item</th><th>Unit Price</th><th>Quantity</th><th>Subtotal</th></tr>");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> billItems = (List<Map<String, Object>>) billContent.get("billItems");
        for (Map<String, Object> item : billItems) {
            emailContent.append("<tr>")
                    .append("<td>").append(item.get("itemName")).append("</td>")
                    .append("<td>LKR ").append(String.format("%.2f", item.get("unitPrice"))).append("</td>")
                    .append("<td>").append(item.get("quantity")).append("</td>")
                    .append("<td>LKR ").append(String.format("%.2f", item.get("subtotal"))).append("</td>")
                    .append("</tr>");
        }
        emailContent.append("</table>")
                .append("<p style='text-align: right;'><strong>Total: LKR ").append(String.format("%.2f", billContent.get("totalAmount"))).append("</strong></p>")
                .append("<p style='text-align: center;'>Thank you for your business!</p>");

        message.setContent(emailContent.toString(), "text/html; charset=utf-8");
        Transport.send(message);
    }
}
