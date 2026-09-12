package com.example.checkout.audit;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST endpoint to query the audit trail captured by
 * {@link CheckoutProcessEventListener}.
 *
 * <pre>
 *   GET /checkout-instances            → all records (newest first)
 *   GET /checkout-instances?event=completed  → only completed
 *   DELETE /checkout-instances          → clear the in-memory store
 * </pre>
 */
@Path("/checkout-instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CheckoutAuditResource {

    @Inject
    CheckoutAuditStore store;

    @GET
    public List<CheckoutAuditRecord> list(@QueryParam("event") String event) {
        if (event == null || event.isBlank()) {
            return store.list();
        }
        return store.list().stream()
                .filter(r -> r.getEvent().equalsIgnoreCase(event))
                .toList();
    }

    @GET
    @Path("/count")
    public Response count() {
        return Response.ok("{\"count\":" + store.size() + "}").build();
    }

    @DELETE
    public Response clear() {
        store.clear();
        return Response.noContent().build();
    }
}
