package com.mycompany.billingservices.resources;

import Model.Item;
import DAO.ItemDAO;
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
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RESTful resource for managing Item entities in the Pahana Edu Online Billing System.
 * Provides endpoints for CRUD operations, searching items, and fetching stock range counts.
 *
 * @author BIMSARA
 */
@Path("items")
public class ItemResource {
    private final ItemDAO itemDAO = new ItemDAO();
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Retrieves a list of all items.
     *
     * @return A Response containing a JSON array of Item objects or an error.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllItems() {
        try {
            List<Item> items = itemDAO.getAllItems();
            return Response.ok(jsonb.toJson(items)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id The ID of the item.
     * @return A Response containing the Item object or a NOT_FOUND/error status.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemById(@PathParam("id") int id) {
        try {
            Item item = itemDAO.getItemById(id);
            if (item != null) {
                return Response.ok(jsonb.toJson(item)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Item with ID " + id + " not found")))
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
     * Adds a new item.
     *
     * @param item The Item object to add.
     * @param uriInfo Context for building the created URI.
     * @return A Response indicating success (201 Created) with the new item's ID, or an error.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addItem(Item item, @Context UriInfo uriInfo) {
        try {
            // Validate required fields
            if (item.getName() == null || item.getName().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required field 'name' cannot be null or empty")))
                               .build();
            }
            if (item.getPrice() < 0 || item.getStockQuantity() < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Price and stock quantity must be non-negative")))
                               .build();
            }
            int id = itemDAO.addItem(item);
            if (id != -1) {
                UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id));
                return Response.created(builder.build()).entity(jsonb.toJson(id)).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity(jsonb.toJson(new ErrorResponse("Failed to add item")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(jsonb.toJson(new ErrorResponse("Duplicate entry for item name: " + e.getMessage())))
                               .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Updates an existing item's details.
     *
     * @param id The ID of the item to update.
     * @param updatedItem The Item object with updated details.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateItem(@PathParam("id") int id, Item updatedItem) {
        try {
            updatedItem.setId(id);
            if (updatedItem.getName() == null || updatedItem.getName().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required field 'name' cannot be null or empty for update")))
                               .build();
            }
            if (updatedItem.getPrice() < 0 || updatedItem.getStockQuantity() < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Price and stock quantity must be non-negative for update")))
                               .build();
            }
            if (itemDAO.updateItem(updatedItem)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Item updated successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Item with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(jsonb.toJson(new ErrorResponse("Duplicate entry for item name during update: " + e.getMessage())))
                               .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Deletes an item by its ID.
     *
     * @param id The ID of the item to delete.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteItem(@PathParam("id") int id) {
        try {
            if (itemDAO.deleteItem(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Item deleted successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Item with ID " + id + " not found")))
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
     * Searches for items based on various criteria.
     *
     * @param name Optional query parameter for item name.
     * @param description Optional query parameter for item description.
     * @param categoryId Optional query parameter for item category ID.
     * @param stockMin Optional query parameter for minimum stock quantity.
     * @param stockMax Optional query parameter for maximum stock quantity.
     * @return A Response containing a JSON array of matching Item objects or an error.
     */
    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchItems(@QueryParam("name") String name,
                               @QueryParam("description") String description,
                               @QueryParam("categoryId") String categoryId,
                               @QueryParam("stockMin") Integer stockMin,
                               @QueryParam("stockMax") Integer stockMax) {
        try {
            Map<String, String> criteria = new HashMap<>();
            if (name != null && !name.isEmpty()) criteria.put("name", name);
            if (description != null && !description.isEmpty()) criteria.put("description", description);
            if (categoryId != null && !categoryId.isEmpty()) criteria.put("category_id", categoryId);
            if (stockMin != null) criteria.put("stock_min", String.valueOf(stockMin));
            if (stockMax != null) criteria.put("stock_max", String.valueOf(stockMax));

            List<Item> items = itemDAO.searchItems(criteria);
            return Response.ok(jsonb.toJson(items)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Retrieves counts of items in specified stock ranges.
     *
     * @return A Response containing a JSON object with counts for stock ranges (0-50, 50-100, 100+).
     */
    @GET
    @Path("stock-counts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStockRangeCounts() {
        try {
            Map<String, Integer> counts = itemDAO.getStockRangeCounts();
            return Response.ok(jsonb.toJson(counts)).build();
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