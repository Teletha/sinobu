/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Variable;

/**
 * @version 2017/05/14 12:09:49
 */
public class ReceiverTest extends SignalTester {

    @Test
    public void to() {
        monitor(signal -> signal);

        assert main.emit(1).value(1);
        assert main.emit(2).value(2);
        assert main.isNotDisposed();
    }

    @Test
    public void toCollection() {
        LinkedHashSet<Integer> set = I.signal(30, 20, 10).to(LinkedHashSet.class);
        Iterator<Integer> iterator = set.iterator();
        assert iterator.next() == 30;
        assert iterator.next() == 20;
        assert iterator.next() == 10;
    }

    @Test
    public void toAlternate() {
        Set<Integer> set = I.signal(30, 20, 10).toAlternate();
        assert set.contains(10);
        assert set.contains(20);
        assert set.contains(30);

        // duplicate
        set = I.signal(30, 20, 20, 30).toAlternate();
        assert set.isEmpty();

        // triple
        set = I.signal(30, 20, 20, 30, 10, 20).toAlternate();
        assert set.contains(10);
        assert set.contains(20);
    }

    @Test
    public void toBinary() {
        Variable<Boolean> binary = I.signal().toBinary();
        assert binary.is(false);

        binary = I.signal("on").toBinary();
        assert binary.is(true);

        binary = I.signal("on", "off").toBinary();
        assert binary.is(false);

        binary = I.signal("on", "off", "on again").toBinary();
        assert binary.is(true);
    }

    @Test
    public void toList() {
        List<String> list = I.<String> signal().toList();
        assert list.isEmpty();

        list = I.signal("A").toList();
        assert list.get(0) == "A";

        list = I.signal("A", "B").toList();
        assert list.get(0) == "A";
        assert list.get(1) == "B";

        list = I.signal("A", "B", "C").toList();
        assert list.get(0) == "A";
        assert list.get(1) == "B";
        assert list.get(2) == "C";
    }

    @Test
    public void toMap() {
        Map<String, String> map = I.<String> signal().toMap(v -> "KEY-" + v);
        assert map.isEmpty();

        map = I.signal("A").toMap(v -> "KEY-" + v);
        assert map.get("KEY-A") == "A";

        map = I.signal("A", "B").toMap(v -> "KEY-" + v);
        assert map.get("KEY-B") == "B";
        assert map.size() == 2;

        map = I.signal("A", "B", "A").toMap(v -> "KEY-" + v);
        assert map.size() == 2;
    }

    @Test
    public void toSet() {
        Set<String> set = I.<String> signal().toSet();
        assert set.isEmpty();

        set = I.signal("A").toSet();
        assert set.size() == 1;

        set = I.signal("A", "B").toSet();
        assert set.size() == 2;

        set = I.signal("A", "B", "A").toSet();
        assert set.size() == 2;
    }
}
