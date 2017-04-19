/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.BaseStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import kiss.model.Model;
import kiss.model.Property;

/**
 * <p>
 * Sinobu is not obsolete framework but utility, which can manipulate objects as a
 * extremely-condensed facade.
 * </p>
 * <dl>
 * <dt>Instantiation and Management</dt>
 * <dd>
 * <p>
 * Usually, the container which manages the object uses “get” method to provide such functionality.
 * However, Sinobu uses {@link #make(Class)}. This difference of method name indicates the
 * difference of the way for the management of objects, which also greatly affects the default
 * lifestyle (called Scope in other containers).
 * </p>
 * <p>
 * We do not provides the functionalities related to object lifecycle through Sinobu, because we
 * believe that it is better to use functionalities equipped in Java as much as possible. For
 * example, if you want to receive initialization callbacks, it is better to use constructor. If you
 * want to receive the destruction callbacks, it is better to use {@link #finalize()} method.
 * </p>
 * </dd>
 * <dt>Dependency Injection</dt>
 * <dd>
 * <p>
 * Sinobu supports Constructor Injection <em>only</em>. The Constructor Injection is a dependency
 * injection variant where an object gets all its dependencies via the constructor. We can say that
 * there is no possibility that Operation Injection, Field Injection and Interface Injection will be
 * supported in the days to come. This is one of the most important policy in Sinobu. The following
 * is a benefit of Constructor Injection:
 * </p>
 * <ul>
 * <li>It makes a strong dependency contract</li>
 * <li>It makes effective use of constructor in object lifecycle</li>
 * <li>It makes JavaBeans property clean</li>
 * <li>It makes testing easy, since dependencies can be passed in as Mock Object</li>
 * </ul>
 * <p>
 * The following is a deficit of Constructor Injection:
 * </p>
 * <ul>
 * <li>It can't resolve circular dependency</li>
 * </ul>
 * </dd>
 * </dl>
 * <h1 id="ConfigurableEnvironment">Configurable Environment</h1>
 * <p>
 * Sinobu provides some enviroment variables that you can configure.
 * </p>
 * <ul>
 * <li><a href="#encoding">Character Encoding</a></li>
 * <li><a href="#working">Working Directory</a></li>
 * <li><a href="#scheduler">Task Scheduler</a></li>
 * </ul>
 * <p>
 * When you want to initialize these enviroment variables and your application environment related
 * to Sinobu, you have to manipulate these variables at static initialization phase of your
 * application class.
 * </p>
 * <h2 id="Patterns">Include/Exclude Patterns</h2>
 * <p>
 * Sinobu adopts "glob" pattern matching instead of "regex". * The case-insensitivity is platform
 * dependent and therefore not specified. Example is the following that:
 * </p>
 * <dl>
 * <dt>*</dt>
 * <dd>Matches zero or more characters of a name component without crossing directory boundaries.
 * </dd>
 * <dt>**</dt>
 * <dd>Matches zero or more characters of a name component with crossing directory boundaries.</dd>
 * <dt>?</dt>
 * <dd>Matches exactly one character of a name component.</dd>
 * <dt>*.java</dt>
 * <dd>Matches a path that represents a file name ending with ".java" in the current directory.</dd>
 * <dt>**.java</dt>
 * <dd>Matches a path that represents a file name ending with ".java" in all directories.</dd>
 * <dt>!**.java</dt>
 * <dd>Matches file names <em>not</em> ending with ".java" in all directories.</dd>
 * <dt>**.*</dt>
 * <dd>Matches file names containing a dot.</dd>
 * <dt>**.{java,class}</dt>
 * <dd>Matches file names ending with ".java" or ".class".</dd>
 * <dt>**&#47;foo.?</dt>
 * <dd>Matches file names starting with "foo." and a single character extension.</dd>
 * </dl>
 * <p>
 * The backslash character (\) is used to escape characters that would otherwise be interpreted as
 * special characters. The expression \\ matches a single backslash and "\{" matches a left brace
 * for example.
 * </p>
 * <p>
 * The frequently used patterns are the followings:
 * </p>
 * <dl>
 * <dt>*</dt>
 * <dd>All children paths which are under the user specified path are matched. (descendant paths
 * will not match, root path will not match)</dd>
 * <dt>**</dt>
 * <dd>All descendant paths which are under the user specified path are matched. (root path will not
 * match)</dd>
 * <dt>*.txt</dt>
 * <dd>All children paths which are under the user specified path and have ".txt" suffix are
 * matched. (descendant paths will not match, root path will not match)</dd>
 * <dt>image*</dt>
 * <dd>All children paths which are under the user specified path and have "image" prefix are
 * matched. (descendant paths will not match, root path will not match)</dd>
 * <dt>**.html</dt>
 * <dd>All descendant paths which are under the user specified path and have ".html" suffix are
 * matched. (root path will not match)</dd>
 * </dl>
 * 
 * @version 2017/03/31 19:10:01
 */
public class I {

    // Candidates of Method Name
    //
    // annotate
    // bind
    // create class copy
    // delete define
    // edit error
    // find
    // get
    // hash have
    // i18n include
    // json join
    // kick
    // locate load log
    // make mock
    // n
    // observe
    // parse
    // quiet
    // read
    // save staple
    // transform
    // unload use
    // v
    // write weave warn walk watch
    // xml xerox
    // yield
    // zip

    /** No Operation */
    public static final Runnable NoOP = () -> {
        // no operation
    };

    /**
     * <p>
     * The configuration of charcter encoding in Sinobu, default value is <em>UTF-8</em>. It is
     * encouraged to use this encoding instead of platform default encoding when file I/O under the
     * Sinobu environment.
     * </p>
     */
    public static Charset $encoding = StandardCharsets.UTF_8;

    /** The configuration of root logger in Sinobu. */
    public static Logger $logger = Logger.getLogger("");

    /** The circularity dependency graph per thread. */
    static final ThreadSpecific<Deque<Class>> dependencies = new ThreadSpecific(ArrayDeque.class);

    /** The cache for {@link Lifestyle}. */
    private static final ClassVariable<Lifestyle> lifestyles = new ClassVariable<>();

    /** The extension file cache to check duplication. */
    private static final Set<String> extensionArea = new HashSet<>();

    /** The definitions for extensions. */
    private static final Map extensions = new HashMap();

    /** The lock for configurations. */
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The javascript engine for reuse. */
    private static final ScriptEngine script;

    // /** The locale name resolver. */
    // private static final Control control = Control.getControl(Control.FORMAT_CLASS);

    /** The daemon thread factory. */
    private static final ThreadFactory factory = run -> {
        Thread thread = new Thread(run);
        thread.setDaemon(true);
        return thread;
    };

    /** The parallel task manager. */
    private static final ScheduledExecutorService parallel = Executors.newScheduledThreadPool(4, factory);

    /** The serial task manager. */
    private static final ScheduledExecutorService serial = Executors.newSingleThreadScheduledExecutor(factory);

    /** The associatable object holder. */
    private static final WeakHashMap<Object, WeakHashMap> associatables = new WeakHashMap();

    /** The document builder. */
    static final DocumentBuilder dom;

    /** The xpath evaluator. */
    static final XPath xpath;

    /** The list of primitive classes. (except for void type) */
    private static final Class[] primitives = {boolean.class, int.class, long.class, float.class, double.class, byte.class, short.class,
            char.class, void.class};

    /** The list of wrapper classes. (except for void type) */
    private static final Class[] wrappers = {Boolean.class, Integer.class, Long.class, Float.class, Double.class, Byte.class, Short.class,
            Character.class, Void.class};

    /**
     * The date format for W3CDTF. Date formats are not synchronized. It is recommended to create
     * separate format instances for each thread. If multiple threads access a format concurrently,
     * it must be synchronized externally.
     */
    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    // initialization
    static {
        // remove all built-in log handlers
        for (Handler h : $logger.getHandlers()) {
            $logger.removeHandler(h);
        }

        // switch err temporaly to create console handler with System.out stream
        PrintStream error = System.err;
        System.setErr(System.out);
        config(new ConsoleHandler());
        System.setErr(error);

        // built-in lifestyles
        lifestyles.set(List.class, ArrayList::new);
        lifestyles.set(Map.class, HashMap::new);
        lifestyles.set(Set.class, HashSet::new);
        lifestyles.set(Lifestyle.class, new Prototype(Prototype.class));
        lifestyles.set(Prototype.class, new Prototype(Prototype.class));
        lifestyles.set(ListProperty.class, () -> new SimpleListProperty(FXCollections.observableArrayList()));
        lifestyles.set(ObservableList.class, FXCollections::observableArrayList);
        lifestyles.set(MapProperty.class, () -> new SimpleMapProperty(FXCollections.observableHashMap()));
        lifestyles.set(ObservableMap.class, FXCollections::observableHashMap);
        lifestyles.set(SetProperty.class, () -> new SimpleSetProperty(FXCollections.observableSet()));
        lifestyles.set(ObservableSet.class, FXCollections::observableSet);

        try {
            // configure dom builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            dom = factory.newDocumentBuilder();
            dom.setErrorHandler(new DefaultHandler());
            xpath = XPathFactory.newInstance().newXPath();
        } catch (Exception e) {
            throw I.quiet(e);
        }

        // configure javascript engine
        script = new ScriptEngineManager().getEngineByName("js");

        // Load myself as module. All built-in classload listeners and extension points will be
        // loaded and activated.
        load(I.class, true);

        // built-in encoders
        load(ExtensionFactory.class, Encoder.class, () -> (ExtensionFactory<Encoder>) type -> {
            if (type.isEnum()) {
                return value -> ((Enum) value).name();
            }

            switch (type.getName().hashCode()) {
            case -530663260: // java.lang.Class
                return value -> ((Class) value).getName();
            case 65575278: // java.util.Date
                // return LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC).toString();
                return format::format;

            default:
                return String::valueOf;
            }
        });

        // built-in decoders
        load(ExtensionFactory.class, Decoder.class, () -> (ExtensionFactory<Decoder>) type -> {
            if (type.isEnum()) {
                return value -> Enum.valueOf((Class<Enum>) type, value);
            }
            switch (type.getName().hashCode()) {
            case 64711720: // boolean
            case 344809556: // java.lang.Boolean
                return Boolean::new;
            case 104431: // int
            case -2056817302: // java.lang.Integer
                return Integer::new;
            case 3327612: // long
            case 398795216: // java.lang.Long
                return Long::new;
            case 97526364: // float
            case -527879800: // java.lang.Float
                return Float::new;
            case -1325958191: // double
            case 761287205: // java.lang.Double
                return Double::new;
            case 3039496: // byte
            case 398507100: // java.lang.Byte
                return Byte::new;
            case 109413500: // short
            case -515992664: // java.lang.Short
                return Short::new;
            case 3052374: // char
            case 155276373: // java.lang.Character
                return value -> value.charAt(0);
            case -530663260: // java.lang.Class
                return I::type;
            case 1195259493: // java.lang.String
                return String::new;
            case -1555282570: // java.lang.StringBuilder
                return StringBuilder::new;
            case 1196660485: // java.lang.StringBuffer
                return StringBuffer::new;
            case 2130072984: // java.io.File
                return File::new;
            case 2050244018: // java.net.URL
                return value -> {
                    try {
                        return new URL(value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };
            case 2050244015: // java.net.URI
                return URI::create;
            case -989675752: // java.math.BigInteger
                return BigInteger::new;
            case -1405464277: // java.math.BigDecimal
                return BigDecimal::new;
            case 65575278: // java.util.Date
                // return Date.from(LocalDateTime.parse(value).toInstant(ZoneOffset.UTC));
                return value -> {
                    try {
                        return format.parse(value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };
            case -1246033885: // java.time.LocalTime
                return LocalTime::parse;
            case -1246518012: // java.time.LocalDate
                return LocalDate::parse;
            case -1179039247: // java.time.LocalDateTime
                return LocalDateTime::parse;
            case -682591005: // java.time.OffsetDateTime
                return OffsetDateTime::parse;
            case -1917484011: // java.time.OffsetTime
                return OffsetTime::parse;
            case 1505337278: // java.time.ZonedDateTime
                return ZonedDateTime::parse;
            case 649475153: // java.time.MonthDay
                return MonthDay::parse;
            case -537503858: // java.time.YearMonth
                return YearMonth::parse;
            case -1062742510: // java.time.Year
                return Year::parse;
            case -1023498007: // java.time.Duration
                return Duration::parse;
            case 649503318: // java.time.Period
                return Period::parse;
            case 1296075756: // java.time.Instant
                return Instant::parse;
            case -1165211622: // java.util.Locale
                return Locale::forLanguageTag;

            // case 1464606545: // java.nio.file.Path
            // case -2015077501: // sun.nio.fs.WindowsPath
            // return Paths::get;
            // case -89228377: // java.nio.file.attribute.FileTime
            // decoder = value -> FileTime.fromMillis(Long.valueOf(value));
            // encoder = (Encoder<FileTime>) value -> String.valueOf(value.toMillis());
            // break;
            default:
                return null;
            }
        });
    }

    /**
     * <p>
     * Initialize environment.
     * </p>
     */
    private I() {
    }

    /**
     * <p>
     * Create {@link Predicate} which accepts any item.
     * </p>
     * 
     * @return An acceptable {@link Predicate}.
     */
    public static <V> Predicate<V> accept() {
        return e -> true;
    }

    /**
     * <p>
     * Write {@link Level#SEVERE} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void alert(String message) {
        $logger.logp(Level.SEVERE, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#SEVERE} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void alert(String message, Object... params) {
        $logger.logp(Level.SEVERE, "", "", message, params);
    }

    /**
     * <p>
     * Retrieve the associated value with the specified object by the specified type.
     * </p>
     *
     * @param host A host object.
     * @param type An association type.
     * @return An associated value.
     */
    public static <V> V associate(Object host, Class<V> type) {
        WeakHashMap<Class<V>, V> association = associatables.computeIfAbsent(host, key -> new WeakHashMap());
        return association.computeIfAbsent(type, I::make);
    }

    /**
     * <p>
     * Create the partial applied {@link Consumer}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param> Runnable bind(Consumer<Param> function, Param param) {
        return function == null ? NoOP : () -> function.accept(param);
    }

    /**
     * <p>
     * Create the partial applied {@link Consumer}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param1, Param2> Runnable bind(BiConsumer<Param1, Param2> function, Param1 param1, Param2 param2) {
        return function == null ? NoOP : () -> function.accept(param1, param2);
    }

    /**
     * <p>
     * Create the partial applied {@link Function}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param, Return> Supplier<Return> bind(Function<Param, Return> function, Param param) {
        Objects.requireNonNull(function);
        return () -> function.apply(param);
    }

    /**
     * <p>
     * Create the partial applied {@link Function}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param1, Param2, Return> Supplier<Return> bind(BiFunction<Param1, Param2, Return> function, Param1 param1, Param2 param2) {
        Objects.requireNonNull(function);
        return () -> function.apply(param1, param2);
    }

    /**
     * <p>
     * Bundle all given funcitons into single function.
     * </p>
     * 
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(F... functions) {
        return bundle((Class<F>) functions.getClass().getComponentType(), functions);
    }

    /**
     * <p>
     * Bundle all given funcitons into single function.
     * </p>
     * 
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(Collection<F> functions) {
        return bundle(findNCA(functions, Class::isInterface), functions);
    }

    // /**
    // * <p>
    // * Find the nearest common ancestor class of the given classes.
    // * </p>
    // *
    // * @param <X> A type.
    // * @param classes A list of classes.
    // * @return A nearest common ancestor class.
    // */
    // private static <X> Class findNCA(X... classes) {
    // return classes.getClass().getComponentType();
    // }

    /**
     * <p>
     * Find the nearest common ancestor class of the given classes.
     * </p>
     * 
     * @param <X> A type.
     * @param items A list of items.
     * @return A nearest common ancestor class.
     */
    private static <X> Class<X> findNCA(Collection<X> items, Predicate<Class> filter) {
        if (filter == null) {
            filter = accept();
        }

        Set<Class> types = null;
        Iterator<X> iterator = items.iterator();

        if (iterator.hasNext()) {
            types = Model.collectTypes(iterator.next().getClass());
            types.removeIf(filter.negate());

            while (iterator.hasNext()) {
                types.retainAll(Model.collectTypes(iterator.next().getClass()));
            }
        }
        return types == null || types.isEmpty() ? null : types.iterator().next();
    }

    /**
     * <p>
     * Bundle all given typed funcitons into single typed function.
     * </p>
     * 
     * @param type A function type.
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(Class<F> type, F... functions) {
        return bundle(type, Arrays.asList(functions));
    }

    /**
     * <p>
     * Bundle all given typed funcitons into single typed function.
     * </p>
     * 
     * @param type A function type.
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(Class<F> type, Collection<F> functions) {
        return make(type, (proxy, method, args) -> {
            Object result = null;

            if (functions != null) {
                for (Object fun : functions) {
                    if (fun != null) {
                        result = method.invoke(fun, args);
                    }
                }
            }
            return result;
        });
    }

    /**
     * <p>
     * Create the specified {@link Collection} with the specified items.
     * </p>
     * 
     * @param type A {@link Collection} type.
     * @param items A list of itmes.
     * @return The new created {@link Collection}.
     */
    public static <T extends Collection<V>, V> T collect(Class<T> type, V... items) {
        T collection = I.make(type);

        if (items != null) {
            for (V item : items) {
                collection.add(item);
            }
        }
        return collection;
    }

    /**
     * <p>
     * Rgister the specified log handler.
     * </p>
     * 
     * @param handler
     */
    public static void config(Handler handler) {
        handler.setFormatter(new Log());
        $logger.addHandler(handler);
    }

    /**
     * <p>
     * Note : This method closes both input and output stream carefully.
     * </p>
     * <p>
     * Copy bytes from a {@link InputStream} to an {@link OutputStream}. This method buffers the
     * input internally, so there is no need to use a buffered stream.
     * </p>
     *
     * @param input A {@link InputStream} to read from.
     * @param output An {@link OutputStream} to write to.
     * @param close Whether input and output steream will be closed automatically or not.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the input or output is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static void copy(InputStream input, OutputStream output, boolean close) {
        int size;
        byte[] buffer = new byte[8192];

        try {
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
        } catch (IOException e) {
            throw quiet(e);
        } finally {
            if (close) {
                quiet(input);
                quiet(output);
            }
        }
    }

    /**
     * <p>
     * Copy data from a {@link Readable} to an {@link Appendable}. This method buffers the input
     * internally, so there is no need to use a buffer.
     * </p>
     *
     * @param input A {@link Readable} to read from.
     * @param output An {@link Appendable} to write to.
     * @param close Whether input and output steream will be closed automatically or not.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the input or output is null.
     */
    public static void copy(Readable input, Appendable output, boolean close) {
        int size;
        CharBuffer buffer = CharBuffer.allocate(8192);

        try {
            while ((size = input.read(buffer)) != -1) {
                buffer.flip();
                output.append(buffer, 0, size);
                buffer.clear();
            }
        } catch (IOException e) {
            throw quiet(e);
        } finally {
            if (close) {
                quiet(input);
                quiet(output);
            }
        }
    }

    /**
     * <p>
     * Write {@link Level#FINEST} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void debug(String message) {
        $logger.logp(Level.FINEST, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#FINEST} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void debug(String message, Object... params) {
        $logger.logp(Level.FINEST, "", "", message, params);
    }

    /**
     * <p>
     * Find all <a href="Extensible.html#Extension">Extensions</a> which are specified by the given
     * <a href="Extensible#ExtensionPoint">Extension Point</a>.
     * </p>
     * <p>
     * The returned list will be "safe" in that no references to it are maintained by Sinobu. (In
     * other words, this method must allocate a new list). The caller is thus free to modify the
     * returned list.
     * </p>
     *
     * @param <E> An Extension Point.
     * @param extensionPoint An extension point class. The
     *            <a href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return empty list.
     * @return All Extensions of the given Extension Point or empty list.
     */
    public static <E extends Extensible> List<E> find(Class<E> extensionPoint) {
        return I.signal(findBy(extensionPoint)).flatIterable(Ⅱ::ⅰ).map(I::make).toList();
    }

    /**
     * <p>
     * Find the <a href="Extensible.html#Extension">Extension</a> which are specified by the given
     * <a href="Extensible#ExtensionPoint">Extension Point</a> and the given key.
     * </p>
     *
     * @param <E> An Extension Point.
     * @param extensionPoint An Extension Point class. The
     *            <a href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return <code>null</code>.
     * @param key An <a href="Extensible.html#ExtensionKey">Extension Key</a> class.
     * @return A associated Extension of the given Extension Point and the given Extension Key or
     *         <code>null</code>.
     */
    public static <E extends Extensible> E find(Class<E> extensionPoint, Class key) {
        if (extensionPoint != null && key != null) {
            Ⅱ<List<Class<E>>, Map<Class, Supplier<E>>> extensions = findBy(extensionPoint);

            for (Class type : Model.collectTypes(key)) {
                Supplier<E> supplier = extensions.ⅱ.get(type);

                if (supplier != null) {
                    return supplier.get();
                }
            }

            if (extensionPoint != ExtensionFactory.class) {
                ExtensionFactory<E> factory = find(ExtensionFactory.class, extensionPoint);

                if (factory != null) {
                    return factory.create(key);
                }
            }
        }
        return null;
    }

    /**
     * <p>
     * Find all <a href="Extensible.html#Extension">Extensions</a> classes which are specified by
     * the given <a href="Extensible#ExtensionPoint">Extension Point</a>.
     * </p>
     * <p>
     * The returned list will be "safe" in that no references to it are maintained by Sinobu. (In
     * other words, this method must allocate a new list). The caller is thus free to modify the
     * returned list.
     * </p>
     *
     * @param <E> An Extension Point.
     * @param extensionPoint An extension point class. The
     *            <a href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return empty list.
     * @return All Extension classes of the given Extension Point or empty list.
     * @throws NullPointerException If the Extension Point is <code>null</code>.
     */
    public static <E extends Extensible> List<Class<E>> findAs(Class<E> extensionPoint) {
        return new ArrayList(findBy(extensionPoint).ⅰ);
    }

    /**
     * <p>
     * Find the extension definition for the specified extension point.
     * </p>
     * 
     * @param extensionPoint A target extension point.
     * @return A extension definition.
     */
    private static <E extends Extensible> Ⅱ<List<Class<E>>, Map<Class, Supplier<E>>> findBy(Class<E> extensionPoint) {
        return (Ⅱ) extensions.computeIfAbsent(extensionPoint, p -> pair(new ArrayList(), new HashMap()));
    }

    public static <P> Consumer<P> imitateConsumer(Runnable lambda) {
        return p -> {
            if (lambda != null) lambda.run();
        };
    }

    public static <P, R> Function<P, R> imitateFunction(Consumer<P> function) {
        return p -> {
            function.accept(p);
            return null;
        };
    }

    public static <P, R> Function<P, R> imitateFunction(Supplier<R> function) {
        return p -> function.get();
    }

    /**
     * <p>
     * Returns a string containing the string representation of each of items, using the specified
     * separator between each.
     * </p>
     *
     * @param delimiter A sequence of characters that is used to separate each of the elements in
     *            the resulting String.
     * @param items A {@link Iterable} items.
     * @return A concat expression.
     */
    public static String join(CharSequence delimiter, Iterable items) {
        if (items == null) {
            return "";
        }

        if (delimiter == null) {
            delimiter = "";
        }

        StringBuilder builder = new StringBuilder();
        Iterator iterator = items.iterator();

        if (iterator.hasNext()) {
            builder.append(iterator.next());

            while (iterator.hasNext()) {
                builder.append(delimiter).append(iterator.next());
            }
        }
        return builder.toString();
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(File input) {
        return parseJSON(input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(InputStream input) {
        return parseJSON(input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(Readable input) {
        return parseJSON(input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(URL input) {
        return parseJSON(input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(URI input) {
        return parseJSON(input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(CharSequence input) {
        return parseJSON(input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * <ul>
     * <li>{@link JSON}</li>
     * <li>{@link File}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link Readable}</li>
     * <li>{@link URL}</li>
     * <li>{@link URI}</li>
     * <li>{@link CharSequence}</li>
     * </ul>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    private static JSON parseJSON(Object input) {
        PushbackReader reader = null;

        try {
            // aquire lock
            lock.readLock().lock();

            // Parse as JSON
            reader = new PushbackReader(new InputStreamReader(new ByteArrayInputStream(read(input)), $encoding), 2);
            reader.unread(new char[] {'a', '='});
            return new JSON(script.eval(reader));
        } catch (Exception e) {
            throw quiet(e);
        } finally {
            // relese lock
            lock.readLock().unlock();

            // close carefuly
            quiet(reader);
        }
    }

    /**
     * <p>
     * Create {@link ArrayList} with the specified items.
     * </p>
     * 
     * @param items A list of itmes.
     * @return The new created {@link ArrayList}.
     */
    public static <V> List<V> list(V... items) {
        return collect(ArrayList.class, items);
    }

    /**
     * <p>
     * Load {@link Extensible} typs from the path that the specified class indicates to. If same
     * path and patten is already called , that will do nothing at all.
     * </p>
     *
     * @param path A path to load.
     * @return The unloader.
     * @see {@link Extensible}
     * @see #find(Class)
     * @see #find(Class, Class)
     * @see #findAs(Class)
     */
    public static <E extends Extensible> Disposable load(Class path, boolean filter) {
        Disposable disposer = Disposable.empty();
        String pattern = filter ? path.getPackage().getName() : "";

        try {
            File file = new File(path.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile();

            // exclude registered file or directory
            if (!extensionArea.add(file + pattern)) {
                return disposer;
            }
            disposer.add(() -> extensionArea.remove(file + pattern));

            int prefix = file.getPath().length() + 1;

            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Sinobu automatically try to switch to other file system (e.g.
            // ZipFileSystem).
            Signal<String> names = file.isFile() ? I.signal(new ZipFile(file).entries()).map(ZipEntry::getName)
                    : I.signal(scan(file, new ArrayList<>())).map(File::getPath).map(name -> name.substring(prefix));

            names.to(name -> {
                // exclude non-class files
                if (!name.endsWith(".class")) {
                    return;
                }

                name = name.substring(0, name.length() - 6).replace('/', '.').replace(File.separatorChar, '.');

                // exclude out of the specified package
                if (!name.startsWith(pattern)) {
                    return;
                }

                Class extension = I.type(name);

                // fast check : exclude non-initializable class
                if (extension.isEnum() || extension.isAnonymousClass() || Modifier.isAbstract(extension.getModifiers())) {
                    return;
                }

                // slow check : exclude non-extensible class
                if (!Extensible.class.isAssignableFrom(extension)) {
                    return;
                }

                // search and collect information for all extension points
                for (Class<E> extensionPoint : Model.collectTypes(extension)) {
                    if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                        // register as new extension
                        findBy(extensionPoint).ⅰ.add(extension);
                        disposer.add(() -> findBy(extensionPoint).ⅰ.remove(extension));

                        // register extension key
                        java.lang.reflect.Type[] params = Model.collectParameters(extension, extensionPoint);

                        if (params.length != 0 && params[0] != Object.class) {
                            Class clazz = (Class) params[0];

                            // register extension by key
                            disposer.add(load(extensionPoint, clazz, () -> (E) I.make(extension)));

                            // The user has registered a newly custom lifestyle, so we
                            // should update lifestyle for this extension key class.
                            // Normally, when we update some data, it is desirable to store
                            // the previous data to be able to restore it later.
                            // But, in this case, the contextual sensitive instance that
                            // the lifestyle emits changes twice on "load" and "unload"
                            // event from the point of view of the user.
                            // So the previous data becomes all but meaningless for a
                            // cacheable lifestyles (e.g. Singleton and ThreadSpecifiec).
                            // Therefore we we completely refresh lifestyles associated with
                            // this extension key class.
                            if (extensionPoint == Lifestyle.class) {
                                lifestyles.remove(clazz);
                                disposer.add(() -> lifestyles.remove(clazz));
                            }
                        }
                    }
                }
            });

            return disposer;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Register extension with key.
     * </p>
     * 
     * @param extensionPoint A extension point.
     * @param extensionKey A extension key,
     * @param extension A extension to register.
     * @return A disposer to unregister.
     */
    public static <E extends Extensible> Disposable load(Class<E> extensionPoint, Class extensionKey, Supplier<E> extension) {
        findBy(extensionPoint).ⅱ.put(extensionKey, extension);
        return () -> findBy(extensionPoint).ⅱ.remove(extensionKey);
    }

    /**
     * <p>
     * Dig class files in directory.
     * </p>
     *
     * @param file A current location.
     * @param files A list of collected files.
     * @return A list of collected files.
     */
    private static List<File> scan(File file, List<File> files) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                scan(sub, files);
            }
        } else {
            files.add(file);
        }
        return files;
    }

    /**
     * <p>
     * Write {@link Level#INFO} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void log(String message) {
        $logger.logp(Level.INFO, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#INFO} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void log(String message, Object... params) {
        $logger.logp(Level.INFO, "", "", message, params);
    }

    /**
     * <p>
     * Returns a new or cached instance of the model class.
     * </p>
     * <p>
     * This method supports the top-level class and the member type. If the local class or the
     * anonymous class is passed to this argument, {@link UnsupportedOperationException} will be
     * thrown. There is a possibility that a part of this limitation will be removed in the future.
     * </p>
     *
     * @param <M> A model type.
     * @param modelClass A target class to create instance.
     * @return A instance of the specified model class. This instance is managed by Sinobu.
     * @throws NullPointerException If the model class is <code>null</code>.
     * @throws IllegalArgumentException If the model class is non-accessible or final class.
     * @throws UnsupportedOperationException If the model class is inner-class.
     * @throws ClassCircularityError If the model has circular dependency.
     * @throws InstantiationException If Sinobu can't instantiate(resolve) the model class.
     */
    public static <M> M make(Class<? extends M> modelClass) {
        return makeLifestyle(modelClass).get();
    }

    /**
     * <p>
     * Create proxy instance.
     * </p>
     * 
     * @param type A model type.
     * @param handler A proxy handler.
     * @return
     */
    public static <T> T make(Class<T> type, InvocationHandler handler) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(handler);

        if (type.isInterface() == false) {
            throw new IllegalArgumentException("Type must be interface.");
        }
        return (T) Proxy.newProxyInstance(I.class.getClassLoader(), new Class[] {type}, handler);
    }

    /**
     * <p>
     * Returns a new or cached instance of the model class.
     * </p>
     * <p>
     * This method supports the top-level class and the member type. If the local class or the
     * anonymous class is passed to this argument, {@link UnsupportedOperationException} will be
     * thrown. There is a possibility that a part of this limitation will be removed in the future.
     * </p>
     *
     * @param <M> A model class.
     * @return A instance of the specified model class. This instance is managed by Sinobu.
     * @throws NullPointerException If the model class is <code>null</code>.
     * @throws IllegalArgumentException If the model class is non-accessible or final class.
     * @throws UnsupportedOperationException If the model class is inner-class.
     * @throws ClassCircularityError If the model has circular dependency.
     * @throws InstantiationException If Sinobu can't instantiate(resolve) the model class.
     */
    static <M> Lifestyle<M> makeLifestyle(Class<M> modelClass) {
        // Skip null check because this method can throw NullPointerException.
        // if (modelClass == null) throw new NullPointerException("NPE");

        // At first, we must confirm the cached lifestyle associated with the model class. If
        // there is no such cache, we will try to create newly lifestyle.
        Lifestyle<M> lifestyle = lifestyles.get(modelClass);

        if (lifestyle != null) return lifestyle; // use cache

        // Skip null check because this method can throw NullPointerException.
        // if (modelClass == null) throw new NullPointerException("NPE");

        // The model class have some preconditions to have to meet.
        if (modelClass.isLocalClass()) {
            throw new UnsupportedOperationException(modelClass + " is  inner class.");
        }

        // Construct dependency graph for the current thred.
        Deque<Class> dependency = dependencies.get();
        dependency.add(modelClass);

        // Don't use 'contains' method check here to resolve singleton based
        // circular reference. So we must judge it from the size of context. If the
        // context contains too many classes, it has a circular reference
        // independencies.
        if (16 < dependency.size()) {
            // Deque will be contain repeated Classes so we must shrink it with
            // maintaining its class order.
            throw new ClassCircularityError(new LinkedHashSet(dependency).toString());
        }

        try {
            // At first, we should search the associated lifestyle from extension points.
            lifestyle = find(Lifestyle.class, modelClass);

            // Then, check its Manageable annotation.
            if (lifestyle == null) {
                // If the actual model class doesn't provide its lifestyle explicitly, we use
                // Prototype lifestyle which is default lifestyle in Sinobu.
                Manageable manageable = modelClass.getAnnotation(Manageable.class);

                // Create new lifestyle for the actual model class
                lifestyle = (Lifestyle) make((Class) (manageable == null ? Prototype.class : manageable.lifestyle()));
            }

            // Trace dependency graph to detect circular dependencies.
            if (lifestyle instanceof Prototype) {
                for (Class param : ((Prototype) lifestyle).instantiator.getParameterTypes()) {
                    if (param != Class.class) {
                        makeLifestyle(param);
                    }
                }
            }

            // This lifestyle is safe and has no circular dependencies.
            return lifestyles.let(modelClass, lifestyle);
        } finally {
            dependency.pollLast();
        }
    }

    //
    // /**
    // * <p>
    // * Parse the specified xml {@link Path} using the specified sequence of {@link
    // XMLFilter} .
    // The
    // * application can use this method to instruct the XML reader to begin parsing an
    // XML document
    // * from the specified path.
    // * </p>
    // * <p>
    // * Sinobu use the {@link XMLReader} which has the following features.
    // * </p>
    // * <ul>
    // * <li>Support XML namespaces.</li>
    // * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
    // Version
    // * 1.0</a>.</li>
    // * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
    // * <li><em>Not</em> support external DTD completely (parser doesn't even access
    // DTD, using
    // * "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    // feature).</li>
    // * </ul>
    // *
    // * @param source A path to xml source.
    // * @param filters A list of filters to parse a sax event. This may be
    // <code>null</code>.
    // * @throws NullPointerException If the specified source is <code>null</code>. If
    // one of the
    // * specified filter is <code>null</code>.
    // * @throws SAXException Any SAX exception, possibly wrapping another exception.
    // * @throws IOException An IO exception from the parser, possibly from a byte
    // stream or
    // character
    // * stream supplied by the application.
    // */
    // public static void parse(Path source, XMLFilter... filters) {
    // try {
    // InputSource input = new InputSource(Files.newBufferedReader(source, $encoding));
    // input.setPublicId(source.toString());
    //
    // parse(input, filters);
    // } catch (Exception e) {
    // throw quiet(e);
    // }
    // }
    //
    // /**
    // * <p>
    // * Parse the specified xml {@link InputSource} using the specified sequence of
    // {@link
    // XMLFilter}
    // * . The application can use this method to instruct the XML reader to begin
    // parsing an XML
    // * document from any valid input source (a character stream, a byte stream, or a
    // URI).
    // * </p>
    // * <p>
    // * Sinobu use the {@link XMLReader} which has the following features.
    // * </p>
    // * <ul>
    // * <li>Support XML namespaces.</li>
    // * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
    // Version
    // * 1.0</a>.</li>
    // * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
    // * <li><em>Not</em> support external DTD completely (parser doesn't even access
    // DTD, using
    // * "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    // feature).</li>
    // * </ul>
    // *
    // * @param source A xml source.
    // * @param filters A list of filters to parse a sax event. This may be
    // <code>null</code>.
    // * @throws NullPointerException If the specified source is <code>null</code>. If
    // one of the
    // * specified filter is <code>null</code>.
    // * @throws SAXException Any SAX exception, possibly wrapping another exception.
    // * @throws IOException An IO exception from the parser, possibly from a byte
    // stream or
    // character
    // * stream supplied by the application.
    // */
    // public static void parse(InputSource source, XMLFilter... filters) {
    // try {
    // // create new xml reader
    // XMLReader reader = sax.newSAXParser().getXMLReader();
    //
    // // chain filters if needed
    // for (int i = 0; i < filters.length; i++) {
    // // find the root filter of the current multilayer filter
    // XMLFilter filter = filters[i];
    //
    // while (filter.getParent() instanceof XMLFilter) {
    // filter = (XMLFilter) filter.getParent();
    // }
    //
    // // the root filter makes previous filter as parent xml reader
    // filter.setParent(reader);
    //
    // if (filter instanceof LexicalHandler) {
    // reader.setProperty("http://xml.org/sax/properties/lexical-handler", filter);
    // }
    //
    // // current filter is a xml reader in next step
    // reader = filters[i];
    // }
    //
    // // start parsing
    // reader.parse(source);
    // } catch (Exception e) {
    // // We must throw the checked exception quietly and pass the original exception
    // instead
    // // of wrapped exception.
    // throw quiet(e);
    // }
    // }

    /**
     * <p>
     * Observe the specified {@link ObservableValue}.
     * </p>
     * <p>
     * An implementation of {@link ObservableValue} may support lazy evaluation, which means that
     * the value is not immediately recomputed after changes, but lazily the next time the value is
     * requested.
     * </p>
     *
     * @param observable A target to observe.
     * @return A observable event stream.
     */
    public static <E extends Observable> Signal<E> observe(E observable) {
        if (observable == null) {
            return Signal.NEVER;
        }

        return new Signal<>((observer, disposer) -> {
            // create actual listener
            InvalidationListener listener = value -> observer.accept((E) value);

            // INITIALIZING STAGE : register listener and notify the current value
            observable.addListener(listener);
            observer.accept(observable);

            // DISPOSING STAGE : unregister listener
            return () -> observable.removeListener(listener);
        });
    }

    /**
     * <p>
     * Observe the specified {@link ObservableValue}.
     * </p>
     * <p>
     * An implementation of {@link ObservableValue} may support lazy evaluation, which means that
     * the value is not immediately recomputed after changes, but lazily the next time the value is
     * requested.
     * </p>
     *
     * @param observable A target to observe.
     * @return A observable event stream.
     */
    public static <E> Signal<E> observe(ObservableValue<E> observable) {
        if (observable == null) {
            return Signal.NEVER;
        }

        return new Signal<>((observer, disposer) -> {
            // create actual listener
            ChangeListener<E> listener = (o, oldValue, newValue) -> observer.accept(newValue);

            // INITIALIZING STAGE : register listener and notify the current value
            observable.addListener(listener);
            observer.accept(observable.getValue());

            // DISPOSING STAGE : unregister listener
            return () -> observable.removeListener(listener);
        });
    }

    /**
     * <p>
     * Create value set.
     * </p>
     *
     * @param param1 A first parameter.
     * @param param2 A second parameter.
     * @return
     */
    public static <Param1, Param2> Ⅱ<Param1, Param2> pair(Param1 param1, Param2 param2) {
        return new Ⅱ(param1, param2);
    }

    /**
     * <p>
     * Create value set.
     * </p>
     *
     * @param param1 A first parameter.
     * @param param2 A second parameter.
     * @param param3 A third parameter.
     * @return
     */
    public static <Param1, Param2, Param3> Ⅲ<Param1, Param2, Param3> pair(Param1 param1, Param2 param2, Param3 param3) {
        return new Ⅲ(param1, param2, param3);
    }

    /**
     * <p>
     * Create paired value {@link Consumer}.
     * </p>
     *
     * @param consumer A {@link BiConsumer} to make parameters paired.
     * @return A paired value {@link Consumer}.
     */
    public static <Param1, Param2> Consumer<Ⅱ<Param1, Param2>> pair(BiConsumer<Param1, Param2> consumer) {
        return params -> consumer.accept(params.ⅰ, params.ⅱ);
    }

    /**
     * <p>
     * Create paired value {@link Function}.
     * </p>
     *
     * @param funtion A {@link BiFunction} to make parameters paired.
     * @return A paired value {@link Function}.
     */
    public static <Param1, Param2, Return> Function<Ⅱ<Param1, Param2>, Return> pair(BiFunction<Param1, Param2, Return> funtion) {
        return params -> funtion.apply(params.ⅰ, params.ⅱ);
    }

    /**
     * <p>
     * Close the specified object quietly if it is {@link AutoCloseable}. Equivalent to
     * {@link AutoCloseable#close()}, except any exceptions will be ignored. This is typically used
     * in finally block like the following.
     * </p>
     * <p>
     * <pre>
     * AutoCloseable input = null;
     *
     * try {
     *     // some IO action
     * } catch (Exception e) {
     *     throw e;
     * } finally {
     *     I.quiet(input);
     * }
     * </pre>
     * <p>
     * Throw the specified checked exception quietly or close the specified {@link AutoCloseable}
     * object quietly.
     * </p>
     * <p>
     * This method <em>doesn't</em> wrap checked exception around unchecked exception (e.g. new
     * RuntimeException(e)) and <em>doesn't</em> shelve it. This method deceive the compiler that
     * the checked exception is unchecked one. So you can catch a raw checked exception in the
     * caller of the method which calls this method.
     * </p>
     * <p>
     * <pre>
     * private void callerWithoutErrorHandling() {
     *     methodQuietly();
     * }
     *
     * private void callerWithErrorHandling() {
     *     try {
     *         methodQuietly();
     *     } catch (Exception e) {
     *         // you can catch the checked exception here
     *     }
     * }
     *
     * private void methodQuietly() {
     *     try {
     *         // throw some cheched exception
     *     } catch (CheckedException e) {
     *         throw I.quiet(e); // rethrow checked exception quietly
     *     }
     * }
     * </pre>
     *
     * @param object A exception to throw quietly or a object to close quietly.
     * @return A pseudo unchecked exception.
     * @throws NullPointerException If the specified exception is <code>null</code>.
     */
    public static RuntimeException quiet(Object object) {
        if (object instanceof Throwable) {
            Throwable throwable = (Throwable) object;

            // retrieve original exception from the specified wrapped exception
            if (throwable instanceof InvocationTargetException) throwable = throwable.getCause();

            // throw quietly
            return I.<RuntimeException> quietly(throwable);
        }

        if (object instanceof AutoCloseable) {
            try {
                ((AutoCloseable) object).close();
            } catch (Exception e) {
                throw quiet(e);
            }
        }

        // API definition
        return null;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static Runnable quiet(UsefulRunnable lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P> Consumer<P> quiet(UsefulConsumer<P> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P1, P2> BiConsumer<P1, P2> quiet(UsefulBiConsumer<P1, P2> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <R> Supplier<R> quiet(UsefulSupplier<R> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P, R> Function<P, R> quiet(UsefulFunction<P, R> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P1, P2, R> BiFunction<P1, P2, R> quiet(UsefulBiFunction<P1, P2, R> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Deceive complier that the specified checked exception is unchecked exception.
     * </p>
     *
     * @param <T> A dummy type for {@link RuntimeException}.
     * @param throwable Any error.
     * @return A runtime error.
     * @throws T Dummy error to deceive compiler.
     */
    private static <T extends Throwable> T quietly(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * <p>
     * Reads Java object tree from the given XML or JSON input.
     * </p>
     *
     * @param input A serialized Java object tree data as XML or JSON. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link ScriptException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static <M> M read(CharSequence input, M output) {
        return json(input).to(output);
    }

    /**
     * <p>
     * Reads Java object tree from the given XML or JSON input.
     * </p>
     * <p>
     * If the input object implements {@link AutoCloseable}, {@link AutoCloseable#close()} method
     * will be invoked certainly.
     * </p>
     *
     * @param input A serialized Java object tree data as XML or JSON. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link ScriptException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IOError If the input data is empty or invalid format.
     */
    public static <M> M read(Readable input, M output) {
        return json(input).to(output);
    }

    /**
     * <p>
     * Create {@link Predicate} which rejects any item.
     * </p>
     * 
     * @return An rejectable {@link Predicate}.
     */
    public static <V> Predicate<V> reject() {
        return e -> false;
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @param recovery A recovery operation builder.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> recoverWhen(Class<? extends Throwable> type, UnaryOperator<O> recovery) {
        return recoverWhen(type, Integer.MAX_VALUE, recovery);
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @param limit A limit number of trials.
     * @param recovery A recovery operation builder.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> recoverWhen(Class<? extends Throwable> type, int limit, UnaryOperator<O> recovery) {
        return (o, e, i) -> type.isInstance(e) && i < limit ? recovery.apply(o) : null;
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> retryWhen(Class<? extends Throwable> type) {
        return retryWhen(type, Integer.MAX_VALUE);
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @param limit A limit number of trials.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> retryWhen(Class<? extends Throwable> type, int limit) {
        return (o, e, i) -> type.isInstance(e) && i < limit ? o : null;
    }

    /**
     * <p>
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * </p>
     * 
     * @param operation A original user operation.
     * @param recoveries A list of recovery operations.
     * @see #retryWhen(Class)
     * @see #retryWhen(Class, int)
     * @see #recoverWhen(Class, UnaryOperator)
     * @see #recoverWhen(Class, int, UnaryOperator)
     */
    public static void run(UsefulRunnable operation, UsefulTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        run(operation, operation, o -> {
            o.run();
            return null;
        }, recoveries, new int[recoveries.length]);
    }

    /**
     * <p>
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * </p>
     * 
     * @param operation A original user operation.
     * @param recoveries A list of recovery operations.
     * @return A operation result.
     * @see #retryWhen(Class)
     * @see #retryWhen(Class, int)
     * @see #recoverWhen(Class, UnaryOperator)
     * @see #recoverWhen(Class, int, UnaryOperator)
     */
    public static <R> R run(UsefulSupplier<R> operation, UsefulTriFunction<Supplier<R>, Throwable, Integer, Supplier<R>>... recoveries) {
        return run(operation, operation, Supplier<R>::get, recoveries, new int[recoveries.length]);
    }

    /**
     * <p>
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * </p>
     * 
     * @param original A original user operation.
     * @param recover A current (original or recovery) operation.
     * @param invoker A operation invoker.
     * @param recoveries A list of recovery operations.
     * @param counts A current number of trials.
     * @return A operation result.
     */
    private static <O, R> R run(O original, O recover, Function<O, R> invoker, UsefulTriFunction<O, Throwable, Integer, O>[] recoveries, int[] counts) {
        try {
            return invoker.apply(recover);
        } catch (Throwable e) {
            for (int i = 0; i < recoveries.length; i++) {
                O next = recoveries[i].apply(original, e, counts[i]);

                if (next != null) {
                    counts[i]++;
                    return run(original, next, invoker, recoveries, counts);
                }
            }
            throw e;
        }
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread}.
     * </p>
     *
     * @param task A task to execute.
     */
    public static Future<?> schedule(Runnable task) {
        return parallel.submit(task);
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread} with the specified delay.
     * </p>
     *
     * @param delay A initial delay time.
     * @param unit A delay time unit.
     * @param parallelExecution The <code>true</code> will execute task in parallel,
     *            <code>false</code> will execute task in serial.
     * @param task A task to execute.
     */
    public static Future<?> schedule(long delay, TimeUnit unit, boolean parallelExecution, Runnable task) {
        return (parallelExecution ? parallel : serial).schedule(task, delay, unit);

    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread} with the specified delay.
     * </p>
     *
     * @param delay A initial delay time.
     * @param unit A delay time unit.
     * @param parallelExecution The <code>true</code> will execute task in parallel,
     *            <code>false</code> will execute task in serial.
     * @param task A task to execute.
     */
    public static Future<?> schedule(Runnable task, long interval, TimeUnit unit) {
        return parallel.scheduleWithFixedDelay(task, 0, interval, unit);
    }

    /**
     * <p>
     * Create {@link HashSet} with the specified items.
     * </p>
     * 
     * @param items A list of itmes.
     * @return The new created {@link HashSet}.
     */
    public static <V> Set<V> set(V... items) {
        return collect(HashSet.class, items);
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    @SafeVarargs
    public static <V> Signal<V> signal(V... values) {
        return Signal.EMPTY.startWith(values);
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Iterable<V> values) {
        return Signal.EMPTY.startWith(values);
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Enumeration<V> values) {
        return Signal.EMPTY.startWith(values);
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V, S extends BaseStream<V, S>> Signal<V> signal(S values) {
        return Signal.EMPTY.startWith(values);
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Variable<V> value) {
        return Signal.EMPTY.startWith(value);
    }

    /**
     * @param value A initial value.
     * @param time A time to interval.
     * @param unit A time unit.
     * @param <V> Value type.
     * @return An {@link Signal} that emits values as a first sequence.
     */
    public static <V> Signal<V> signalInfinite(V value, long time, TimeUnit unit) {
        return new Signal<>((observer, disposer) -> {
            Future schedule = schedule(() -> observer.accept(value), time, unit);

            return disposer.add(() -> schedule.cancel(true));
        });
    }

    /**
     * <p>
     * Traverse the tree structure.
     * </p>
     * 
     * @param root A root node to traverse.
     * @param traverser A function to navigate from a node to its children.
     * @return
     */
    public static <T> Signal<T> signal(T root, UnaryOperator<Signal<T>> traverser) {
        return walk(signal(root), traverser);
    }

    /**
     * <p>
     * Traverse the tree structure.
     * </p>
     * 
     * @param root A root node to traverse.
     * @param traverser A function to navigate from a node to its children.
     * @return
     */
    private static <T> Signal<T> walk(Signal<T> root, UnaryOperator<Signal<T>> traverser) {
        return root.merge(root.flatMap(e -> walk(traverser.apply(I.signal(e)), traverser)));
    }

    /**
     * <p>
     * Transform any type object into the specified type if possible.
     * </p>
     *
     * @param <In> A input type you want to transform from.
     * @param <Out> An output type you want to transform into.
     * @param input A target object.
     * @param output A target type.
     * @return A transformed object.
     * @throws NullPointerException If the output type is <code>null</code>.
     */
    public static <In, Out> Out transform(In input, Class<Out> output) {
        if (input == null) {
            return null;
        }

        String encoded = input instanceof String ? (String) input : find(Encoder.class, input.getClass()).encode(input);

        if (output == String.class) {
            return (Out) encoded;
        }

        return ((Decoder<Out>) find(Decoder.class, output)).decode(encoded);
    }

    /**
     * <p>
     * Find the class by the specified fully qualified class name.
     * </p>
     *
     * @param fqcn A fully qualified class name to want.
     * @return The specified class.
     */
    public static Class type(String fqcn) {
        if (fqcn.indexOf('.') == -1) {
            for (Class clazz : primitives) {
                if (clazz.getName().equals(fqcn)) {
                    return clazz;
                }
            }
        }

        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Write {@link Level#WARNING} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void warn(String message) {
        $logger.logp(Level.WARNING, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#WARNING} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void warn(String message, Object... params) {
        $logger.logp(Level.WARNING, "", "", message, params);
    }

    /**
     * <p>
     * Return a non-primitive {@link Class} of the specified {@link Class} object. <code>null</code>
     * will be return <code>null</code>.
     * </p>
     *
     * @param type A {@link Class} object to convert to non-primitive class.
     * @return A non-primitive {@link Class} object.
     */
    public static Class wrap(Class type) {
        // check primitive classes
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i] == type) {
                return wrappers[i];
            }
        }

        // the specified class is not primitive
        return type;
    }

    /**
     * <p>
     * Writes Java object tree to the given output as XML or JSON.
     * </p>
     * <p>
     * If the output object implements {@link AutoCloseable}, {@link AutoCloseable#close()} method
     * will be invoked certainly.
     * </p>
     *
     * @param input A Java object. All properties will be serialized deeply. <code>null</code> will
     *            throw {@link java.lang.NullPointerException}.
     * @param out A serialized data output. <code>null</code> will throw
     *            {@link NullPointerException}.
     * @param format <code>true</code> will produce JSON expression, <code>false</code> will produce
     *            XML expression.
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
     */
    public static void write(Object input, Appendable out) {
        Objects.nonNull(out);

        try {
            // aquire lock
            lock.writeLock().lock();

            // traverse object as json
            Model model = Model.of(input);
            Format format = new Format();
            format.out = out;
            format.accept(pair(model, new Property(model, ""), input));
        } finally {
            // relese lock
            lock.writeLock().unlock();

            // close carefuly
            quiet(out);
        }
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(File source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(InputStream source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(Readable source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(URL source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(URI source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(Node source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(CharSequence source) {
        return I.xml(null, source);
    }

    /** XML literal pattern. */
    private static final Pattern XMLiteral = Pattern.compile("^\\s*<.+>\\s*$", Pattern.DOTALL);

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     * <ul>
     * <li>{@link XML}</li>
     * <li>{@link File}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link Readable}</li>
     * <li>{@link URL}</li>
     * <li>{@link URI}</li>
     * <li>{@link Node}</li>
     * <li>{@link CharSequence}</li>
     * </ul>
     * <ul>
     * <li>XML Literal</li>
     * <li>HTML Literal</li>
     * </ul>
     *
     * @param xml A xml expression.
     * @return A constructed {@link XML}.
     */
    static XML xml(Document doc, Object xml) {
        try {
            // XML related types
            if (xml instanceof XML) {
                return (XML) xml;
            } else if (xml instanceof Node) {
                return new XML(((Node) xml).getOwnerDocument(), list(xml));
            }

            // byte data types
            byte[] bytes = read(xml);

            if (6 < bytes.length && bytes[0] == '<') {
                // doctype declaration (starts with <! )
                // root element is html (starts with <html> )
                if (bytes[1] == '!' || (bytes[1] == 'h' && bytes[2] == 't' && bytes[3] == 'm' && bytes[4] == 'l' && bytes[5] == '>')) {
                    return new XML(null, null).parse(bytes, $encoding);
                }
            }

            String value = new String(bytes, $encoding);

            if (XMLiteral.matcher(value).matches()) {
                doc = dom.parse(new InputSource(new StringReader("<m>".concat(value.replaceAll("<\\?.+\\?>", "")).concat("</m>"))));
                return new XML(doc, XML.convert(doc.getFirstChild().getChildNodes()));
            } else {
                return xml(doc != null ? doc.createTextNode(value) : dom.newDocument().createElement(value));
            }
        } catch (Exception e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Read byte data from various sources.
     * </p>
     * 
     * @param input A data source.
     * @return A data.
     */
    private static byte[] read(Object input) throws Exception {
        // skip character data
        if (input instanceof CharSequence == false) {
            // object to stream
            if (input instanceof URI) {
                input = ((URI) input).toURL();
            }
            if (input instanceof URL) {
                input = ((URL) input).openStream();
            } else if (input instanceof File) {
                input = new FileInputStream((File) input);
            }

            // stream to byte
            if (input instanceof InputStream) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copy((InputStream) input, out, true);
                input = out.toByteArray();
            } else if (input instanceof Readable) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copy((Readable) input, new OutputStreamWriter(out, $encoding), true);
                input = out.toByteArray();
            }
        }
        return input instanceof byte[] ? (byte[]) input : input.toString().getBytes();
    }
}
