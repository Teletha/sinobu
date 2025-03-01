/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * In order to reduce code size, a variety of less relevant interfaces are implemented in a single
 * class. Fields should only be initialized if they are needed in the constructor. If you initialize
 * a field at the time of its declaration, even unnecessary fields will be initialized.
 */
class Subscriber<T> implements Observer<T>, Disposable, WebSocket.Listener, Storable<Subscriber> {

    /** Generic counter. */
    volatile long index;

    /** Generic num value. */
    volatile long time;

    /** Generic object. */
    T o;

    OutputStream out;

    /**
     * {@link Subscriber} must have this constructor only. Don't use instance field initialization
     * to
     * reduce creation cost.
     */
    Subscriber() {
    }

    /** The delegation. */
    Observer observer;

    /** The delegation. */
    Consumer<? super T> next;

    /** The delegation. */
    Consumer<Throwable> error;

    /** The delegation. */
    Runnable complete;

    /** The delegation. */
    Disposable disposer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        if (disposer == null || !disposer.isDisposed()) {
            if (complete != null) {
                complete.run();
            } else if (observer != null) {
                observer.complete();
            }
        }
        if (disposer != null && index == 1) disposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable e) {
        if (disposer == null || !disposer.isDisposed()) {
            if (error != null) {
                error.accept(e);
            } else if (observer != null) {
                observer.error(e);
            } else {
                Observer.super.error(e);
            }
        }
        if (disposer != null && index == 1) disposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        // To reduce CPU computation, the termination of the event stream must be confirmed
        // by the caller of the accept method, not by the callee.
        //
        // When the callee confirms the termination, the action is to enumerate all the values and
        // ignore the ones after the termination, but if the caller confirms the termination, the
        // enumeration of the values can be interrupted immediately upon termination.
        //
        // if (disposer == null || disposer.isDisposed() == false) {
        try {
            if (next != null) {
                next.accept(value);
            } else if (observer != null) {
                observer.accept(value);
            }
        } catch (UndeclaredThrowableException e) {
            error(e.getUndeclaredThrowable());
        } catch (Throwable e) {
            error(e);
        }
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }

    /**
     * Utility to create the specific {@link Signal} for this {@link Subscriber}.
     * 
     * @return
     */
    Signal<T> signal() {
        Signaling<T> signal = new Signaling();
        observer = signal;
        return signal.expose;
    }

    // ======================================================================
    // Anonymous Disposable Instance Manager
    // ======================================================================
    private static final Map<Disposable, Subscriber> cache = new WeakHashMap();

    static synchronized Subscriber of(Disposable disposable) {
        if (disposable instanceof Subscriber) {
            return (Subscriber) disposable;
        } else {
            return cache.computeIfAbsent(disposable, k -> new Subscriber());
        }
    }

    // ======================================================================
    // Websocket Listener
    // ======================================================================
    byte[] a;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(WebSocket web) {
        web.request(Long.MAX_VALUE);
        next.accept((T) web);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onText(WebSocket web, CharSequence data, boolean last) {
        StringBuilder b = (StringBuilder) o;

        if (last) {
            // If there is a pre-buffered string, it must be concatenated.
            // If not, we can stringify directly to avoid unnecessary bytes copying.
            if (b.length() == 0) {
                observer.accept(data.toString());
            } else {
                observer.accept(b.append(data).toString());
                b.setLength(0);
            }
        } else {
            b.append(data);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onBinary(WebSocket web, ByteBuffer data, boolean last) {
        try {
            byte[] b = new byte[data.remaining()];
            data.get(b);

            if (a != null) {
                b = ByteBuffer.allocate(a.length + b.length).put(a).put(b).array();
                a = null;
            }

            if (last) {
                StringBuilder out = new StringBuilder();
                I.copy(new InputStreamReader(b[0] == 31 && b[1] == -117 ? new GZIPInputStream(new ByteArrayInputStream(b))
                        : new InflaterInputStream(new ByteArrayInputStream(b), new Inflater(true)), StandardCharsets.UTF_8), out, true);
                observer.accept(out.toString());
            } else {
                a = b;
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onClose(WebSocket web, int status, String reason) {
        if (status == 1000) {
            observer.complete();
        } else {
            observer.error(new Error(status + reason));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(WebSocket web, Throwable e) {
        observer.error(e);
    }

    // ======================================================================
    // Resource Bundle
    // ======================================================================
    // By setting the modifier to public, it is treated as a property to be saved.
    public Map<String, String> messages;

    /**
     * @param lang An associated language.
     */
    Subscriber(String lang) {
        o = (T) lang;
        messages = new ConcurrentSkipListMap();

        restore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path locate() {
        return Path.of(I.env("LangDirectory", "lang") + "/" + o + ".json");
    }
}