package com.mdscem.apitestframework;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodInvoker {

    public static <A extends AbstractAssert<?, ?>> void executeAssertions(
            A assertObject, String... methodChain) throws Exception {
        for (String methodCall : methodChain) {
            String methodName = extractMethodName(methodCall);
            Object[] parsedArgs = extractMethodArguments(methodCall);

            // Find and invoke the method dynamically
            Method method = findCompatibleMethod(assertObject.getClass(), methodName, parsedArgs);
            Object[] preparedArgs = prepareArguments(method, parsedArgs);

            // Execute method and continue method chaining
            assertObject = (A) method.invoke(assertObject, preparedArgs);
        }
    }

    private static String extractMethodName(String methodCall) {
        return methodCall.contains("(")
                ? methodCall.substring(0, methodCall.indexOf("("))
                : methodCall;
    }

    private static Object[] extractMethodArguments(String methodCall) {
        if (!methodCall.contains("(")) {
            return new Object[0];
        }
        String argsString = methodCall.substring(methodCall.indexOf("(") + 1, methodCall.indexOf(")"));
        if (argsString.isEmpty()) {
            return new Object[0];
        }
        return parseArguments(argsString.split(","));
    }

    private static Object[] parseArguments(String[] args) {
        List<Object> parsedArgs = new ArrayList<>();
        for (String arg : args) {
            parsedArgs.add(parseArgument(arg.trim()));
        }
        return parsedArgs.toArray();
    }

    private static Object parseArgument(String arg) {
        try {
            if (arg.startsWith("[") && arg.endsWith("]")) {
                // Parse arrays or lists
                String[] elements = arg.substring(1, arg.length() - 1).split(",");
                return Arrays.asList(parseArguments(elements));
            } else if (arg.endsWith(".class")) {
                // Parse class arguments
                String className = arg.substring(0, arg.lastIndexOf(".class"));
                return Class.forName("java.lang." + className);
            } else if (arg.startsWith("\"") && arg.endsWith("\"")) {
                // Parse strings
                return arg.substring(1, arg.length() - 1);
            } else if (arg.matches("\\d+")) {
                // Parse integers
                return Integer.valueOf(arg);
            } else if (arg.matches("\\d+\\.\\d+")) {
                // Parse doubles
                return Double.valueOf(arg);
            } else if ("true".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg)) {
                // Parse booleans
                return Boolean.valueOf(arg);
            } else if (arg.contains(".")) {
                // Parse enums (ClassName.EnumName)
                String[] parts = arg.split("\\.");
                if (parts.length >= 2) {
                    String className = String.join(".", Arrays.copyOf(parts, parts.length - 1));
                    String enumName = parts[parts.length - 1];
                    Class<?> enumClass = Class.forName(className);
                    if (enumClass.isEnum()) {
                        return Enum.valueOf((Class<Enum>) enumClass, enumName);
                    }
                }
            }
            // Default: assume other types
            return arg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing argument: " + arg, e);
        }
    }


    private static Method findCompatibleMethod(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (method.isVarArgs() || method.getParameterCount() == args.length) {
                    if (isCompatibleMethod(method, args)) {
                        return method;
                    }
                }
            }
        }
        throw new NoSuchMethodException("No method found for: " + methodName);
    }

    private static boolean isCompatibleMethod(Method method, Object[] args) {
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                if (paramTypes[i].isPrimitive()) {
                    if (!isPrimitiveCompatible(paramTypes[i], args[i])) {
                        return false;
                    }
                } else if (!paramTypes[i].isAssignableFrom(args[i].getClass())) {
                    if (!isWrapperCompatible(paramTypes[i], args[i].getClass())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isPrimitiveCompatible(Class<?> paramType, Object arg) {
        if (paramType == int.class && arg instanceof Integer) return true;
        if (paramType == long.class && arg instanceof Long) return true;
        if (paramType == double.class && arg instanceof Double) return true;
        if (paramType == boolean.class && arg instanceof Boolean) return true;
        return false;
    }

    private static boolean isWrapperCompatible(Class<?> paramType, Class<?> argClass) {
        if (paramType.isAssignableFrom(argClass)) return true;
        if (paramType == Integer.class && argClass == int.class) return true;
        if (paramType == Double.class && argClass == double.class) return true;
        if (paramType == Boolean.class && argClass == boolean.class) return true;
        return false;
    }

    private static Object[] prepareArguments(Method method, Object[] args) {
        if (method.isVarArgs()) {
            Object[] newArgs = new Object[method.getParameterCount()];
            System.arraycopy(args, 0, newArgs, 0, method.getParameterCount() - 1);

            Class<?> varArgType = method.getParameterTypes()[method.getParameterCount() - 1].getComponentType();
            Object varArgs = Array.newInstance(varArgType, args.length - method.getParameterCount() + 1);
            for (int i = method.getParameterCount() - 1; i < args.length; i++) {
                Array.set(varArgs, i - (method.getParameterCount() - 1), args[i]);
            }
            newArgs[method.getParameterCount() - 1] = varArgs;
            return newArgs;
        }
        return args;
    }

    public static void main(String[] args) {
        try {
            // Example: Integer Assertions
            int actualValue = 42;
            executeAssertions(
                    Assertions.assertThat(actualValue),
                    "isNotNull()",
                    "isGreaterThan(10)",
                    "isLessThanOrEqualTo(50)",
                    "isEqualTo(42)"
            );

            // Example: String Assertions
            String actualString = "AssertJ is powerful!";
            executeAssertions(
                    Assertions.assertThat(actualString),
                    "isNotNull()",
                    "isMixedCase()",
                    "startsWith(\"AssertJ\")",
                    "endsWith(\"powerful!\")",
                    "contains(\"powerful!\")",

                    "isEqualToIgnoringCase(\"assertj is powerful!\")"
            );


            System.out.println("All assertions passed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
