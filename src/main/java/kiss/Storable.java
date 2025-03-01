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

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.TimeUnit.*;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * There are two main ways to change the default save location. One is specified by overriding the
 * {@link #locate()} method. The other is to specify a value for "PreferenceDirectory" in global
 * application environment (see {@link I#env(String)}).
 */
public interface Storable<Self> {

    /**
     * Restore all properties from persistence domain.
     * 
     * @return Chainable API.
     */
    default Self restore() {
        synchronized (getClass()) {
            try {
                I.json(Files.newBufferedReader(locate())).as(this);
            } catch (Throwable e) {
                // ignore error
            }
            return (Self) this;
        }
    }

    /**
     * Store all properties to persistence domain.
     * 
     * @return Chainable API.
     */
    default Self store() {
        synchronized (getClass()) {
            try {
                Path file = locate();
                Path tmp = Files.createTempFile(Files.createDirectories(file.getParent()), file.getFileName().toString(), null);

                try (FileChannel c = FileChannel.open(file.resolveSibling(file.getFileName() + ".lock"), CREATE, WRITE, DELETE_ON_CLOSE)) {
                    c.lock();
                    I.write(this, Files.newBufferedWriter(tmp));

                    // From Javadoc
                    // The move is performed as an atomic file system operation and all other
                    // options are ignored. If the target file exists then it is implementation
                    // specific if the existing file is replaced or this method fails by throwing an
                    // IOException. If the move cannot be performed as an atomic file system
                    // operation then AtomicMoveNotSupportedException is thrown. This can arise, for
                    // example, when the target location is on a different FileStore and would
                    // require that the file be copied, or target location is associated with a
                    // different provider to this object.
                    //
                    // But some implementation (e.g. jimfs) throws FileAlreadyExistsException,
                    // so we should remain that option
                    Files.move(tmp, file, ATOMIC_MOVE, REPLACE_EXISTING);
                }
            } catch (Throwable e) {
                // ignore
                I.error(e);
            }
            return (Self) this;
        }
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
            if (p.model.atomic) {
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
    default Path locate() {
        return Path.of(I.env("PreferenceDirectory", ".preferences") + "/" + Model.of(this).type.getName() + ".json");
    }
}