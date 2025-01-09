package com.mdscem.apitestframework.requestprocessor.validation;

import org.assertj.core.api.AbstractAssert;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

public class AssertJValidation {

    //Dynamically executes a chain of assertion methods on an AssertJ assertion object.
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

    //Extracts the method name from a method call string.
    private static String extractMethodName(String methodCall) {
        return methodCall.contains("(")
                ? methodCall.substring(0, methodCall.indexOf("("))
                : methodCall;
    }

    //Extracts method arguments from a method call string.
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

    //Converts a list of argument strings into actual Java objects.
    private static Object[] parseArguments(String[] args) {
        List<Object> parsedArgs = new ArrayList<>();
        for (String arg : args) {
            parsedArgs.add(parseArgument(arg.trim()));
        }
        return parsedArgs.toArray();
    }

    //Converts a single argument string into an appropriate Java object.
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
            return arg;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing argument: " + arg, e);
        }
    }

    /** responsible for locating the correct method in the AssertJ class (or its subclasses)
     * that matches the name and arguments extracted from the assertion chain.
     */
    private static Method findCompatibleMethod(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {

                // Handle exact match for parameter count
                if (method.getParameterCount() == args.length && isCompatibleMethod(method, args)) {
                    return method;
                }

                // Handle varargs method
                if (method.isVarArgs()) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (args.length >= paramTypes.length - 1) {
                        // Check if non-varargs parameters are compatible
                        boolean nonVarArgsCompatible = isCompatibleMethod(method, Arrays.copyOf(args, paramTypes.length - 1));
                        if (nonVarArgsCompatible) {
                            // Check if remaining arguments are compatible with varargs type
                            Class<?> varArgType = paramTypes[paramTypes.length - 1].getComponentType();
                            boolean varArgsCompatible = true;
                            for (int i = paramTypes.length - 1; i < args.length; i++) {
                                if (!isCompatibleType(varArgType, args[i])) {
                                    varArgsCompatible = false;
                                    break;
                                }
                            }
                            if (varArgsCompatible) {
                                return method;
                            }
                        }
                    }
                }

                // Handle methods with an `Iterable` as the first parameter and multiple arguments
                if (args.length > 1 && Iterable.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException("No method found for " + methodName + " or argument mismatch");
    }


    //Checks if a method is compatible with the given arguments.
    private static boolean isCompatibleMethod(Method method, Object[] args) {
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            if (!isCompatibleType(paramTypes[i], args[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCompatibleType(Class<?> paramType, Object arg) {
        if (arg == null) {
            return !paramType.isPrimitive(); // Null can be assigned to non-primitive types
        }
        if (paramType.isAssignableFrom(arg.getClass())) {
            return true; // Exact match or superclass
        }
        // Handle primitive types
        if (paramType.isPrimitive()) {
            return (paramType == int.class && arg instanceof Integer)
                    || (paramType == long.class && arg instanceof Long)
                    || (paramType == double.class && arg instanceof Double)
                    || (paramType == boolean.class && arg instanceof Boolean)
                    || (paramType == char.class && arg instanceof Character)
                    || (paramType == float.class && arg instanceof Float)
                    || (paramType == byte.class && arg instanceof Byte)
                    || (paramType == short.class && arg instanceof Short);
        }
        return false;
    }

    //Prepares arguments for method invocation.
    private static Object[] prepareArguments(Method method, Object[] args) {
        if (method.isVarArgs()) {
            // If the method expects varargs, ensure we create an array for the varargs
            int paramCount = method.getParameterCount();
            Class<?> varArgType = method.getParameterTypes()[paramCount - 1].getComponentType();

            // If the argument count matches the parameter count, we need to treat the last argument as varargs
            Object[] newArgs = new Object[paramCount];

            // Copy the first parameters (those before the varargs)
            System.arraycopy(args, 0, newArgs, 0, paramCount - 1);

            // Create an array for the varargs and copy the remaining arguments
            Object varArgsArray = Array.newInstance(varArgType, args.length - paramCount + 1);
            for (int i = paramCount - 1; i < args.length; i++) {
                Array.set(varArgsArray, i - (paramCount - 1), args[i]);
            }

            // Set the varargs argument in the new argument array
            newArgs[paramCount - 1] = varArgsArray;
            return newArgs;
        }

        // If the method accepts an Iterable, convert the arguments into a List
        else if (method.getParameterCount() == 1 && Iterable.class.isAssignableFrom(method.getParameterTypes()[0])) {
            List<Object> list = new ArrayList<>();
            list.addAll(Arrays.asList(args)); // Add all the arguments to a list
            return new Object[]{list}; // Wrap the list in an array and return
        }

        // Default return of arguments as they are
        return args;
    }
}
