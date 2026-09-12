package com.example.checkout.audit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.KogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures process lifecycle events for the Checkout process and records them
 * into {@link CheckoutAuditStore}.
 *
 * <p>Registration: Kogito picks up any CDI bean that implements
 * {@link KogitoProcessEventListener} automatically — no extra wiring needed. The
 * {@link #config()} producer exists only to guarantee the listener is created
 * eagerly at startup (otherwise it would be lazily instantiated on the first
 * event, which still works, but eager is clearer for educational purposes).
 */
@ApplicationScoped
public class CheckoutProcessEventListener implements KogitoProcessEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutProcessEventListener.class);

    @Inject
    CheckoutAuditStore store;

    /**
     * Produce a {@link DefaultProcessEventListenerConfig} that wires this
     * listener into the Kogito runtime. The config is a CDI bean, so Quarkus
     * will instantiate it and the listener at startup.
     */
    @Produces
    public DefaultProcessEventListenerConfig config() {
        return new DefaultProcessEventListenerConfig(this);
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        KogitoProcessInstance pi = cast(event);
        LOG.info("Checkout process starting: id={}", pi.getStringId());
        record(pi, "started");
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        // no-op — we already captured everything in beforeProcessStarted
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        // no-op — variables are still intact in afterProcessCompleted
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
