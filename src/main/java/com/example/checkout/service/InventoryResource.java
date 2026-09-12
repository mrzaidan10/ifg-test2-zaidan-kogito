package com.example.checkout.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @Inject
    InventoryService inventoryService;

    @GET
    public Map<String, Object> getAll() {
        return Map.of("message", "Use /inventory/{cartId} to check specific stock");
    }

    @GET
    @Path("/{cartId}")
    public Response getStock(@PathParam("cartId") String cartId) {
        int stock = inventoryService.getStock(cartId);
        return Response.ok("{\"" + cartId + "\":" + stock + "}").build();
    }
}
