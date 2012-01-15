/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import hub.Agent.Translator;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

import org.junit.Rule;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @version 2012/01/10 9:52:42
 */
public class PowerAssert extends ReusableRule {

    @Rule
    private final Agent agent = new Agent(PowerAssertTranslator.class);

    /** The caller class. */
    private final Class caller;

    /** The tester flag. */
    private final boolean selfTest;

    /** The expected operands. */
    private final List<Operand> expecteds = new ArrayList();

    /**
     * Assertion Utility.
     */
    public PowerAssert() {
        this.caller = UnsafeUtility.getCaller(1);
        this.selfTest = false;

        // force to transform
        agent.transform(caller);
    }

    /**
     * Test for {@link PowerAssert}.
     */
    PowerAssert(boolean selfTest) {
        this.caller = UnsafeUtility.getCaller(1);
        this.selfTest = selfTest;

        // force to transform
        agent.transform(caller);
    }

    /**
     * @param name
     * @param value
     */
    void willCapture(String name, Object value) {
        expecteds.add(new Operand(name, value));
    }

    /**
     * @see hub.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        expecteds.clear();
    }

    /**
     * @see hub.ReusableRule#validateError(java.lang.Throwable)
     */
    @Override
    protected Throwable validateError(Throwable throwable) {
        if (selfTest && throwable instanceof AssertionError) {
            PowerAssertionContext context = PowerAssertionContext.get();

            for (Operand expected : expecteds) {
                if (!context.operands.contains(expected)) {
                    return new AssertionError("Can't capture the below operand.\r\nCode  : " + expected.name + "\r\nValue : " + expected.value + "\r\n\r\n" + context);
                }
            }
            return null;
        } else {
            return throwable;
        }
    }

    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    /**
     * @version 2012/01/14 22:48:47
     */
    private final class PowerAssertTranslator extends Translator {

        /** The state. */
        private boolean startAssertion = false;

        /** The state. */
        private boolean skipNextJump = false;

        /** The state. */
        private boolean processAssertion = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitCode() {
            super.visitCode();

            // reset
            startAssertion = false;
            skipNextJump = false;
            processAssertion = false;
        }

        /**
         * <p>
         * Helper method to write bytecode which wrap the primitive value which is last on operand
         * stack to its wrapper value.
         * </p>
         * 
         * @param type
         */
        private void wrap(Type type) {
            Type wrapper = getWrapperType(type);

            if (wrapper != type) {
                super.visitMethodInsn(INVOKESTATIC, wrapper.getInternalName(), "valueOf", Type.getMethodDescriptor(wrapper, type));
            }
        }

        /**
         * <p>
         * Search wrapper type of the specified primitive type.
         * </p>
         * 
         * @param type
         * @return
         */
        private Type getWrapperType(Type type) {
            switch (type.getSort()) {
            case Type.BOOLEAN:
                return Type.getType(Boolean.class);

            case Type.INT:
                return Type.getType(Integer.class);

            case Type.LONG:
                return Type.getType(Long.class);

            case Type.FLOAT:
                return Type.getType(Float.class);

            case Type.DOUBLE:
                return Type.getType(Double.class);

            case Type.CHAR:
                return Type.getType(Character.class);

            case Type.BYTE:
                return Type.getType(Byte.class);

            case Type.SHORT:
                return Type.getType(Short.class);

            default:
                return type;
            }
        }

        private void context() {
            super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (!startAssertion && opcode == GETSTATIC && name.equals("$assertionsDisabled")) {
                startAssertion = true;
                skipNextJump = true;

                super.visitFieldInsn(opcode, owner, name, desc);
            } else {
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (skipNextJump) {
                skipNextJump = false;
                processAssertion = true;

                super.visitJumpInsn(opcode, label);
            } else if (!processAssertion) {
                super.visitJumpInsn(opcode, label);
            } else {
                super.visitJumpInsn(opcode, label);

                switch (opcode) {
                case IFEQ:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;");
                    super.visitLdcInsn("==");
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeExpression", "(Ljava/lang/String;)V");
                    break;

                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;");
                    super.visitLdcInsn("!=");
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeExpression", "(Ljava/lang/String;)V");
                    break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (processAssertion && opcode == NEW && type.equals("java/lang/AssertionError")) {
                processAssertion = false;

                super.visitTypeInsn(opcode, type);
            } else {
                super.visitTypeInsn(opcode, type);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (opcode == INVOKESPECIAL && owner.equals("java/lang/AssertionError")) {
                super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;");
                super.visitMethodInsn(opcode, owner, name, "(Ljava/lang/Object;)V");
            } else if (!processAssertion) {
                super.visitMethodInsn(opcode, owner, name, desc);
            } else {
                Type methodType = Type.getType(desc);
                Type returnType = methodType.getReturnType();

                super.visitMethodInsn(opcode, owner, name, desc);
                super.visitInsn(DUP);
                super.visitVarInsn(returnType.getOpcode(ISTORE), 0);

                context();
                super.visitLdcInsn(name);
                super.visitIntInsn(BIPUSH, methodType.getArgumentTypes().length);
                super.visitVarInsn(returnType.getOpcode(ILOAD), 0);
                wrap(returnType);
                super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeMethod", "(Ljava/lang/String;ILjava/lang/Object;)V");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitIntInsn(int opcode, int operand) {
            super.visitIntInsn(opcode, operand);

            if (processAssertion) {
                context();
                super.visitIntInsn(opcode, operand);
                wrap(INT_TYPE);
                super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeConstant", "(Ljava/lang/Object;)V");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitInsn(int opcode) {
            super.visitInsn(opcode);

            if (processAssertion) {
                switch (opcode) {
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    context();
                    super.visitInsn(opcode);
                    wrap(INT_TYPE);
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeConstant", "(Ljava/lang/Object;)V");
                    break;

                case LCONST_0:
                case LCONST_1:
                    context();
                    super.visitInsn(opcode);
                    wrap(LONG_TYPE);
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeConstant", "(Ljava/lang/Object;)V");
                    break;

                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    context();
                    super.visitInsn(opcode);
                    wrap(FLOAT_TYPE);
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeConstant", "(Ljava/lang/Object;)V");
                    break;

                case DCONST_0:
                case DCONST_1:
                    context();
                    super.visitInsn(opcode);
                    wrap(DOUBLE_TYPE);
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeConstant", "(Ljava/lang/Object;)V");
                    break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitLdcInsn(Object value) {
            super.visitLdcInsn(value);

            if (processAssertion) {
                context();
                super.visitLdcInsn(value);
                wrap(Type.getType(value.getClass()));
                super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeConstant", "(Ljava/lang/Object;)V");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);

            if (processAssertion) {
                Type localVariableType = Type.INT_TYPE;

                switch (opcode) {
                case LLOAD:
                    localVariableType = Type.LONG_TYPE;
                    break;

                case FLOAD:
                    localVariableType = Type.FLOAT_TYPE;
                    break;

                case DLOAD:
                    localVariableType = Type.DOUBLE_TYPE;
                    break;

                case ALOAD:
                    localVariableType = OBJECT_TYPE;
                    break;
                }

                super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;");
                super.visitLdcInsn(PowerAssertionContext.computeLocalVariableId("class", methodName, methodType.getDescriptor(), String.valueOf(var)));
                super.visitVarInsn(opcode, var);
                wrap(localVariableType);
                super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertionContext", "recodeLocalVariable", "(Ljava/lang/String;Ljava/lang/Object;)V");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            super.visitLocalVariable(name, desc, signature, start, end, index);

            String id = PowerAssertionContext.computeLocalVariableId("class", methodName, methodType.getDescriptor(), String.valueOf(index));

            PowerAssertionContext.getLocalVariable(id, name, Type.getType(desc));
        }

    }

    /**
     * @version 2012/01/11 11:27:35
     */
    @Manageable(lifestyle = ThreadSpecific.class)
    public static class PowerAssertionContext implements Constant, Variable, LocalVariable, Expression, MethodCall {

        /** The local variable name mapping. */
        private static final Map<String, Local> locals = new HashMap();

        /** The operand stack. */
        private ArrayDeque<Operand> stack = new ArrayDeque();

        /** The using operand list. */
        private ArrayList<Operand> operands = new ArrayList();

        /** The source code representation. */
        private StringBuilder code = new StringBuilder("\r\n");

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeConstant(Object constant) {
            Operand operand = new Operand(constant);
            stack.add(operand);
            operands.add(operand);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeVariable(Object variable, String expression) {
            Operand operand = new Operand(expression, variable);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeLocalVariable(String id, Object variable) {
            Operand operand;
            Local local = locals.get(id);

            switch (local.type.getSort()) {
            case BOOLEAN:

                operand = new Operand(local.name, (int) variable == 1);
                break;

            default:
                operand = new Operand(local.name, variable);
                break;
            }

            stack.add(operand);
            operands.add(operand);
        }

        /**
         * @see hub.PowerAssert.Expression#recodeExpression(java.lang.String)
         */
        @Override
        public void recodeExpression(String expression) {
            switch (stack.size()) {
            case 0:
                break;

            case 1:
                code.append(stack.pollLast());
                break;

            default:
                code.append(stack.pollLast()).append(' ').append(expression).append(' ').append(stack.pollLast());
                break;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeMethod(String name, int paramsSize, Object value) {
            // build method invocation
            StringBuilder invocation = new StringBuilder("()");

            for (int i = 0; i < paramsSize; i++) {
                invocation.insert(1, stack.pollLast());

                if (i + 1 != paramsSize) {
                    invocation.insert(1, ", ");
                }
            }
            invocation.insert(0, name).insert(0, '.').insert(0, stack.pollLast());

            Operand operand = new Operand(invocation.toString(), value);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(code);
            builder.append("\r\n");

            for (Operand operand : operands) {
                if (!operand.constant) {
                    builder.append("\r\n").append(operand.name).append(" : ").append(operand.value);
                }
            }
            return builder.toString();
        }

        public static PowerAssertionContext get() {
            return I.make(PowerAssertionContext.class);
        }

        /**
         * @param id
         * @param node
         */
        public static Local getLocalVariable(String id, String name, Type type) {
            Local local = locals.get(id);

            if (local == null) {
                local = new Local();

                locals.put(id, local);
            }

            if (local.name == null && name != null) {
                local.name = name;
                local.type = type;
            }
            return local;
        }

        /**
         * <p>
         * Compute identifier for local variable.
         * </p>
         * 
         * @param className
         * @param methodName
         * @param methodDescriptor
         * @param position
         * @return
         */
        public static String computeLocalVariableId(String className, String methodName, String methodDescriptor, String position) {
            return className + methodName + methodDescriptor + position;
        }

        /**
         * @version 2012/01/14 20:50:07
         */
        private static class Local {

            /** The variable type. */
            public Type type;

            /** The variable name. */
            public String name;
        }
    }

    /**
     * @version 2012/01/11 14:11:46
     */
    private static class Operand {

        /** The human redable expression. */
        private String name;

        /** The actual value. */
        private Object value;

        /** The constant flag. */
        private boolean constant;

        /**
         * 
         */
        private Operand(Object value) {
            this.name = value instanceof String ? "\"" + value + "\"" : String.valueOf(value);
            this.value = value;
            this.constant = true;
        }

        /**
         * 
         */
        private Operand(String name, Object value) {
            this.name = name;
            this.value = value;
            this.constant = false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Operand other = (Operand) obj;
            if (name == null) {
                if (other.name != null) return false;
            } else if (!name.equals(other.name)) return false;
            if (value == null) {
                if (other.value != null) return false;
            } else if (!value.equals(other.value)) return false;
            return true;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * <p>
     * Marker interface for type-safe bytecode builder.
     * </p>
     * 
     * @version 2012/01/14 2:08:48
     */
    private static interface Recodable {
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface Constant<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param constant
         */
        void recodeConstant(T constant);
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface LocalVariable<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeLocalVariable(String id, T variable);
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface Variable<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeVariable(T variable, String expression);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface Expression<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeExpression(String expression);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface MethodCall<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeMethod(String name, int paramsSize, T value);
    }

    /**
     * @version 2012/01/14 2:02:54
     */
    private static class RecodableMethod {

        /** The cache for recoder type. */
        private static final Map<Class, RecodableMethod> types = new HashMap();

        /** The method name. */
        private final String name;

        /** The method descriptor. */
        private final String descriptor;

        /** The method owner. */
        private final String owner;

        /** The method type. */
        private final Type type;

        /**
         * 
         */
        private RecodableMethod(Class<? extends Recodable> recoder) {
            Method method = recoder.getMethods()[0];
            this.name = method.getName();
            this.type = Type.getType(method);
            this.descriptor = type.getDescriptor();
            this.owner = Type.getType(method.getDeclaringClass()).getInternalName();
        }

        /**
         * <p>
         * Search recoder method.
         * </p>
         * 
         * @param recoder
         * @return
         */
        private static RecodableMethod get(Class<? extends Recodable> recoder) {
            RecodableMethod method = types.get(recoder);

            if (method == null) {
                method = new RecodableMethod(recoder);

                types.put(recoder, method);
            }
            return method;
        }
    }
}
