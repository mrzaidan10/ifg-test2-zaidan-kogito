package com.example.checkout.audit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.KogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures process lifecycle events for the Checkout process and records them
 * into {@link CheckoutAuditStore}.
 *
 * <p>Registration: Kogito auto-discovers any CDI bean that implements
 * {@link KogitoProcessEventListener} — no extra wiring needed.
 */
@ApplicationScoped
public class CheckoutProcessEventListener implements KogitoProcessEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutProcessEventListener.class);

    @Inject
    CheckoutAuditStore store;

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        KogitoProcessInstance pi = cast(event);
        LOG.info("Checkout process starting: id={}", pi.getStringId());
        record(pi, "started");
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        // no-op
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        // no-op
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        KogitoProcessInstance pi = cast(event);
        LOG.info("Checkout process completed: id={}", pi.getStringId());
        record(pi, "completed");
    }

    @Override
    public void beforeNodeTriggered(org.kie.api.event.process.ProcessNodeTriggeredEvent event) {
        // not needed for audit
    }

    @Override
    public void afterNodeTriggered(org.kie.api.event.process.ProcessNodeTriggeredEvent event) {
        // not needed for audit
    }

    @Override
    public void beforeNodeLeft(org.kie.api.event.process.ProcessNodeLeftEvent event) {
        // not needed for audit
    }

    @Override
    public void afterNodeLeft(org.kie.api.event.process.ProcessNodeLeftEvent event) {
        // not needed for audit
    }

    @Override
    public void beforeVariableChanged(org.kie.api.event.process.ProcessVariableChangedEvent event) {
        // not needed for audit
    }

    @Override
    public void afterVariableChanged(org.kie.api.event.process.ProcessVariableChangedEvent event) {
        // not needed for audit
    }

    // ---- helpers ----------------------------------------------------------

    private void record(KogitoProcessInstance pi, String eventName) {
        Map<String, Object> vars = new HashMap<>();
        if (pi.getVariables() != null) {
            vars.putAll(pi.getVariables());
        }
        store.add(new CheckoutAuditRecord(
                pi.getStringId(),
                pi.getProcessId(),
                eventName,
                Instant.now(),
                vars));
    }

    private static KogitoProcessInstance cast(org.kie.api.event.process.ProcessEvent event) {
        return (KogitoProcessInstance) event.getProcessInstance();
    }
}