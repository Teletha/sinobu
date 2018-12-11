/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class XML implements Iterable<XML> {

    /**
     * Original pattern.
     * <p>
     * <pre>
     * ([>~+\-, ]*)?((?:(?:\w+|\*)\|)?(?:\w+(?:\\.\w+)*|\*))?(?:#(\w+))?((?:\.\w+)*)(?:\[\s?([\w:]+)(?:\s*([=~^$*|])?=\s*["]([^"]*)["])?\s?\])?(?::([\w-]+)(?:\((odd|even|(\d*)(n)?(?:\+(\d+))?|(?:.*))\))?)?
     * </pre>
     */
    private static final Pattern SELECTOR = Pattern
            .compile("([>~+\\-, ]*)?((?:(?:\\w+|\\*)\\|)?(?:\\w+(?:\\\\.\\w+)*|\\*))?(?:#(\\w+))?((?:\\.\\w+)*)(?:\\[\\s?([\\w:]+)(?:\\s*([=~^$*|])?=\\s*[\"]([^\"]*)[\"])?\\s?\\])?(?::([\\w-]+)(?:\\((odd|even|(\\d*)(n)?(?:\\+(\\d+))?|(?:.*))\\))?)?");

    /** The cache for compiled selectors. */
    private static final Map<String, XPathExpression> selectors = new ConcurrentHashMap();

    /** The current document. */
    private Document doc;

    /** The current node set. */
    private List<Node> nodes;

    /**
     * <p>
     * From node set.
     * </p>
     *
     * @param doc A parent document.
     * @param nodes A current node set.
     */
    XML(Document doc, List nodes) {
        this.doc = doc == null ? I.dom.newDocument() : doc;
        this.nodes = nodes == null ? I.list(this.doc) : nodes;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, to the end of each element in the set of matched
     * elements.
     * </p>
     *
     * @param xml A element set to append.
     * @return Chainable API.
     */
    public XML append(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.appendChild(n.cloneNode(true));
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, to the beginning of each element in the set of
     * matched elements.
     * </p>
     *
     * @param xml A element set to prepend.
     * @return Chainable API.
     */
    public XML prepend(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.insertBefore(n.cloneNode(true), node.getFirstChild());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, before each element in the set of matched
     * elements.
     * </p>
     *
     * @param xml A element set to add.
     * @return Chainable API.
     */
    public XML before(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.getParentNode().insertBefore(n.cloneNode(true), node);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, after each element in the set of matched
     * elements.
     * </p>
     *
     * @param xml A element set to add
     * @return Chainable API.
     */
    public XML after(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.getParentNode().insertBefore(n.cloneNode(true), node.getNextSibling());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove all child nodes of the set of matched elements from the DOM.
     * </p>
     *
     * @return Chainable API.
     */
    public XML empty() {
        for (Node node : nodes) {
            while (node.hasChildNodes()) {
                node.removeChild(node.getFirstChild());
            }
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove the set of matched elements from the DOM.
     * </p>
     * <p>
     * Similar to {@link #empty()}, the {@link #remove()} method takes elements out of the DOM. Use
     * {@link #remove()} when you want to remove the element itself, as well as everything inside
     * it.
     * </p>
     *
     * @return Chainable API.
     */
    public XML remove() {
        for (Node node : nodes) {
            node.getParentNode().removeChild(node);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Wrap an HTML structure around each element in the set of matched elements.
     * </p>
     *
     * @param xml A element set to wrap.
     * @return Chainable API.
     */
    public XML wrap(Object xml) {
        XML element = I.xml(doc, xml);

        for (XML e : this) {
            e.wrapAll(element);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Wrap an HTML structure around all elements in the set of matched elements.
     * </p>
     *
     * @param xml A element set to wrap.
     * @return Chainable API.
     */
    public XML wrapAll(Object xml) {
        first().after(xml).find("+*").append(this);

        // API definition
        return this;
    }

    /**
     * <p>
     * Create a deep copy of the set of matched elements.
     * </p>
     * <p>
     * The .clone() method performs a deep copy of the set of matched elements, meaning that it
     * copies the matched elements as well as all of their descendant elements and text nodes. When
     * used in conjunction with one of the insertion methods, .clone() is a convenient way to
     * duplicate elements on a page.
     * </p>
     * {@inheritDoc}
     */
    @Override
    public XML clone() {
        List list = new ArrayList();

        for (Node node : nodes) {
            list.add(node.cloneNode(true));
        }
        return new XML(doc, list);

    }

    /**
     * <p>
     * Get the combined text contents of each element in the set of matched elements, including
     * their descendants.
     * </p>
     *
     * @return Chainable API.
     */
    public String text() {
        StringBuilder text = new StringBuilder();

        for (Node node : nodes) {
            text.append(node.getTextContent());
        }
        return text.toString();
    }

    /**
     * <p>
     * Set the content of each element in the set of matched elements to the specified text.
     * </p>
     *
     * @param text A text to set.
     * @return Chainable API.
     */
    public XML text(String text) {
        for (Node node : nodes) {
            node.setTextContent(text);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Get the element name for the first element in the set of matched elements.
     * </p>
     *
     * @return An element name.
     */
    public String name() {
        return ((org.w3c.dom.Element) nodes.iterator().next()).getTagName();
    }

    /**
     * <p>
     * Get the value of an attribute for the first element in the set of matched elements.
     * </p>
     *
     * @param name An attribute name.
     * @return Chainable API.
     */
    public String attr(String name) {
        return ((org.w3c.dom.Element) nodes.iterator().next()).getAttribute(name);
    }

    /**
     * <p>
     * Set one or more attributes for the set of matched elements.
     * </p>
     *
     * @param name An attribute name.
     * @param value An attribute value.
     * @return Chainable API.
     */
    public XML attr(String name, Object value) {
        if (name != null && name.length() != 0) {
            for (Node node : nodes) {
                org.w3c.dom.Element e = (org.w3c.dom.Element) node;

                if (value == null) {
                    if (name.startsWith(XMLNS_ATTRIBUTE)) {
                        // namespace
                        e.removeAttributeNS(XMLNS_ATTRIBUTE_NS_URI, name);
                    } else {
                        // attribute
                        e.removeAttribute(name);
                    }
                } else {
                    try {
                        if (name.startsWith(XMLNS_ATTRIBUTE)) {
                            // namespace
                            e.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, name, value.toString());
                        } else {
                            // attribute
                            e.setAttribute(name, value.toString());
                        }
                    } catch (DOMException dom) {
                        // user specify invalid attribute name
                    }
                }
            }
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Adds the specified class(es) to each of the set of matched elements.
     * </p>
     * <p>
     * It's important to note that this method does not replace a class. It simply adds the class,
     * appending it to any which may already be assigned to the elements. More than one class may be
     * added at a time, separated by a space, to the set of matched elements.
     * </p>
     *
     * @param names Space separated class name list.
     * @return Chainable API.
     */
    public XML addClass(String names) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            for (String name : names.split(" ")) {
                if (!value.contains(" ".concat(name).concat(" "))) {
                    value = value.concat(name).concat(" ");
                }
            }
            e.attr("class", value.trim());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove a single class, multiple classes, or all classes from each element in the set of
     * matched elements.
     * </p>
     * <p>
     * If a class name is included as a parameter, then only that class will be removed from the set
     * of matched elements. If no class names are specified in the parameter, all classes will be
     * removed. More than one class may be removed at a time, separated by a space, from the set of
     * matched elements.
     * </p>
     *
     * @param names Space separated class name list.
     * @return Chainable API.
     */
    public XML removeClass(String names) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            for (String name : names.split(" ")) {
                value = value.replace(" ".concat(name).concat(" "), " ");
            }
            e.attr("class", value.trim());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Add or remove one class from each element in the set of matched elements, depending on either
     * the class's presence or the value of the switch argument.
     * </p>
     *
     * @param name A class name.
     * @return Chainable API.
     */
    public XML toggleClass(String name) {
        for (XML e : this) {
            if (e.hasClass(name)) {
                e.removeClass(name);
            } else {
                e.addClass(name);
            }
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Determine whether any of the matched elements are assigned the given class.
     * </p>
     *
     * @param name A class name.
     * @return Chainable API.
     */
    public boolean hasClass(String name) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            if (value.contains(" ".concat(name).concat(" "))) {
                return true;
            }
        }
        return false;
    }

    // ===============================================
    // Traversing
    // ===============================================

    /**
     * <p>
     * Reduce the set of matched elements to the first in the set.
     * </p>
     *
     * @return Chainable API.
     */
    public XML first() {
        return nodes.isEmpty() ? this : new XML(doc, nodes.subList(0, 1));
    }

    /**
     * <p>
     * Reduce the set of matched elements to the final one in the set.
     * </p>
     *
     * @return Chainable API.
     */
    public XML last() {
        return nodes.isEmpty() ? this : new XML(doc, nodes.subList(nodes.size() - 1, nodes.size()));
    }

    /**
     * <p>
     * Append new child element with the specified name and traverse into them.
     * </p>
     *
     * @param name A child element name.
     * @return A created child elements.
     */
    public XML child(String name) {
        return append("<" + name + "/>").lastChild();
    }

    /**
     * <p>
     * Get the children of each element in the current set of matched elements.
     * </p>
     *
     * @return A set of children elements.
     */
    public XML children() {
        CopyOnWriteArrayList list = new CopyOnWriteArrayList();

        for (Node node : nodes) {
            list.addAllAbsent(convert(node.getChildNodes()));
        }
        return new XML(doc, list);
    }

    /**
     * <p>
     * Get the last child element of each element in the current set of matched elements.
     * </p>
     * 
     * @return A set of fisrt elements.
     */
    public XML firstChild() {
        return find(n -> {
            Node child = n.getFirstChild();
            while (child != null) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    return child;
                }
                child = child.getNextSibling();
            }
            return null;
        });
    }

    /**
     * <p>
     * Get the last child element of each element in the current set of matched elements.
     * </p>
     * 
     * @return A set of last elements.
     */
    public XML lastChild() {
        return find(n -> {
            Node child = n.getLastChild();
            while (child != null) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    return child;
                }
                child = child.getPreviousSibling();
            }
            return null;
        });
    }

    /**
     * <p>
     * Get the parent of each element in the current set of matched elements.
     * </p>
     *
     * @return A set of parent elements.
     */
    public XML parent() {
        return find(n -> {
            Node parent = n.getParentNode();
            return parent instanceof Element ? parent : n;
        });
    }

    /**
     * <p>
     * Get the previous sibling element of each element in the current set of matched elements.
     * </p>
     *
     * @return A set of previous elements.
     */
    public XML prev() {
        return find(n -> {
            Node sibling = n.getPreviousSibling();
            while (sibling != null) {
                if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                    return sibling;
                }
                sibling = sibling.getPreviousSibling();
            }
            return null;
        });
    }

    /**
     * <p>
     * Get the next sibling element of each element in the current set of matched elements.
     * </p>
     *
     * @return A set of next elements.
     */
    public XML next() {
        return find(n -> {
            Node sibling = n.getNextSibling();
            while (sibling != null) {
                if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                    return sibling;
                }
                sibling = sibling.getNextSibling();
            }
            return null;
        });
    }

    /**
     * <p>
     * Get the descendants of each element in the current set of matched elements, filtered by
     * {@link UnaryOperator} selector.
     * </p>
     * 
     * @param selector
     * @return Chainable API.
     */
    private XML find(UnaryOperator<Node> selector) {
        CopyOnWriteArrayList<Node> list = new CopyOnWriteArrayList();

        for (Node node : nodes) {
            Node found = selector.apply(node);

            if (found != null) {
                list.addIfAbsent(found);
            }
        }
        return new XML(doc, list);
    }

    /**
     * <p>
     * Get the descendants of each element in the current set of matched elements, filtered by a css
     * selector.
     * </p>
     *
     * @param selector A string containing a css selector expression to match elements against.
     * @return Chainable API.
     */
    public XML find(String selector) {
        XPathExpression xpath = compile(selector);
        CopyOnWriteArrayList<Node> result = new CopyOnWriteArrayList();

        try {
            for (Node node : nodes) {
                result.addAll(convert((NodeList) xpath.evaluate(node, XPathConstants.NODESET)));
            }
            return new XML(doc, result);
        } catch (XPathExpressionException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Return size of the current node set.
     * </p>
     *
     * @return A size of current node set.
     */
    public int size() {
        return nodes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<XML> iterator() {
        List<XML> elements = new ArrayList();

        for (Node node : nodes) {
            elements.add(new XML(doc, Collections.singletonList(node)));
        }
        return elements.iterator();
    }

    /**
     * <p>
     * Convert the first element to {@link Node}.
     * </p>
     *
     * @return A first {@link Node} or <code>null</code>.
     */
    public Node to() {
        return nodes.size() == 0 ? null : nodes.get(0);
    }

    /**
     * <p>
     * Write this elements to the specified output.
     * </p>
     *
     * @param output A output channel.
     */
    public void to(Appendable output) {
        try {
            LSSerializer writer = ((DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS"))
                    .createLSSerializer();

            DOMConfiguration config = writer.getDomConfig();
            config.setParameter("format-pretty-print", Boolean.TRUE);
            config.setParameter("xml-declaration", Boolean.FALSE);

            for (Node node : nodes) {
                output.append(writer.writeToString(node));
            }

            // flush and close if needed
            I.quiet(output);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        to(builder);

        return builder.toString().trim();
    }

    /**
     * <p>
     * Helper method to convert {@link XML} to single {@link Node}.
     * </p>
     *
     * @param xml
     * @return
     */
    private Node convert(XML xml) {
        DocumentFragment fragment = doc.createDocumentFragment();

        for (int i = 0; i < xml.nodes.size(); i++) {
            Node node = xml.nodes.get(i);

            if (doc == xml.doc) {
                fragment.appendChild(node);
            } else {
                xml.nodes.set(i, fragment.appendChild(doc.importNode(node, true)));
            }
        }

        // update
        xml.doc = doc;

        // root
        return fragment;
    }

    /**
     * <p>
     * Helper method to convert {@link NodeList} to single {@link List}.
     * </p>
     *
     * @param list A {@link NodeList} to convert.
     * @return
     */
    static List<Node> convert(NodeList list) {
        CopyOnWriteArrayList<Node> nodes = new CopyOnWriteArrayList();

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                nodes.addIfAbsent(node);
            }
        }
        return nodes;
    }

    /**
     * <p>
     * Compile and cache the specified selector.
     * </p>
     *
     * @param selector A css selector.
     * @return A compiled xpath expression.
     */
    private static XPathExpression compile(String selector) {
        // check cache
        XPathExpression compiled = selectors.get(selector);

        if (compiled == null) {
            try {
                // compile actually
                compiled = I.xpath.compile(convert(selector));

                // cache it
                selectors.put(selector, compiled);
            } catch (XPathExpressionException e) {
                throw I.quiet(e);
            }
        }

        // API definition
        return compiled;
    }

    /**
     * <p>
     * Helper method to convert css selector to xpath.
     * </p>
     *
     * @param selector A css selector.
     * @return A xpath.
     */
    private static String convert(String selector) {
        StringBuilder xpath = new StringBuilder();
        Matcher matcher = SELECTOR.matcher(selector.trim());

        while (matcher.find()) {
            // =================================================
            // Combinators
            // =================================================
            String suffix = null;
            String match = matcher.group(1);
            boolean contextual = matcher.start() == 0;

            if (match.length() == 0) {
                // no combinator
                if (contextual) {
                    // first selector
                    xpath.append("descendant::");
                } else {
                    break; // finish parsing
                }
            } else {
                match = match.trim();

                if (match.length() == 0) {
                    // Descendant combinator
                    xpath.append("//");
                } else {
                    switch (match.charAt(0)) {
                    case '>': // Child combinator
                        xpath.append('/');
                        break;

                    case '~': // General sibling combinator
                        xpath.append("/following-sibling::");
                        break;

                    case '+': // Adjacent sibling combinator
                        xpath.append("/following-sibling::");
                        suffix = "[1]";
                        break;

                    case '-': // Adjacent previous sibling combinator (EXTENSION)
                        xpath.append("/preceding-sibling::");
                        suffix = "[1]";
                        break;

                    case ',': // selector separator
                        xpath.append("|descendant::");
                        break;
                    }

                    if (contextual) {
                        xpath.delete(0, 1);
                    }
                }
            }

            // =================================================
            // Type (Universal) Selector
            // =================================================
            match = matcher.group(2);

            if (match == null || match.equals("*")) {
                xpath.append("*");
            } else {
                xpath.append("*[name()='").append(match.replace('|', ':').replaceAll("\\\\", "")).append("']");
            }

            // =================================================
            // ID Selector
            // =================================================
            match = matcher.group(3);

            if (match != null) {
                xpath.append("[@id='").append(match).append("']");
            }

            // =================================================
            // Class Selector
            // =================================================
            match = matcher.group(4);

            if (match != null && match.length() != 0) {
                for (String className : match.substring(1).split("\\.")) {
                    xpath.append("[contains(concat(' ',normalize-space(@class),' '),' ").append(className).append(" ')]");
                }
            }

            // =================================================
            // Attribute Selector
            // =================================================
            match = matcher.group(5);

            if (match != null) {
                String value = matcher.group(7);

                if (value == null) {
                    // [att]
                    //
                    // Represents an element with the att attribute, whatever the value
                    // of the attribute.
                    xpath.append("[@").append(match).append("]");
                } else {
                    String type = matcher.group(6);

                    if (type == null) {
                        // [att=val]
                        //
                        // Represents an element with the att attribute whose value
                        // is exactly "val".
                        xpath.append("[@").append(match).append("='").append(value).append("']");
                    } else {
                        switch (type.charAt(0)) {
                        case '~':
                            // [att~=val]
                            //
                            // Represents an element with the att attribute whose value is a
                            // whitespace-separated list of words, one of which is exactly
                            // "val". If "val" contains whitespace, it will never represent
                            // anything (since the words are separated by spaces). Also if "val"
                            // is the empty string, it will never represent anything.
                            xpath.append("[contains(concat(' ',@").append(match).append(",' '),' ").append(value).append(" ')]");
                            break;

                        case '*':
                            // [att*=val]
                            //
                            // Represents an element with the att attribute whose value contains
                            // at least one instance of the substring "val". If "val" is the
                            // empty string then the selector does not represent anything.
                            xpath.append("[contains(@").append(match).append(",'").append(value).append("')]");
                            break;

                        case '^':
                            // [att^=val]
                            //
                            // Represents an element with the att attribute whose value begins
                            // with the prefix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[starts-with(@").append(match).append(",'").append(value).append("')]");
                            break;

                        case '$':
                            // [att$=val]
                            //
                            // Represents an element with the att attribute whose value ends
                            // with the suffix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[substring(@")
                                    .append(match)
                                    .append(", string-length(@")
                                    .append(match)
                                    .append(") - string-length('")
                                    .append(value)
                                    .append("') + 1) = '")
                                    .append(value)
                                    .append("']");
                            break;

                        case '|':
                            // [att|=val]
                            //
                            // Represents an element with the att attribute, its value either
                            // being exactly "val" or beginning with "val" immediately followed
                            // by "-" (U+002D).
                            break;
                        }
                    }
                }
            }

            // =================================================
            // Structural Pseudo Classes Selector
            // =================================================
            match = matcher.group(8);

            if (match != null) {
                switch (match.hashCode()) {
                case -947996741: // only-child
                    xpath.append("[count(../*) = 1]");
                    break;

                case 1455900751: // only-of-type
                    xpath.append("[count(../").append(matcher.group(2)).append(")=1]");
                    break;

                case 96634189: // empty
                    xpath.append("[not(node())]");
                    break;

                case 109267: // not
                case 103066: // has
                    xpath.append('[');

                    if (match.charAt(0) == 'n') {
                        xpath.append("not");
                    }
                    xpath.append('(');

                    String sub = convert(matcher.group(9));

                    if (sub.startsWith("descendant::")) {
                        sub = sub.replace("descendant::", "descendant-or-self::");
                    }
                    xpath.append(sub).append(")]");
                    break;

                case -995424086: // parent
                    xpath.append("/..");
                    break;

                case 3506402: // root
                    xpath.delete(0, xpath.length()).append("/*");
                    break;

                case -567445985: // contains
                    xpath.append("[contains(text(),'").append(matcher.group(9)).append("')]");
                    break;

                case 835834661: // last-child
                    xpath.append("[not(following-sibling::*)]");
                    break;

                case -2136991809: // first-child
                case 1292941139: // first-of-type
                case 2025926969: // last-of-type
                case -1754914063: // nth-child
                case -1629748624: // nth-last-child
                case -897532411: // nth-of-type
                case -872629820: // nth-last-of-type
                    String coefficient = matcher.group(10);
                    String remainder = matcher.group(9);

                    if (remainder == null) {
                        // coefficient = null; // coefficient is already null
                        remainder = "1";
                    } else if (matcher.group(11) == null) {
                        coefficient = null;
                        // remainder = matcher.group(9); // remainder is already assigned

                        if (remainder.equals("even")) {
                            coefficient = "2";
                            remainder = "0";
                        } else if (remainder.equals("odd")) {
                            coefficient = "2";
                            remainder = "1";
                        }
                    } else {
                        // coefficient = matcher.group(10); // coefficient is already assigned
                        remainder = matcher.group(12);

                        if (remainder == null) {
                            remainder = "0";
                        }
                    }

                    xpath.append("[(count(");

                    if (match.contains("last")) {
                        xpath.append("following");
                    } else {
                        xpath.append("preceding");
                    }

                    xpath.append("-sibling::");

                    if (match.endsWith("child")) {
                        xpath.append("*");
                    } else {
                        xpath.append(matcher.group(2));
                    }
                    xpath.append(")+1)");

                    if (coefficient != null) {
                        if (coefficient.length() == 0) {
                            coefficient = "1";
                        }
                        xpath.append(" mod ").append(coefficient);
                    }
                    xpath.append('=').append(remainder).append("]");
                    break;
                }
            }

            if (suffix != null) {
                xpath.append(suffix);
            }
        }
        return xpath.toString();
    }

    // =======================================================================
    // HTML Parser for building XML
    // =======================================================================
    /** The defiened empty elements. */
    private static final String[] empties = {"area", "base", "br", "col", "command", "device", "embed", "frame", "hr", "img", "input",
            "keygen", "link", "meta", "param", "source", "track", "wbr"};

    /** The defiened data elements. */
    private static final String[] datas = {"noframes", "script", "style", "textarea", "title"};

    /** The position for something. */
    private int pos;

    /** The encoded text data. */
    private String html;

    /**
     * <p>
     * Parse HTML and build {@link XML}.
     * </p>
     * 
     * @param raw The raw date of HTML.
     * @param encoding The charset to parse.
     */
    XML parse(byte[] raw, Charset encoding) {
        // ====================
        // Initialization
        // ====================
        XML xml = this;
        html = new String(raw, 0, raw.length, encoding);
        pos = 0;

        // If crazy html provides multiple meta element for character encoding,
        // we should adopt first one.
        boolean detectable = true;

        // ====================
        // Start Parsing
        // ====================
        nextSpace();

        while (pos != html.length()) {
            if (test("<!--")) {
                // =====================
                // Comment
                // =====================
                xml.append(xml.doc.createComment(next("->")));
            } else if (test("<![CDATA[")) {
                // =====================
                // CDATA
                // =====================
                xml.append(xml.doc.createCDATASection(next("]]>")));
            } else if (test("<!") || test("<?")) {
                // =====================
                // DocType and PI
                // =====================
                // ignore doctype and pi
                next(">");
            } else if (test("</")) {
                // =====================
                // End Element
                // =====================
                next(">");

                // update current element into parent
                xml = xml.parent();
            } else if (test("<")) {
                // =====================
                // Start Element
                // =====================
                String name = nextName();
                nextSpace();

                XML child = xml.child(name);

                // parse attributes
                while (html.charAt(pos) != '/' && html.charAt(pos) != '>') {
                    String attr = nextName();
                    nextSpace();

                    if (!test("=")) {
                        // single value attribute
                        child.attr(attr, attr);

                        // step into next
                        pos++;
                    } else {
                        // name-value pair attribute
                        nextSpace();

                        if (test("\"")) {
                            // quote attribute
                            child.attr(attr, next("\""));
                        } else if (test("'")) {
                            // apostrophe attribute
                            child.attr(attr, next("'"));
                        } else {
                            // non-quoted attribute
                            int start = pos;
                            char c = html.charAt(pos);

                            while (c != '>' && !Character.isWhitespace(c)) {
                                c = html.charAt(++pos);
                            }

                            if (html.charAt(pos - 1) == '/') {
                                pos--;
                            }
                            child.attr(attr, html.substring(start, pos));
                        }
                    }
                    nextSpace();
                }

                // close start element
                if (next(">").length() == 0 && Arrays.binarySearch(empties, name) < 0) {
                    // container element
                    if (0 <= Arrays.binarySearch(datas, name)) {
                        // text data only element - add contents as text

                        // At first, we find the end element, but the some html provides crazy
                        // element pair like <div></DIV>. So we should search by case-insensitive,
                        // don't use find("</" + name + ">").
                        int start = pos;

                        next("</");
                        while (!nextName().equals(name)) {
                            next("</");
                        }
                        next(">");

                        child.text(html.substring(start, pos - 3 - name.length()));
                        // don't update current elment
                    } else {
                        // mixed element
                        // update current element into child
                        xml = child;
                    }
                } else {
                    // empty element

                    // check encoding in meta element
                    if (detectable && name.equals("meta")) {
                        String value = child.attr("charset");

                        if (value.length() == 0 && child.attr("http-equiv").equalsIgnoreCase("content-type")) {
                            value = child.attr("content");
                        }

                        if (value.length() != 0) {
                            detectable = false;

                            try {
                                int index = value.lastIndexOf('=');
                                Charset detect = Charset.forName(index == -1 ? value : value.substring(index + 1));

                                if (!encoding.equals(detect)) {
                                    // reset and parse again if the current encoding is wrong
                                    return parse(raw, detect);
                                }
                            } catch (Exception e) {
                                // unkwnown encoding name
                            }
                        }
                    }
                    // don't update current elment
                }
            } else {
                // =====================
                // Text
                // =====================
                xml.append(xml.doc.createTextNode(next("<")));

                // If the current positon is not end of document, we should continue to parse
                // next elements. So rollback position(1) for the "<" next start element.
                if (pos != html.length()) {
                    pos--;
                }
            }
            nextSpace();
        }

        this.nodes = convert(xml.doc.getChildNodes());
        return this;
    }

    /**
     * <p>
     * Check whether the current sequence is started with the specified characters or not. If it is
     * matched, cursor positioning step into it.
     * </p>
     * 
     * @param until A condition.
     * @return A result.
     */
    private boolean test(String until) {
        if (html.startsWith(until, pos)) {
            pos += until.length();
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Consume the next run of characters until the specified sequence will be appered.
     * </p>
     * 
     * @param until A target character sequence. (include this sequence)
     * @return A consumed character sequence. (exclude the specified sequence)
     */
    private String next(String until) {
        int start = pos;
        int index = html.indexOf(until, pos);

        if (index == -1) {
            // until last
            pos = html.length();
        } else {
            // until matched sequence
            pos = index + until.length();
        }
        return html.substring(start, pos - until.length());
    }

    /**
     * <p>
     * Consume an identical name. (word or :, _, -)
     * </p>
     * 
     * @return An identical name for XML.
     */
    private String nextName() {
        int start = pos;
        char c = html.charAt(pos);

        while (Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-') {
            c = html.charAt(++pos);
        }
        return html.substring(start, pos).toLowerCase();
    }

    /**
     * <p>
     * Consume the next run of whitespace characters.
     * </p>
     */
    private void nextSpace() {
        while (pos < html.length() && Character.isWhitespace(html.charAt(pos))) {
            pos++;
        }
    }
}
