/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import kiss.I;

class ParserTest {

    @Test
    void empty() {
        parse("{}");
        parse("{ }");
        parse("{\t}");
        parse("{\r}");
        parse("{\n}");
        parse(" { } ");
        parse("");
    }

    @Test
    void space() {
        parseRaw("""
                {
                    " s p a c e " : null
                }
                """);
    }

    @Test
    void invalidEndBrace() {
        assertThrows(IllegalStateException.class, () -> {
            parse("{");
        });
    }

    @Test
    void invalidStartBrace() {
        assertThrows(IllegalStateException.class, () -> {
            parse("}");
        });
    }

    @Test
    void invalidNoSeparator() {
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "true": true
                        "false": false
                    }
                    """);
        });
    }

    @Test
    void invalidTailSeparator() {
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "true": true,
                        "false": false,
                    }
                    """);
        });
    }

    @Test
    void primitiveTrue() {
        parseRaw("""
                {
                    "name": true
                }
                """);
    }

    @Test
    void primitiveFalse() {
        parseRaw("""
                {
                    "name": false
                }
                """);
    }

    @Test
    void primitiveNull() {
        parseRaw("""
                {
                    "name": null
                }
                """);
    }

    @Test
    void invalidPrimitives() {
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "name": undefined
                    }
                    """);
        });
    }

    @Test
    void invalidPrimitiveName1() {
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "name": nulll
                    }
                    """);
        });
    }

    @Test
    void array() {
        // @formatter:off
        parse("{",
        "  'value': ['a', true, false, null, 1, -1, 0.2, 3e+1],",
        "  'space': [' ',''   ,\t'with tab'],",
        "  'empty': []",
        "}");
        // @formatter:on
    }

    @Test
    void object() {
        // @formatter:off
        parse("{",
        "  'value': {'a': 1, 'b': false, 'c': null},",
        "  'space': { ' ' : ' ' },",
        "  'empty': {}",
        "}");
        // @formatter:on
    }

    @Test
    void string() {
        parseRaw("""
                {
                  "name": "the value",
                  "escepe": "\\b \\f \\n \\r \\t \\\" \\\\ \\/",
                  "unicode": "\\u004Do\\u006E",
                  "non-ascii": "あいうえお",
                  "empty": "",
                  "blank": "    "
                }
                """);
    }

    @Test
    void escapedQuote1() {
        // \"
        parseRaw("""
                {
                    "valid": "\\\""
                }
                """);
    }

    @Test
    void escapedQuote2() {
        // \\"
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "valid": "\\\\\""
                    }
                    """);
        });
    }

    @Test
    void escapedQuote3() {
        // \\\"
        parseRaw("""
                {
                    "valid": "\\\\\\\""
                }
                """);
    }

    @Test
    void escapedQuote4() {
        // \\\\"
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "valid": "\\\\\\\\\""
                    }
                    """);
        });
    }

    @Test
    void escapedQuote5() {
        // \\\\\"
        parseRaw("""
                {
                    "valid": "\\\\\\\\\\\""
                }
                """);
    }

    @Test
    void escapedQuote6() {
        // \\\\\\"
        assertThrows(IllegalStateException.class, () -> {
            parseRaw("""
                    {
                        "valid": "\\\\\\\\\\\\\""
                    }
                    """);
        });
    }

    @Test
    void number() {
        // @formatter:off
        parse("{",
        "  '0': 0,",
        "  '1': 1,",
        "  '2': 2,",
        "  '3': 3,",
        "  '4': 4,",
        "  '5': 5,",
        "  '6': 6,",
        "  '7': 7,",
        "  '8': 8,",
        "  '9': 9,",
        "  'minus': -12345,",
        "  'minusZero': -0,",
        "  'fraction': 0.12345,",
        "  'exponetSmall': 1.2e+3,",
        "  'exponetLarge': 1.2E+3,",
        "  'exponetMinus': 1.2e-3,",
        "  'exponetNone': 1.2e3",
        "}");
        // @formatter:on
    }

    @Test
    void invalidNoQuotedName() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
        parse("{",
        "  invalid: 'name'",
        "}");
        // @formatter:on
        });
    }

    @Test
    void invalidNaN() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'NaN': NaN",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidPlus() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'plus': +1",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidZeroPrefix() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'zeroPrefix': 012",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidMinusZeroPrefix() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'zeroPrefix': -012",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidFraction() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'fraction-abbr': .1",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidExponent() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'invalid': 1e*1",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidMinusOnly() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'minus': -",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidMinus() {
        assertThrows(IllegalStateException.class, () -> {
            // @formatter:off
            parse("{",
            "  'minus': -a",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidUnicode1() {
        assertThrows(NumberFormatException.class, () -> {
            // @formatter:off
            parse("{",
            "  'invalid': '\\u000'",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidUnicode2() {
        assertThrows(NumberFormatException.class, () -> {
            // @formatter:off
            parse("{",
            "  'invalid': '\\u000G'",
            "}");
            // @formatter:on
        });
    }

    @Test
    void invalidUnicode3() {
        assertThrows(NumberFormatException.class, () -> {
            // @formatter:off
            parse("{",
            "  'invalid': '\\u000-'",
            "}");
            // @formatter:on
        });
    }

    private void parse(String... texts) {
        StringJoiner joiner = new StringJoiner("\r\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }

        try {
            Constructor<?> c = Class.forName("kiss.JSON").getDeclaredConstructor(Reader.class);
            c.setAccessible(true);
            c.newInstance(new StringReader(joiner.toString()));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    private void parseRaw(String text) {
        try {
            Constructor<?> c = Class.forName("kiss.JSON").getDeclaredConstructor(Reader.class);
            c.setAccessible(true);
            c.newInstance(new StringReader(text));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}