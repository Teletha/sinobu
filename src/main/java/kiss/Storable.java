/*
 * Copyright (C) 2019 Nameless Production Committee
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
 * @version 2018/09/07 10:21:25
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

        model.walk(object, (m, property, o) -> {
            if (property.isAttribute()) {
                signal[0] = signal[0].merge(m.observe(object, property).diff());
            } else {
                signal[0] = signal[0].merge(auto(property.model, property.model.get(o, property)));
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
        return ".preferences/" + Model.of(this).type.getName() + ".json";
    }
}
