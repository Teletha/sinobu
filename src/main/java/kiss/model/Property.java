/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import static java.lang.reflect.Modifier.*;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.WritableValue;

import kiss.Accessor;
import kiss.I;

/**
 * <p>
 * This class represents a property of object model.
 * </p>
 * <p>
 * Note: Care should be exercised if {@link Property} objects are used as keys in a
 * {@link java.util.SortedMap} or elements in a {@link java.util.SortedSet} since Property's natural
 * ordering is inconsistent with equals. See {@link Comparable}, {@link java.util.SortedMap} or
 * {@link java.util.SortedSet} for more information.
 * </p>
 * 
 * @version 2009/07/17 15:03:16
 */
@SuppressWarnings("unchecked")
public class Property<M, V> implements Comparable<Property>, Accessor<M, V> {

    /** The assosiated object model with this {@link Property}. */
    public final Model<V> model;

    /** The human readable identifier of this {@link Property}. */
    public final String name;

    /** The flag whether this {@link Property} is transient or not. */
    public final boolean isTransient;

    /** The annotated element. */
    private final Map annotations = new HashMap(2);

    /** The actual accessor methods. */
    MethodHandle[] accessors;

    /** The {@link Property} type. */
    int type;

    /**
     * Create a property.
     * 
     * @param T The type which can treat as {@link Property}. (i.e. {@link Field} or {@link Method})
     * @param model A model that this property belongs to.
     * @param name A property name.
     */
    public <T extends AccessibleObject & Member> Property(Model model, String name, T... elements) {
        this.model = model;
        this.name = name;

        boolean serializable = false;

        for (T element : elements) {
            if ((element.getModifiers() & TRANSIENT) != 0 || element.isAnnotationPresent(Transient.class)) {
                serializable = true;
            }

            for (Annotation annotation : element.getAnnotations()) {
                annotations.put(annotation.annotationType(), annotation);
            }
        }
        this.isTransient = serializable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Property o) {
        // compare type
        if (isAttribute() != o.isAttribute()) {
            return isAttribute() ? -1 : 1;
        }

        // compare name
        return name.compareTo(o.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(M model) {
        if (model == null) {
            return null;
        }

        try {
            if (type == 2) {
                // property access
                return ((WritableValue<V>) accessors[0].invoke(model)).getValue();
            } else {
                // field or method access
                return (V) accessors[0].invoke(model);
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M set(M model, V property) {
        if (model != null) {
            try {
                if (type == 2) {
                    // property access
                    ((WritableValue<V>) accessors[0].invoke(model)).setValue(property);
                } else {
                    // field or method access
                    Class type = this.model.type;

                    if ((!type.isPrimitive() && !type.isEnum()) || property != null) {
                        accessors[1].invoke(model, property);
                    }
                }
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        }
        return model;
    }

    /**
     * Decide whether this object model can be Attribute or not.
     * 
     * @return A result.
     */
    public boolean isAttribute() {
        return model.decoder() != null || model.type.isArray();
    }

    /**
     * <p>
     * Returns this property's annotation for the specified type if such an annotation is present,
     * else null.
     * </p>
     * 
     * @param annotationClass A Class object corresponding to the annotation type
     * @return This property's annotation for the specified annotation type if present on this
     *         element, else null
     * @throws NullPointerException if the given annotation class is null
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return (A) annotations.get(annotationClass);
    }
}
