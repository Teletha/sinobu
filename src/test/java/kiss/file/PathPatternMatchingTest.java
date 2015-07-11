/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import java.util.function.Consumer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;
import kiss.I;

/**
 * @version 2015/07/11 21:39:57
 */
public class PathPatternMatchingTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom();

    private void structure1(FileSystemDSL $) {
        $.dir("directory1", () -> {
            $.file("01.file");
            $.file("01.txt");
            $.file("02.txt");
        });
        $.dir("directory2", () -> {
            $.file("01.file");
            $.file("01.txt");
            $.file("02.txt");
        });
        $.file("01.file");
        $.file("01.txt");
        $.file("02.txt");
    }

    private void structure2(FileSystemDSL $) {
        $.dir("directory1", () -> {
            $.dir("child1", () -> {
                $.dir("decendant1", () -> {
                    $.dir("lowest", () -> {
                        $.file("01.txt");
                    });
                });
                $.dir("decendant2", () -> {
                    $.file("01.txt");
                });
            });

            $.dir("child2", () -> {
                $.file("01.txt");
            });

            $.file("01.txt");
            $.file("02.txt");
        });
        $.dir("directory2", () -> {
            $.dir("child1", () -> {
                $.file("01.txt");
            });

            $.dir("child2", () -> {
                $.file("02.txt");
            });
        });
    }

    @Test
    public void all() {
        assertCount(9);
    }

    @Test
    public void descendant() {
        assertCount(9, "**");
    }

    @Test
    public void includeWildcardLeft() {
        assertCount(1, "*.file");
    }

    @Test
    public void includeWildcardsLeft() {
        assertCount(3, "**.file");
    }

    @Test
    public void includeWildcardRight() {
        assertCount(3, "0*");
    }

    @Test
    public void includeWildcardsRight() {
        assertCount(6, "d**");
    }

    @Test
    public void includeWildcardBoth() {
        assertCount(2, "*1*");
    }

    @Test
    public void includeWildcardsBoth() {
        assertCount(7, "**1**");
    }

    @Test
    public void excludeWildcardLeft() {
        assertCount(8, "!*.file");
    }

    @Test
    public void excludeWildcardsLeft() {
        assertCount(6, "!**.file");
    }

    @Test
    public void excludeWildcardRight() {
        assertCount(6, "!0*");
    }

    @Test
    public void excludeWildcardsRight() {
        assertCount(3, "!d**");
    }

    @Test
    public void excludeWildcardBoth() {
        assertCount(7, "!*1*");
    }

    @Test
    public void excludeWildcardsBoth() {
        assertCount(2, "!**1**");
    }

    @Test
    public void multiple() {
        assertCount(2, "**.txt", "!directory**");
    }

    @Test
    public void deep() {
        assertCount(3, "directory1/**");
    }

    @Test
    public void wildcardOnly() {
        assertCount(3, "*");
    }

    @Test
    public void wildcardSecondLevel() {
        assertCount(6, "*/*");
    }

    @Test
    public void excludeDirectory() {
        assertCount(6, "!directory1/**");
    }

    @Test
    public void directory1() {
        assertDirectoryCount(3, this::structure1);
    }

    @Test
    public void directory2() {
        assertDirectoryCount(10, this::structure2);
    }

    @Test
    public void directoryPatternTopLevelPartialWildcard() {
        assertDirectoryCount(2, this::structure2, "directory*");
    }

    @Test
    public void directoryPatternTopLevelWildcard() {
        assertDirectoryCount(2, this::structure2, "*");
    }

    @Test
    public void directoryPatternWildcard() {
        assertDirectoryCount(9, this::structure2, "**");
    }

    @Test
    public void directoryPattern() {
        assertDirectoryCount(1, this::structure2, "directory1");
    }

    @Test
    public void directoryPatternDeepWildcard() {
        assertDirectoryCount(2, this::structure2, "**/child1");
    }

    @Test
    public void directoryPatternWildcardRight() {
        assertDirectoryCount(2, this::structure2, "**/d*");
    }

    @Test
    public void directoryPatternWildcardLeft() {
        assertDirectoryCount(3, this::structure2, "**/*2");
    }

    @Test
    public void directoryPatternWildcardBoth() {
        assertDirectoryCount(3, this::structure2, "**/*e*");
    }

    @Test
    public void directoryPatternNegative() {
        assertDirectoryCount(4, this::structure2, "!directory1");
    }

    /**
     * Helper method to test.
     */
    private void assertCount(int expected, String... patterns) {
        assertCount(expected, this::structure1, patterns);
    }

    /**
     * Helper method to test.
     */
    private void assertCount(int expected, Consumer<FileSystemDSL> pattern, String... patterns) {
        room.with(pattern);

        assert expected == I.walk(room.root, patterns).size();
    }

    /**
     * Helper method to test.
     */
    private void assertDirectoryCount(int expected, Consumer<FileSystemDSL> pattern, String... patterns) {
        room.with(pattern);

        assert I.walkDirectory(room.root, patterns).size() == expected;
    }
}
