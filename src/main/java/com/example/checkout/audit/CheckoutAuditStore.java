package com.example.checkout.audit;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory store for {@link CheckoutAuditRecord} entries. Application-scoped
 * so it survives across requests within the same running Quarkus instance.
 *
 * NOTE: this is intentionally simple (in-memory, no persistence). Entries live
 * only while the JVM is up. For a production system you would back this with a
 * database or the Kogito Data Index service instead.
 */
@ApplicationScoped
public class CheckoutAuditStore {

    private final List<CheckoutAuditRecord> records = new ArrayList<>();

    public void add(CheckoutAuditRecord record) {
        records.add(record);
    }

    /** Returns an immutable snapshot, newest first. */
    public List<CheckoutAuditRecord> list() {
        List<CheckoutAuditRecord> snapshot = new ArrayList<>(records);
        Collections.reverse(snapshot);
        return Collections.unmodifiableList(snapshot);
    }

    public int size() {
        return records.size();
    }

    public void clear() {
        records.clear();
    }
}
