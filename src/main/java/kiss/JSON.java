/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2017/03/26 12:53:30
 */
public class JSON {

    /** The root object. */
    private final Object root;

    /**
     * Hide constructor.
     * 
     * @param root A root json object.
     */
    JSON(Object root) {
        this.root = root;
    }

    public Signal<String> find(String expression) {
        return find(expression, String.class);
    }

    public <M> Signal<M> find(String expression, Class<M> type) {
        return select(expression).map(v -> v.to(type));
    }

    private Signal<JSON> select(String expression) {
        Signal<Object> current = I.signal(root);

        for (String name : expression.split("\\.")) {
            current = current.flatMap(v -> {
                if (v instanceof Map) {
                    int i = name.lastIndexOf('[');
                    String main = i == -1 ? name : name.substring(0, i);
                    String sub = i == -1 ? null : name.substring(i + 1, name.length() - 1);
                    Object value = ((Map) v).get(main);

                    if (sub != null) {
                        return I.signal(((Map) value).get(sub));
                    } else if (value instanceof LinkedHashMap) {
                        return I.signal(((Map) value).values());
                    }

                    return I.signal(value);
                } else {
                    return Signal.NEVER;
                }
            });
        }
        return current.map(JSON::new);
    }

    public <M> M to(Class<M> type) {
        Model<M> model = Model.of(type);
        return model.attribute ? I.transform(root, type) : to(model, I.make(type), root);
    }

    public <M> M to(M value) {
        return to(Model.of(value), value, root);
    }

    /**
     * <p>
     * Helper method to traverse json structure using Java Object {@link Model}.
     * </p>
     *
     * @param <M> A current model type.
     * @param model A java object model.
     * @param java A java value.
     * @param js A javascript value.
     * @return A restored java object.
     */
    private <M> M to(Model<M> model, M java, Object js) {
        if (js instanceof Map) {
            Map<String, Object> map = (Map) js;

            List<Property> properties = new ArrayList(model.properties());

            if (properties.isEmpty()) {
                for (String id : map.keySet()) {
                    Property property = model.property(id);

                    if (property != null) {
                        properties.add(property);
                    }
                }
            }

            for (Property property : properties) {
                if (!property.isTransient) {
                    if (map.containsKey(property.name)) {
                        // calculate value
                        map.containsKey(property.name);
                        Object value = map.get(property.name);
                        Class type = property.model.type;

                        // convert value
                        if (property.isAttribute()) {
                            value = I.transform(value, type);
                        } else {
                            value = to(property.model, I.make(type), value);
                        }

                        // assign value
                        model.set(java, property, value);
                    }
                }
            }
        }

        // API definition
        return java;
    }

    // ===========================================================
    // Parser API
    // ===========================================================
    private Reader reader;

    private char[] buffer;

    private int index;

    private int fill;

    private int current;

    private StringBuilder captureBuffer;

    private int captureStart;

    JSON(Reader reader) throws IOException {
        this.reader = reader;
        buffer = new char[1024];
        captureStart = -1;

        read();
        space();
        root = readValue();
    }

    private Object readValue() throws IOException {
        switch (current) {
        // keyword
        case 'n':
            return keyword(null);
        case 't':
            return keyword(Boolean.TRUE);
        case 'f':
            return keyword(Boolean.FALSE);

        // string
        case '"':
            return string();

        case '[':
            read();
            Map array = new LinkedHashMap();
            space();
            if (read(']')) {
                return array;
            }

            int count = 0;
            do {
                space();
                array.put(String.valueOf(count++), readValue());
                space();
            } while (read(','));

            if (!read(']')) {
                throw expected("',' or ']'");
            }
            return array;

        // object
        case '{':
            Map object = new HashMap();
            read();
            space();
            if (read('}')) {
                return object;
            }
            do {
                space();

                if (current != '"') {
                    throw expected("name");
                }
                String name = string();
                space();
                if (!read(':')) {
                    throw expected("':'");
                }
                space();
                object.put(name, readValue());
                space();
            } while (read(','));
            if (!read('}')) {
                throw expected("',' or '}'");
            }
            return object;

        // number
        case '-':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            startCapture();
            read('-');
            if (current == '0') {
                read();
            } else {
                digit();
            }

            // fraction
            if (read('.')) {
                digit();
            }

            // exponent
            if (read('e') || read('E')) {
                if (!read('+')) {
                    read('-');
                }
                digit();
            }
            return endCapture();

        default:
            throw expected("value");
        }
    }

    /**
     * <p>
     * Read the sequence of white spaces
     * </p>
     * 
     * @throws IOException
     */
    private void space() throws IOException {
        while (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
            read();
        }
    }

    /**
     * <p>
     * Read the sequence of digit.
     * </p>
     * 
     * @throws IOException
     */
    private void digit() throws IOException {
        int count = 0;

        while ('0' <= current && current <= '9') {
            read();
            count++;
        }

        if (count == 0) {
            throw expected("digit");
        }
    }

    /**
     * <p>
     * Read the sequence of keyword.
     * </p>
     * 
     * @param keyword A target value.
     * @return A target value.
     * @throws IOException
     */
    private Object keyword(Object keyword) throws IOException {
        read();

        String value = String.valueOf(keyword);

        for (int i = 1; i < value.length(); i++) {
            if (!read(value.charAt(i))) {
                throw expected("'" + value.charAt(i) + "'");
            }
        }
        return keyword;
    }

    /**
     * <p>
     * Read the sequence of String.
     * </p>
     * 
     * @return A parsed string.
     * @throws IOException
     */
    private String string() throws IOException {
        read();
        startCapture();
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                // escape
                read();
                switch (current) {
                case '"':
                case '/':
                case '\\':
                    captureBuffer.append((char) current);
                    break;
                case 'b':
                    captureBuffer.append('\b');
                    break;
                case 'f':
                    captureBuffer.append('\f');
                    break;
                case 'n':
                    captureBuffer.append('\n');
                    break;
                case 'r':
                    captureBuffer.append('\r');
                    break;
                case 't':
                    captureBuffer.append('\t');
                    break;
                case 'u':
                    char[] chars = new char[4];
                    for (int i = 0; i < 4; i++) {
                        read();
                        chars[i] = (char) current;
                    }
                    captureBuffer.append((char) Integer.parseInt(new String(chars), 16));
                    break;
                default:
                    throw expected("valid escape sequence");
                }
                read();
                startCapture();
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    /**
     * <p>
     * Read the next character.
     * </p>
     * 
     * @throws IOException
     */
    private void read() throws IOException {
        if (index == fill) {
            if (captureStart != -1) {
                captureBuffer.append(buffer, captureStart, fill - captureStart);
                captureStart = 0;
            }
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
                current = -1;
                return;
            }
        }
        current = buffer[index++];
    }

    /**
     * <p>
     * Read the specified character.
     * </p>
     * 
     * @param c The character to be red.
     * @return A result.
     * @throws IOException
     */
    private boolean read(char c) throws IOException {
        if (current != c) {
            return false;
        } else {
            read();
            return true;
        }
    }

    private void startCapture() {
        if (captureBuffer == null) {
            captureBuffer = new StringBuilder();
        }
        captureStart = index - 1;
    }

    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        captureBuffer.append(buffer, captureStart, end - captureStart);
        captureStart = -1;
    }

    private String endCapture() {
        int end = current == -1 ? index : index - 1;
        String captured;
        if (captureBuffer.length() > 0) {
            captureBuffer.append(buffer, captureStart, end - captureStart);
            captured = captureBuffer.toString();
            captureBuffer.setLength(0);
        } else {
            captured = new String(buffer, captureStart, end - captureStart);
        }
        captureStart = -1;
        return captured;
    }

    private IllegalStateException expected(String expected) {
        if (current == -1) {
            return new IllegalStateException("Unexpected end of input");
        } else {
            return new IllegalStateException("Expected " + expected);
        }
    }
}
