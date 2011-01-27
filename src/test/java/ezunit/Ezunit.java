/**
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezunit;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.I;
import ezbean.io.FileSystem;
import ezbean.xml.SAXBuilder;
import ezbean.xml.XMLWriter;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/29 22:04:35
 */
public class Ezunit {

    /** The empty attribute for reuse. */
    public static final Attributes EMPTY_ATTR = new AttributesImpl();

    /**
     * <p>
     * Assert that the specified abstract file is file (not directory), present and has the given
     * file contents.
     * </p>
     * 
     * @param file A file to test.
     * @param contents File contents.
     */
    public static void assertFile(File file, String... contents) {
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertFalse(file.isDirectory());

        String expected = option(contents, null);

        if (expected != null) {
            assertEquals(expected, readLine(file));
        }
    }

    /**
     * Helper method to assert file.
     * 
     * @param file
     */
    public static void assertDirectory(File file) {
        assertNotNull(file);
        assertTrue(file.isDirectory());
        assertFalse(file.isFile());
        assertTrue(file.exists());
    }

    /**
     * Helper method to assert file.
     * 
     * @param file
     */
    public static void assertArchive(File file) {
        assertNotNull(file);
        assertTrue(file.isDirectory());
        assertTrue(file.isFile());
        assertTrue(file.exists());
    }

    /**
     * Assert file path's equivalence.
     * 
     * @param expected
     * @param test
     */
    public static void assertFileEquals(File expected, File test) {
        assertNotNull(test);
        assertNotNull(expected);

        assertEquals(expected.getPath(), test.getPath());
        assertEquals(expected.getAbsolutePath(), test.getAbsolutePath());
        assertEquals(expected.getAbsoluteFile(), test.getAbsoluteFile());

        try {
            assertEquals(expected.getCanonicalPath(), test.getCanonicalPath());
            assertEquals(expected.getCanonicalFile(), test.getCanonicalFile());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * <p>
     * Locate a class file of the specified class.
     * </p>
     * 
     * @param clazz A class to resolve location.
     * @return A located class file.
     * @throws NullPointerException If the class is <code>null</code>.
     */
    public static File locate(Class clazz) {
        return I.locate(clazz.getResource(clazz.getSimpleName().concat(".class")));
    }

    /**
     * <p>
     * Locate a package directory that the specified class exists.
     * </p>
     * 
     * @param clazz A class to resolve location.
     * @return A located package directory.
     * @throws NullPointerException If the class is <code>null</code>.
     */
    public static File locatePackage(Class clazz) {
        return I.locate(clazz.getResource(""));
    }

    /**
     * <p>
     * Reads all characters from a file into a {@link String}, using the given character set or
     * {@link I#getEncoding()}.
     * </p>
     * 
     * @param file A file to read from.
     * @param charset A character set used when reading the file.
     * @return A string containing all the characters from the file.
     */
    public static String read(File file, Charset... charset) {
        StringBuilder builder = new StringBuilder();

        for (String line : readLines(file, charset)) {
            builder.append(line).append(File.separatorChar);
        }

        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * <p>
     * Reads the first line from a file. The line does not include line-termination characters, but
     * does include other leading and trailing whitespace.
     * </p>
     * 
     * @param file A file to read from
     * @param charset A character set used when writing the file. If you don't specify, Otherwise
     *            {@link I#getEncoding()}.
     * @return the first line, or null if the file is empty
     * @throws IOException if an I/O error occurs
     */
    public static String readLine(File file, Charset... charset) {
        List<String> lines = readLines(file, option(charset, I.getEncoding()), false);

        return lines.size() == 0 ? "" : lines.get(0);
    }

    /**
     * <p>
     * Reads all of the lines from a file. The lines do not include line-termination characters, but
     * do include other leading and trailing whitespace.
     * </p>
     * 
     * @param file A file to read from
     * @param charset A character set used when writing the file. If you don't specify, Otherwise
     *            {@link I#getEncoding()}.
     * @return the first line, or null if the file is empty
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLines(File file, Charset... charset) {
        return readLines(file, option(charset, I.getEncoding()), true);
    }

    /**
     * Helper method to decide option.
     * 
     * @param <T>
     * @param option
     * @param defaultValue
     * @return
     */
    private static <T> T option(T[] option, T defaultValue) {
        return option != null && option.length != 0 && option[0] != null ? option[0] : defaultValue;
    }

    /**
     * Read file contents actually.
     * 
     * @param file
     * @param charset
     * @param all
     * @return
     */
    private static List<String> readLines(File file, Charset charset, boolean all) {
        List<String> lines = new ArrayList();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

            for (int i = 0; reader.ready(); i++) {
                if (!all && i == 1) {
                    break;
                }
                lines.add(reader.readLine());
            }
        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            FileSystem.close(reader);
        }
        return lines;
    }

    // ==================================================================== //
    // Informal Methods
    // ==================================================================== //

    /**
     * <p>
     * Locate the specified file name.
     * </p>
     * 
     * @param filePath A file name to resolve location.
     * @return A located {@link File}.
     */
    public static final File locate(String filePath) {
        return locateFileFromCaller(filePath);
    }

    /**
     * <p>
     * Locate the specified file name as {@link InputSource}.
     * </p>
     * 
     * @param filePath A file name to resolve location.
     * @return A located {@link InputSource}.
     */
    public static final InputSource locateSource(String filePath) {
        // API definition
        return locateSource(locateFileFromCaller(filePath));
    }

    /**
     * <p>
     * Locate the specified file name as {@link InputSource}.
     * </p>
     * 
     * @param filePath A file name to resolve location.
     * @return A located {@link InputSource}.
     */
    public static final InputSource locateSource(File file) {
        InputSource source = new InputSource(file.toURI().toString());
        source.setEncoding(I.getEncoding().name());
        source.setPublicId(file.toString());

        // API definition
        return source;
    }

    /**
     * <p>
     * Build a xml document form the specified file name.
     * </p>
     * 
     * @param filePath A file name to resolve location.
     * @param filters A list of filters to transform xml.
     * @return A created {@link Document}.
     */
    public static final Document locateDOM(String filePath, XMLFilter... filters) {
        return locateDOM(locateFileFromCaller(filePath), filters);
    }

    /**
     * <p>
     * Build a xml document form the specified file name.
     * </p>
     * 
     * @param filePath A file name to resolve location.
     * @param filters A list of filters to transform xml.
     * @return A created {@link Document}.
     */
    public static final Document locateDOM(File file, XMLFilter... filters) {
        // build xml pipe
        SAXBuilder builder = new SAXBuilder();

        List<XMLFilter> list = new ArrayList();
        list.addAll(Arrays.asList(filters));
        list.add(new IgnoreWhitspaceFilter());
        list.add(builder);

        // start parsing
        I.parse(locateSource(file), list.toArray(new XMLFilter[list.size()]));

        // retrive result DOM document
        Document document = builder.getDocument();

        // check null
        assertNotNull(document);

        // API definition
        return document;
    }

    /**
     * <p>
     * Locate the specified file name with the context which is located by the caller class.
     * </p>
     * 
     * @param filePath A file name to resolve location.
     * @return A located file.
     */
    private static File locateFileFromCaller(String filePath) {
        Class caller = getCaller();
        URL url = caller.getResource(filePath);

        if (url == null) {
            fail("The resource is not found. [" + filePath + "]");
        }

        // resolve file location
        return I.locate(url);
    }

    /**
     * <p>
     * Search caller testcase class.
     * </p>
     * 
     * @return A testcase class.
     */
    public static final Class getCaller() {
        // caller
        Exception e = new Exception();
        StackTraceElement[] elements = e.getStackTrace();

        for (StackTraceElement element : elements) {
            String name = element.getClassName();

            if (name.endsWith("Test")) {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException classNotFoundException) {
                    // If this exception will be thrown, it is bug of this program. So we must
                    // rethrow the wrapped error in here.
                    throw new Error(classNotFoundException);
                }
            }
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error("Testcas is not found.");
    }

    /**
     * Assert that two XML documents are similar.
     * 
     * @param expected A XML to be compared against.
     * @param tested A XML to be tested.
     */
    public static final void assertXMLEqual(Document expected, Document tested) {
        Diff diff = new Diff(expected, tested);

        if (!diff.similar()) {
            System.out.println("The given document :");
            dumpXML(tested);

            System.out.println("The expected document :");
            dumpXML(expected);

            fail(diff.toString());
        }
    }

    /**
     * <p>
     * Assert that two XML documents are similar.
     * </p>
     * 
     * @param expectedXMLFilePath A XML to be compared against.
     * @param testedXMLFilePath A XML to be tested.
     * @param filters A list of filters to transform xml.
     */
    public static final void assertXMLEqual(String expectedXMLFilePath, String testedXMLFilePath, XMLFilter... filters) {
        assertXMLEqual(locateDOM(expectedXMLFilePath), locateDOM(testedXMLFilePath, filters));
    }

    /**
     * Assert that two XML documents are identical.
     * 
     * @param expected A XML to be compared against.
     * @param tested A XML to be tested.
     */
    public static final void assertXMLIdentical(Document expected, Document tested) {
        Diff diff = new Diff(expected, tested);

        if (!diff.identical()) {
            System.out.println("The given document :");
            dumpXML(tested);

            System.out.println("The expected document :");
            dumpXML(expected);

            fail(diff.toString());
        }
    }

    /**
     * Assert that two XML documents are identical.
     * 
     * @param expectedXMLFilePath A XML to be compared against.
     * @param testedXMLFilePath A XML to be tested.
     * @param filters A list of filters to transform xml.
     */
    public static final void assertXMLIdentical(String expectedXMLFilePath, String testedXMLFilePath, XMLFilter... filters) {
        assertXMLIdentical(locateDOM(expectedXMLFilePath), locateDOM(testedXMLFilePath, filters));
    }

    /**
     * Assert that the XML documents has the expected value which is result of XPath evaluation.
     * 
     * @param expected A expected value.
     * @param tested A XML to be tested.
     * @param xpath A XPath expression.
     */
    public static final void assertXPathEqual(String expected, String testedXMLFilePath, String xpath) {
        assertXPathEqual(expected, locateFileFromCaller(testedXMLFilePath), xpath);
    }

    /**
     * Assert that the XML documents has the expected value which is result of XPath evaluation.
     * 
     * @param expected A expected value.
     * @param tested A XML to be tested.
     * @param xpath A XPath expression.
     */
    public static final void assertXPathEqual(String expected, File testedXMLFile, String xpath) {
        try {
            XPath context = XPathFactory.newInstance().newXPath();
            context.setNamespaceContext(new Namespaces());
            String result = context.evaluate(xpath, locateSource(testedXMLFile));

            assertEquals(expected, result);
        } catch (XPathExpressionException e) {
            fail(e.getLocalizedMessage());
        }
    }

    /**
     * <p>
     * Helper method to dump XML data to system output.
     * </p>
     * 
     * @param document A target document to dump.
     */
    public static final void dumpXML(Document document) {
        try {
            XMLWriter formatter = new XMLWriter(System.out);
            // create SAX result
            SAXResult result = new SAXResult(formatter);
            result.setLexicalHandler(formatter);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), result);

            System.out.println("");
            System.out.println("");
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/08/29 20:19:07
     */
    private static class IgnoreWhitspaceFilter extends XMLFilterImpl {

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            for (int i = start; i < start + length; i++) {
                if (!Character.isWhitespace(ch[i])) {
                    super.characters(ch, start, length);
                    return;
                }
            }
        }
    }

    /**
     * @version 2010/01/08 15:56:46
     */
    private static class Namespaces implements NamespaceContext {

        /**
         * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
         */
        public String getNamespaceURI(String prefix) {
            if (prefix.equals("ez")) {
                return "http://ez.bean/";
            }
            return null;
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
         */
        public String getPrefix(String namespaceURI) {
            return null;
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
         */
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
