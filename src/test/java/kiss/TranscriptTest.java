/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.CleanRoom;

@Execution(ExecutionMode.SAME_THREAD)
class TranscriptTest {

    static String originalLanguage;

    @RegisterExtension
    final CleanRoom room = new CleanRoom();

    @BeforeEach
    private void initialize() {
        Transcript.Lang.set("en");
        I.envy("LangDirectory", room.locateDirectory("transcript").toAbsolutePath().toString());
    }

    @BeforeAll
    static void startup() {
        originalLanguage = Transcript.Lang.v;
    }

    @AfterAll
    static void cleanup() {
        Transcript.Lang.set(originalLanguage);
    }

    /**
     * Create bundle dynamically.
     * 
     * @param lang
     * @param base
     * @param translated
     */
    private void createBundle(String lang, String base, String translated) {
        Bundle bundle = new Bundle(lang);
        bundle.put(base, translated);
        bundle.store();
    }

    /**
     * Wait for online translation result.
     * 
     * @param text
     */
    private void waitForTranslation(Transcript text) {
        try {
            CompletableFuture future = new CompletableFuture();
            text.observe().to(future::complete);
            future.get();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Wait for online translation result.
     * 
     * @param text
     */
    private void waitForTranslationTo(String lang, Transcript text) {
        try {
            CompletableFuture future = new CompletableFuture();
            text.observe().to(future::complete);

            Transcript.Lang.set(lang);
            future.get();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    @Test
    void base() {
        Transcript text = new Transcript("test");
        assert text.is("test");
    }

    @Test
    void nullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> new Transcript((String) null));
    }

    @Test
    void context() {
        Transcript text = new Transcript("You can use {0}.", "context");
        assert text.is("You can use context.");
    }

    @Test
    void contexts() {
        Transcript text = new Transcript("You can {1} {0}.", "context", "use");
        assert text.is("You can use context.");
    }

    @Test
    void translateByBundle() {
        createBundle("fr", "base", "nombre d'unités");
        createBundle("ja", "base", "基数");

        Transcript text = new Transcript("base");
        assert text.is("base");

        Transcript.Lang.set("fr");
        assert text.is("nombre d'unités");

        Transcript.Lang.set("ja");
        assert text.is("基数");
    }

    @Test
    void translateByOnline() {
        Transcript text = new Transcript("Water");
        assert text.is("Water");

        // Immediately after the language change,
        // it has not yet been translated due to network usage.
        Transcript.Lang.set("de");
        assert text.is("Water");

        // It will be reflected when the translation results are available.
        waitForTranslation(text);
        assert text.is("Wasser");

        // Immediately after the language change,
        // it has not yet been translated due to network usage.
        Transcript.Lang.set("ja");
        assert text.is("Wasser");

        // It will be reflected when the translation results are available.
        waitForTranslation(text);
        assert text.is("水");
    }

    @Test
    void testName() {
        I.http(HttpRequest.newBuilder()
                .uri(URI.create("https://www.ibm.com/demos/live/watson-language-translator/api/translate/text"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(BodyPublishers.ofString("{\"text\":\"" + "translate on\r\nruntime"
                        .replaceAll("\\n|\\r\\n|\\r", " ") + "\",\"source\":\"en\",\"target\":\"" + "ja" + "\"}")), String.class)
                .to(e -> {
                    System.out.println(e);
                });
    }
}
