/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.nio.file.WatchEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import kiss.model.Model;
import kiss.model.Property;

/**
 * <p>
 * Versatile wrapper or delegator.
 * </p>
 * 
 * @version 2014/02/03 11:19:06
 */
class Agent<T> implements Observer<T>, WatchEvent, Codec<Date> {

    /** For reuse. */
    T object;

    /**
     * {@link Agent} must have this constructor only. Dont use instance field initialization to
     * reduce creation cost.
     */
    Agent() {
    }

    // ============================================================
    // For Observer
    // ============================================================

    /** The delegation. */
    Observer observer;

    /** The delegation. */
    Consumer<T> next;

    /** The delegation. */
    Consumer<Throwable> error;

    /** The delegation. */
    Runnable complete;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompleted() {
        if (complete != null) {
            complete.run();
        } else if (observer != null) {
            observer.onCompleted();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Throwable e) {
        if (error != null) {
            error.accept(e);
        } else if (observer != null) {
            observer.onError(e);
        } else {
            Thread.currentThread().getThreadGroup().uncaughtException(null, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNext(T value) {
        if (next != null) {
            next.accept(value);
        } else if (observer != null) {
            observer.onNext(value);
        }
    }

    // ============================================================
    // For WatchEvent
    // ============================================================

    /** The event holder. */
    WatchEvent watch;

    /**
     * {@inheritDoc}
     */
    @Override
    public Kind kind() {
        return watch.kind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        return watch.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T context() {
        return object;
    }

    // ============================================================
    // For XML Desirialization and Bean Conversion
    // ============================================================

    /** The current model. */
    Model model;

    /** The property for xml deserialization process. */
    Property property;

    /** The current location for deserialization process. */
    int index;

    // ============================================================
    // For XML Desirialization and Bean Conversion
    // ============================================================

    /**
     * The date format for W3CDTF. Date formats are not synchronized. It is recommended to create
     * separate format instances for each thread. If multiple threads access a format concurrently,
     * it must be synchronized externally.
     */
    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * {@inheritDoc}
     */
    @Override
    public Date decode(String value) {
        try {
            return format.parse(value);
        } catch (Exception e) {
            throw I.quiet(e);
        }
        // return Date.from(LocalDateTime.parse(value).toInstant(ZoneOffset.UTC));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Date value) {
        return format.format(value);
        // return LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC).toString();
    }

}