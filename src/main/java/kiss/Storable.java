/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import kiss.model.Model;

/**
 * There are two main ways to change the default save location. One is specified by overriding the
 * {@link #locate()} method. The other is to specify a value for "PreferenceDirectory" in global
 * application environment (see {@link I#env(String)}).
 */
public interface Storable<Self> {

    /**
     * <p>
     * Restore all properties from persistence domain.
     * </p>
     * 
     * @return Chainable API.
     */
    default Self restore() {
        try {
            I.read(Files.newBufferedReader(Paths.get(locate())), this);
        } catch (Throwable e) {
            // ignore error
        }
        return (Self) this;
    }

    /**
     * <p>
     * Store all properties to persistence domain.
     * </p>
     * 
     * @return Chainable API.
     */
    default Self store() {
        try {
            Path path = Paths.get(locate());

            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
            }
            I.write(this, Files.newBufferedWriter(path));
        } catch (Throwable e) {
            // ignore error
        }
        return (Self) this;
    }

    /**
     * Make this {@link Storable} save automatically.
     * 
     * @return Call {@link Disposable#dispose()} to stop automatic save.
     */
    default Disposable auto() {
        return auto(timing -> timing.debounce(1, SECONDS));
    }

    /**
     * Make this {@link Storable} save automatically.
     * 
     * @return Call {@link Disposable#dispose()} to stop automatic save.
     */
    default Disposable auto(Function<Signal, Signal> timing) {
        synchronized (this) {
            // dispose previous saver
            Disposable disposer = I.autosaver.get(this);
            if (disposer != null) disposer.dispose();

            // build new saver and store it
            disposer = timing.apply(auto(Model.of(this), this)).to(this::store);
            I.autosaver.put(this, disposer);

            // API definition
            return disposer;
        }
    }

    /**
     * Search autosavable {@link Variable} property.
     * 
     * @param model
     * @param object
     */
    private Signal auto(Model<Object> model, Object object) {
        Signal[] signal = {Signal.never()};

        model.walk(object, (m, p, o) -> {
            if (p.isAttribute()) {
                signal[0] = signal[0].merge(m.observe(object, p).diff());
            } else {
                signal[0] = signal[0].merge(auto(p.model, o));
            }
        });
        return signal[0];
    }

    /**
     * <p>
     * Specify the identifier of persistence location.
     * </p>
     * 
     * @return An identifier of persistence location.
     */
    default String locate() {
        return I.env("PreferenceDirectory", ".preferences") + "/" + Model.of(this).type.getName() + ".json";
    }
}