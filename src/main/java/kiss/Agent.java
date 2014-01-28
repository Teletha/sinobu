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

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @version 2014/01/28 23:05:39
 */
class Agent<V> implements Observer<V>, Disposable {

    /** The delegation. */
    Observer<? super V> observer;

    /** The delegation. */
    Consumer<? super V> next;

    /** The delegation. */
    Consumer<Throwable> error;

    /** The delegation. */
    Runnable complete;

    /** The container of unsubscribers. */
    private ArrayList<Disposable> list = new ArrayList();

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
    public void onNext(V value) {
        if (next != null) {
            next.accept(value);
        } else if (observer != null) {
            observer.onNext(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        for (Disposable disposable : list) {
            disposable.dispose();
        }
        list.clear();
    }

    /**
     * <p>
     * Aggregate {@link Disposable} into this instance.
     * </p>
     * 
     * @param disposable A target to dispose.
     * @return Chainable API.
     */
    public Agent<V> and(Disposable disposable) {
        list.add(disposable);

        // API definition
        return this;
    }
}