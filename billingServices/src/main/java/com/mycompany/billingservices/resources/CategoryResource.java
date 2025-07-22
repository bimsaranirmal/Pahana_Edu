package com.mycompany.billingservices.resources;

import Model.Category;
import DAO.CategoryDAO;
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
 * RESTful resource for managing Category entities in the Pahana Edu Online Billing System.
 * Provides endpoints for CRUD operations and searching categories.
 *
 * @author BIMSARA
 */
@Path("categories")
public class CategoryResource {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Retrieves a list of all categories.
     *
     * @return A Response containing a JSON array of Category objects or an error.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            return Response.ok(jsonb.toJson(categories)).build();
        } catch (SQLException e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Retrieves a category by its ID.
     *
     * @param id The ID of the category.
     * @return A Response containing the Category object or a NOT_FOUND/error status.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoryById(@PathParam("id") int id) {
        try {
            Category category = categoryDAO.getCategoryById(id);
            if (category != null) {
                return Response.ok(jsonb.toJson(category)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Category with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Adds a new category.
     *
     * @param category The Category object to add.
     * @param uriInfo Context for building the created URI.
     * @return A Response indicating success (201 Created) with the new category's ID, or an error.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(Category category, @Context UriInfo uriInfo) {
        try {
            // Validate required fields
            if (category.getName() == null || category.getName().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required field 'name' cannot be null or empty")))
                               .build();
            }
            int id = categoryDAO.addCategory(category);
            if (id != -1) {
                UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id));
                return Response.created(builder.build()).entity(jsonb.toJson(id)).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity(jsonb.toJson(new ErrorResponse("Failed to add category")))
                               .build();
            }
        } catch (SQLException e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            // Handle specific SQL errors, e.g., duplicate category name
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(jsonb.toJson(new ErrorResponse("Duplicate entry for category name: " + e.getMessage())))
                               .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Updates an existing category's details.
     *
     * @param id The ID of the category to update.
     * @param updatedCategory The Category object with updated details.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(@PathParam("id") int id, Category updatedCategory) {
        try {
            // Ensure the ID from the path matches the ID in the object for consistency
            updatedCategory.setId(id);
            // Validate required fields
            if (updatedCategory.getName() == null || updatedCategory.getName().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(jsonb.toJson(new ErrorResponse("Required field 'name' cannot be null or empty for update")))
                               .build();
            }
            if (categoryDAO.updateCategory(updatedCategory)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Category updated successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Category with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            // Handle specific SQL errors, e.g., duplicate category name
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(jsonb.toJson(new ErrorResponse("Duplicate entry for category name during update: " + e.getMessage())))
                               .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id The ID of the category to delete.
     * @return A Response indicating success or a NOT_FOUND/error status.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@PathParam("id") int id) {
        try {
            if (categoryDAO.deleteCategory(id)) {
                return Response.ok(jsonb.toJson(new SuccessResponse("Category deleted successfully"))).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(jsonb.toJson(new ErrorResponse("Category with ID " + id + " not found")))
                               .build();
            }
        } catch (SQLException e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(jsonb.toJson(new ErrorResponse("Database error: " + e.getMessage())))
                           .build();
        }
    }

    /**
     * Searches for categories based on various criteria.
     *
     * @param name Optional query parameter for category name.
     * @param description Optional query parameter for category description.
     * @return A Response containing a JSON array of matching Category objects or an error.
     */
    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchCategories(@QueryParam("name") String name,
                                    @QueryParam("description") String description) {
        try {
            Map<String, String> criteria = new HashMap<>();
            if (name != null && !name.isEmpty()) criteria.put("name", name);
            if (description != null && !description.isEmpty()) criteria.put("description", description);

            List<Category> categories = categoryDAO.searchCategories(criteria);
            return Response.ok(jsonb.toJson(categories)).build();
        } catch (SQLException e) {
            // Log the exception for debugging purposes
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