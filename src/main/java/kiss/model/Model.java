/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import static java.lang.reflect.Modifier.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;

import kiss.Decoder;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiConsumer;
import kiss.WiseFunction;
import kiss.WiseTriConsumer;

/**
 * {@link Model} is the advanced representation of {@link Class} in Sinobu.
 */
public class Model<M> {

    /** Reusable variable arguments. */
    private static final Object[] NoARG = new Object[0];

    /** The model repository. */
    static final Map<Class, Model> models = new ConcurrentHashMap();

    /** The {@link Class} which is represented by this {@link Model}. */
    public final Class<M> type;

    /** Whether this {@link Model} is an atomic type or a object type. */
    public final boolean atomic;

    /** The unmodifiable properties list of this object model. */
    Map<String, Property> properties;

    /**
     * Create Model instance.
     * 
     * @param type A target class to analyze as model.
     * @throws NullPointerException If the specified model class is <code>null</code>.
     */
    Model(Class type) {
        // Skip null check because this method can throw NullPointerException.
        // if (type == null) throw new NullPointerException("Model class shouldn't be null.");
        this.type = type;
        this.atomic = I.find(Decoder.class, type) != null || type.isArray();
    }

    /**
     * Initialize this {@link Model} only once.
     */
    private synchronized void init() {
        if (properties == null) {
            properties = Collections.EMPTY_MAP;
            try {
                // examine all methods without private, final, static or native
                Map<String, Method[]> candidates = new HashMap();

                for (Class clazz : Model.collectTypes(type)) {
                    if (!Proxy.isProxyClass(clazz)) {
                        for (Method method : clazz.getDeclaredMethods()) {
                            // exclude the method which modifier is final, static, private or native
                            if (((STATIC | NATIVE) & method.getModifiers()) == 0) {
                                // exclude the method which is created by compiler
                                if (!method.isBridge() && !method.isSynthetic()) {
                                    // if (method.getAnnotations().length != 0) {
                                    // intercepts.add(method);
                                    // }

                                    int length = 1;
                                    String prefix = "set";
                                    String name = method.getName();

                                    if (method.getGenericReturnType() != Void.TYPE) {
                                        length = 0;
                                        prefix = name.charAt(0) == 'i' ? "is" : "get";
                                    }

                                    // exclude the method (by name)
                                    if (prefix.length() < name.length() && name.startsWith(prefix) && !Character
                                            .isLowerCase(name.charAt(prefix.length()))) {
                                        // exclude the method (by parameter signature)
                                        if (method.getGenericParameterTypes().length == length) {
                                            // compute property name
                                            name = name.substring(prefix.length());
                                            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

                                            // store a candidate of property accessor
                                            Method[] methods = candidates.get(name);

                                            if (methods == null) {
                                                methods = new Method[2];
                                                candidates.put(name, methods);
                                            }

                                            if (methods[length] == null) {
                                                methods[length] = method;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // build valid properties
                // don't use type parameter to reduce footprint
                properties = new TreeMap();

                for (Entry<String, Method[]> entry : candidates.entrySet()) {
                    Method[] methods = entry.getValue();
                    if (methods[0] != null && methods[1] != null) {
                        // create model for the property
                        try {
                            Model model = of(methods[0].getGenericReturnType(), type);

                            if (of(methods[1].getGenericParameterTypes()[0], type).type.isAssignableFrom(model.type)) {
                                methods[0].setAccessible(true);
                                methods[1].setAccessible(true);

                                // this property is valid
                                WiseBiConsumer setter = createSetter(methods[1]);
                                Property property = new Property(model, entry.getKey(), null);
                                property.getter = createGetter(methods[0]);
                                property.setter = (m, v) -> {
                                    setter.ACCEPT(m, v);
                                    // methods[1].invoke(m, v);
                                    return m;
                                };

                                // register it
                                properties.put(property.name, property);
                            }
                        } catch (Throwable e) {
                            throw I.quiet(e);
                        }
                    }
                }

                // We are not using Class#isRecord to support java11.
                boolean isRecord = type.getSuperclass() != null && type.getSuperclass().getName().equals("java.lang.Record");

                // Search field properties.
                Class clazz = type;
                while (clazz != null) {
                    for (Field field : clazz.getDeclaredFields()) {
                        int modifier = field.getModifiers();
                        boolean notFinal = (FINAL & modifier) == 0;

                        // reject the field which modifier is static or native
                        if (((STATIC | NATIVE) & modifier) == 0) {
                            // accept fields which
                            // -- is public modifier (implicitely)
                            // -- is annotated by Managed (explicitely)
                            // -- is Record component (implicitely)
                            if ((PUBLIC & modifier) == PUBLIC //
                                    || field.isAnnotationPresent(Managed.class) //
                                    || (isRecord && (PRIVATE & modifier) == PRIVATE)) {
                                field.setAccessible(true);
                                Model fieldModel = of(field.getGenericType(), type);

                                if (Variable.class.isAssignableFrom(fieldModel.type)) {
                                    // variable
                                    Property property = new Property(of(collectParameters(field
                                            .getGenericType(), Variable.class, type)[0], type), field.getName(), field);
                                    property.getter = m -> ((Variable) field.get(m)).v;
                                    property.setter = (m, v) -> {
                                        ((Variable) field.get(m)).set(v);
                                        return m;
                                    };
                                    property.observer = m -> ((Variable) field.get(m)).observe();

                                    // register it
                                    properties.put(property.name, property);
                                } else if ((fieldModel.atomic && notFinal) || !fieldModel.atomic || isRecord) {
                                    // field
                                    field.setAccessible(true);

                                    Property property = new Property(fieldModel, field.getName(), field);
                                    property.getter = m -> field.get(m);
                                    property.setter = !isRecord ? (m, v) -> {
                                        if (notFinal) field.set(m, v);
                                        return m;
                                    } : (m, v) -> {
                                        Constructor c = collectConstructors(type)[0];
                                        Parameter[] params = c.getParameters();
                                        Object[] values = new Object[params.length];
                                        for (int i = 0; i < params.length; i++) {
                                            String name = params[i].getName();
                                            values[i] = name.equals(property.name) ? v : get((M) m, property(name));
                                        }
                                        return c.newInstance(values);
                                    };

                                    // register it
                                    properties.put(property.name, property);
                                }
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * List up all properties.
     * 
     * @return
     */
    public Collection<Property> properties() {
        return properties.values();
    }

    /**
     * Find the property which has the specified name in this object model. If the suitable property
     * is not found, <code>null</code> is returned.
     * 
     * @param name A name of property.
     * @return A suitable property or <code>null</code>.
     */
    public Property property(String name) {
        return properties.get(name);
    }

    /**
     * Returns the value of the given property in the given object.
     * 
     * @param object A object as source. This value must not be <code>null</code>.
     * @param property A property. This value must not be <code>null</code>.
     * @return A resolved property value. This value may be <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public Object get(M object, Property property) {
        if (object == null || property == null) {
            return null;
        }
        return property.getter.apply(object);
    }

    /**
     * Change the given property in the given object to the given new property value.
     * 
     * @param object A object as source. This value must not be <code>null</code>.
     * @param property A property. This value must not be <code>null</code>.
     * @param value A new property value that you want to set. This value accepts <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public M set(M object, Property property, Object value) {
        if (object != null && property != null) {
            // field or method access
            Class type = property.model.type;

            if ((!type.isPrimitive() && !type.isEnum()) || value != null) {
                return (M) property.setter.apply(object, value);
            }
        }
        return object;
    }

    /**
     * Observe the given property in the given object.
     * 
     * @param object A object as source. This value must not be <code>null</code>.
     * @param property A property. This value must not be <code>null</code>.
     * @return A property observer.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public Signal observe(M object, Property property) {
        if (object != null && property != null && property.observer != null) {
            return property.observer.apply(object);
        } else {
            return Signal.never();
        }
    }

    /**
     * Iterate over all properties in the given object and propagate the property and it's value to
     * the given property walker.
     * 
     * @param object A object as source. This value must not be <code>null</code>,
     * @param walker A property iterator. This value accepts <code>null</code>.
     */
    public void walk(M object, WiseTriConsumer<Model<M>, Property, Object> walker) {
        // check whether this model is attribute or not.
        if (walker != null) {
            for (Property property : properties.values()) {
                walker.accept(this, property, get(object, property));
            }
        }
    }

    /**
     * Utility method to retrieve the cached model. If the model of the given class is not found,
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * If the given model has no cached information, it will be created automatically. This
     * operation is thread-safe.
     * <p>
     * Note : All classes do not necessary have each information. Some classes might share same
     * {@link Model} object. (e.g. AutoGenerated Class)
     * 
     * @param modelType A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static <M> Model<M> of(M modelType) {
        return of((Class<M>) modelType.getClass());
    }

    /**
     * Utility method to retrieve the cached model. If the model of the given class is not found,
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * If the given model has no cached information, it will be created automatically. This
     * operation is thread-safe.
     * <p>
     * Note : All classes do not necessary have each information. Some classes might share same
     * {@link Model} object. (e.g. AutoGenerated Class)
     * 
     * @param modelClass A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static <M> Model<M> of(Class<? super M> modelClass) {
        // check cache
        Model model = models.get(modelClass);
        if (model == null) {
            if (List.class.isAssignableFrom(modelClass)) {
                model = new ListModel(modelClass, Model.collectParameters(modelClass, List.class), List.class);
            } else if (Map.class.isAssignableFrom(modelClass)) {
                model = new MapModel(modelClass, Model.collectParameters(modelClass, Map.class), Map.class);
            } else {
                // To resolve cyclic reference, try to retrive from cache.
                model = models.computeIfAbsent(modelClass, Model::new);
                model.init();
            }
            models.put(modelClass, model);
        }
        return model;
    }

    /**
     * Utility method to retrieve the cached model. If the model of the given type is not found,
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param type A target type to analyze.
     * @param base A declaration class.
     * @return A cached model information.
     * @throws IllegalArgumentException If the given model type is null.
     * @see TypeVariable
     */
    static Model of(Type type, Type base) {
        // class
        if (type instanceof Class) {
            return of((Class) type);
        }

        // parameterized type
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            Class clazz = (Class) parameterized.getRawType();

            // ListModel
            if (List.class.isAssignableFrom(clazz)) {
                return new ListModel(clazz, parameterized.getActualTypeArguments(), base);
            }

            // MapModel
            if (Map.class.isAssignableFrom(clazz)) {
                return new MapModel(clazz, parameterized.getActualTypeArguments(), base);
            }

            // ClassModel
            return of(clazz);
        }

        // wildcard type
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;

            Type[] types = wildcard.getLowerBounds();

            if (types.length != 0) {
                return of(types[0], base);
            }

            types = wildcard.getUpperBounds();

            if (types.length != 0) {
                return of(types[0], base);
            }
        }

        // variable type
        if (type instanceof TypeVariable) {
            TypeVariable variable = (TypeVariable) type;
            TypeVariable[] variables = variable.getGenericDeclaration().getTypeParameters();

            for (int i = 0; i < variables.length; i++) {
                // use equals method instead of "==".
                //
                // +++ From TypeVariable Javadoc +++
                // Multiple objects may be instantiated at run-time to represent a given type
                // variable. Even though a type variable is created only once, this does not imply
                // any requirement to cache instances representing the type variable. However, all
                // instances representing a type variable must be equal() to each other. As a
                // consequence, users of type variables must not rely on the identity of instances
                // of classes implementing this interface.
                if (variable.equals(variables[i])) {
                    if (base == variable.getGenericDeclaration()) {
                        return of(variable.getBounds()[0], base);
                    } else {
                        return of(collectParameters(base, variable.getGenericDeclaration())[i], base);
                    }
                }
            }
        }

        // generic array type
        if (type instanceof GenericArrayType) {
            return of(((GenericArrayType) type).getGenericComponentType(), base);
        }

        // If this error will be thrown, it is bug of this program. Please send a bug report to us.
        throw new Error();
    }

    /**
     * Collect all annotated methods and thire annotations.
     * 
     * @param clazz A target class.
     * @return A table of method and annnotations.
     */
    public static Map<Method, List<Annotation>> collectAnnotatedMethods(Class clazz) {
        Map<Method, List<Annotation>> table = new HashMap();

        for (Class type : collectTypes(clazz)) {
            if (type != Object.class) {
                for (Method method : type.getDeclaredMethods()) {
                    // exclude the method which is created by compiler
                    // exclude the private method which is not declared in the specified class
                    if (!method.isBridge() && !method
                            .isSynthetic() && (((method.getModifiers() & Modifier.PRIVATE) == 0) || method.getDeclaringClass() == clazz)) {
                        Annotation[] annotations = method.getAnnotations();

                        if (annotations.length != 0) {
                            List<Annotation> list = new ArrayList();

                            // disclose container annotation
                            for (Annotation annotation : annotations) {
                                try {
                                    Class annotationType = annotation.annotationType();
                                    Method value = annotationType.getMethod("value");
                                    Class returnType = value.getReturnType();

                                    if (returnType.isArray()) {
                                        Class<?> componentType = returnType.getComponentType();
                                        Repeatable repeatable = componentType.getAnnotation(Repeatable.class);

                                        if (repeatable != null && repeatable.value() == annotationType) {
                                            value.setAccessible(true);

                                            Collections.addAll(list, (Annotation[]) value.invoke(annotation));
                                            continue;
                                        }
                                    }
                                } catch (Exception e) {
                                    // do nothing
                                }
                                list.add(annotation);
                            }

                            // check method overriding
                            for (Method candidate : table.keySet()) {
                                if (candidate.getName().equals(method.getName()) && Arrays
                                        .deepEquals(candidate.getParameterTypes(), method.getParameterTypes())) {
                                    method = candidate; // detect overriding
                                    break;
                                }
                            }

                            add: for (Annotation annotation : list) {
                                Class annotationType = annotation.annotationType();
                                List<Annotation> items = table.computeIfAbsent(method, m -> new ArrayList());

                                if (!annotationType.isAnnotationPresent(Repeatable.class)) {
                                    for (Annotation item : items) {
                                        if (item.annotationType() == annotationType) {
                                            continue add;
                                        }
                                    }
                                }
                                items.add(annotation);
                            }
                        }
                    }
                }
            }
        }
        return table;
    }

    /**
     * Collect all constructors which are defined in the specified {@link Class}. If the given class
     * is interface, primitive types, array class or <code>void</code>, <code>empty array</code>
     * will be return.
     * 
     * @param <T> A class type.
     * @param clazz A target class.
     * @return A collected constructors.
     */
    public static <T> Constructor<T>[] collectConstructors(Class<T> clazz) {
        Constructor[] cc = clazz.getDeclaredConstructors();
        Arrays.sort(cc, Comparator.<Constructor> comparingInt(c -> {
            for (Annotation a : c.getAnnotations()) {
                if (a.annotationType() == Managed.class || a.annotationType().getSimpleName().equals("Inject")) {
                    return -1;
                }
            }

            // Constructor#getParameters is not supported in lower version than Android O.
            // So we must use Class#getParameterType#length instead.
            return c.getParameterTypes().length;
        }));
        return cc;
    }

    // public static Signal<Class> findTypes(Class... clazz) {
    // return Signal.from(clazz)
    // .skipNull()
    // .flatMap(c -> Signal.from(c).concat(() -> findTypes(c.getSuperclass())).concat(() ->
    // findTypes(c.getInterfaces())))
    // .distinct();
    // }

    /**
     * Collect all classes which are extended or implemented by the target class.
     * 
     * @param clazz A target class. <code>null</code> will be return the empty set.
     * @return A set of classes, with predictable bottom-up iteration order.
     */
    public static Set<Class> collectTypes(Class clazz) {
        // check null
        if (clazz == null) {
            return Collections.EMPTY_SET;
        }

        // container
        Set<Class> set = new LinkedHashSet(); // order is important

        // add current class
        set.add(clazz);

        // add super class
        set.addAll(collectTypes(clazz.getSuperclass()));

        // add interface classes
        for (Class c : clazz.getInterfaces()) {
            set.addAll(collectTypes(c));
        }

        // API definition
        return set;
    }

    /**
     * List up all target types which are implemented or extended by the specified class.
     * 
     * @param type A class type which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types. <code>null</code> will be return the
     *            zero-length array.
     * @return A list of actual types.
     */
    public static Type[] collectParameters(Type type, GenericDeclaration target) {
        return collectParameters(type, target, type);
    }

    /**
     * List up all target types which are implemented or extended by the specified class.
     * 
     * @param clazz A class type which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types. <code>null</code> will be return the
     *            zero-length array.
     * @param base A base class type.
     * @return A list of actual types.
     */
    private static Type[] collectParameters(Type clazz, GenericDeclaration target, Type base) {
        // check null
        if (clazz == null || clazz == target) {
            return new Class[0];
        }

        // compute actual class
        Class raw = clazz instanceof Class ? (Class) clazz : Model.of(clazz, base).type;

        // collect all types
        Set<Type> types = new HashSet();
        types.add(clazz);
        types.add(raw.getGenericSuperclass());
        Collections.addAll(types, raw.getGenericInterfaces());

        // check them all
        for (Type type : types) {
            // check ParameterizedType
            if (type instanceof ParameterizedType) {
                ParameterizedType param = (ParameterizedType) type;

                // check raw type
                if (target == param.getRawType()) {
                    Type[] args = param.getActualTypeArguments();
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof TypeVariable) {
                            try {
                                args[i] = Model.of(args[i], base).type;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                args[i] = collectParameters(clazz, target)[i];
                            }
                        }
                    }
                    return args;
                }
            }
        }

        // search from superclass
        Type[] parameters = collectParameters(raw.getGenericSuperclass(), target, base);

        if (parameters.length != 0) {
            return parameters;
        }

        // search from interfaces
        for (Type type : raw.getInterfaces()) {
            parameters = collectParameters(type, target, base);

            if (parameters.length != 0) {
                return parameters;
            }
        }
        return parameters;
    }

    static WiseFunction createGetter(Method method) throws Throwable {
        Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
        MethodHandle mh = lookup.unreflect(method);

        return (WiseFunction) LambdaMetafactory
                .metafactory(lookup, "APPLY", MethodType.methodType(WiseFunction.class), mh.type().generic(), mh, mh.type())
                .dynamicInvoker()
                .invokeExact();
    }

    static WiseBiConsumer createSetter(Method method) throws Throwable {
        Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
        MethodHandle mh = lookup.unreflect(method);
        Class<?> param = method.getParameterTypes()[0];
        MethodType type = MethodType.methodType(void.class, Object.class, param.isPrimitive() ? param : Object.class);

        if (param == int.class) {
            ObjIntConsumer con = (ObjIntConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjIntConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (int) b);
        } else if (param == long.class) {
            ObjLongConsumer con = (ObjLongConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjLongConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (long) b);
        } else if (param == double.class) {
            ObjDoubleConsumer con = (ObjDoubleConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjDoubleConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (double) b);
        } else if (param == float.class) {
            ObjFloatConsumer con = (ObjFloatConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjFloatConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (float) b);
        } else if (param == boolean.class) {
            WiseBiConsumer con = (WiseBiConsumer) LambdaMetafactory
                    .metafactory(lookup, "ACCEPT", MethodType.methodType(WiseBiConsumer.class), MethodType
                            .methodType(void.class, Object.class, Object.class), mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (boolean) b);
        } else if (param == byte.class) {
            ObjByteConsumer con = (ObjByteConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjByteConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (byte) b);
        } else if (param == short.class) {
            ObjShortConsumer con = (ObjShortConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjShortConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (short) b);
        } else if (param == char.class) {
            ObjCharConsumer con = (ObjCharConsumer) LambdaMetafactory
                    .metafactory(lookup, "accept", MethodType.methodType(ObjCharConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
            return (a, b) -> con.accept(a, (char) b);
        } else {
            return (WiseBiConsumer) LambdaMetafactory
                    .metafactory(lookup, "ACCEPT", MethodType.methodType(WiseBiConsumer.class), type, mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
        }
    }

    public interface ObjBooleanConsumer {
        void accept(Object o, boolean value);
    }

    public interface ObjByteConsumer {
        void accept(Object o, byte value);
    }

    public interface ObjShortConsumer {
        void accept(Object o, short value);
    }

    public interface ObjFloatConsumer {
        void accept(Object o, float value);
    }

    public interface ObjCharConsumer {
        void accept(Object o, char value);
    }
}