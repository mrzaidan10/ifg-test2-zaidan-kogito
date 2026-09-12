package com.example.checkout.audit;

import java.time.Instant;
import java.util.Map;

/**
 * Audit record for one lifecycle occurrence of a Checkout process instance.
 * Captured by {@link CheckoutProcessEventListener} and served via
 * {@link CheckoutAuditResource}.
 */
public class CheckoutAuditRecord {

    private final String instanceId;
    private final String processId;
    private final String event; // "started" or "completed"
    private final Instant timestamp;
    private final Map<String, Object> variables;

    public CheckoutAuditRecord(String instanceId, String processId, String event,
            Instant timestamp, Map<String, Object> variables) {
        this.instanceId = instanceId;
        this.processId = processId;
        this.event = event;
        this.timestamp = timestamp;
        this.variables = variables;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public String getEvent() {
        return event;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
