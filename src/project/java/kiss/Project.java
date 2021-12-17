/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static bee.api.License.MIT;

import javax.lang.model.SourceVersion;

import bee.task.Jar;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "sinobu", ref("version.txt"));
        license(MIT);
        describe("""
                Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.

                This library aims to simplify and highly condense the functions related to domains that are frequently encountered in real-world development projects, making them easier to use.
                * Dependency Injection
                * Object lifecycle management
                * JavaBeans-like property based type modeling
                * HTTP(S)
                * JSON
                * HTML(XML)
                * Reactive Programming (Rx)
                * Asynchronous processing
                * Parallel processing
                * Multilingualization
                * Template Engine (Mustache)
                * Dynamic plug-in mechanism
                * Domain specific languages
                * Object Persistence
                * Logging

                With a few exceptions, Sinobu and its APIs are designed to be simple to use and easy to understand by adhering to the following principles.
                * Keep it stupid simple
                * Less is more
                * Type safety
                * Refactoring safety
                """);

        require(SourceVersion.latest(), SourceVersion.RELEASE_11, SourceVersion.latest());
        require("com.github.teletha", "antibug").atTest();
        require("com.pgs-soft", "HttpClientMock").atTest();
        require("io.reactivex.rxjava3", "rxjava").atTest();

        // For JSON benchmark
        require("com.fasterxml.jackson.core", "jackson-databind").atTest();
        require("com.google.code.gson", "gson").atTest();
        require("com.alibaba", "fastjson").atTest();

        // For logging benchmark
        require("org.apache.logging.log4j", "log4j-core").atTest();
        require("com.lmax", "disruptor").atTest();
        require("org.tinylog", "tinylog-impl").atTest();
        require("ch.qos.logback", "logback-classic").atTest();
        require("org.slf4j", "slf4j-nop").atTest();

        versionControlSystem("https://github.com/teletha/sinobu");

        // Task Settings
        Jar.SkipDebugInfo = true;
        Jar.SkipTraceInfo = false;
    }
}