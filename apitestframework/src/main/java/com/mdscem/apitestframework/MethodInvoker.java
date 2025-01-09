package com.mdscem.apitestframework;

import org.assertj.core.api.StringAssert;
import org.assertj.core.api.AbstractAssert;

import org.assertj.core.api.Assertions;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

public class MethodInvoker {

    public static <A extends AbstractAssert<?, ?>> void executeAssertions(
            A assertObject, String... methodChain) throws Exception {
        for (String methodCall : methodChain) {
            String methodName = extractMethodName(methodCall);
            Object[] parsedArgs = extractMethodArguments(methodCall);
            System.out.println("Parsed arguments: " + Arrays.toString(parsedArgs));
            // Find and invoke the method dynamically
            Method method = findCompatibleMethod(assertObject.getClass(), methodName, parsedArgs);
            Object[] preparedArgs = prepareArguments(method, parsedArgs);
            System.out.println("Prepared arguments for " + methodName + ": " + Arrays.toString(preparedArgs));
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

                System.out.println("Checking method: " + method.getName());
                System.out.println("Method parameter types: " + Arrays.toString(method.getParameterTypes()));
                System.out.println("Provided arguments: " + Arrays.toString(args));
                System.out.println("Provided argument types: " + Arrays.toString(
                        Arrays.stream(args).map(arg -> arg != null ? arg.getClass().getName() : "null").toArray()
                ));

                // Handle case for methods that accept a single CharSequence or Iterable
                if (args.length == 1 && args[0] instanceof CharSequence) {
                    if (method.getParameterCount() == 1) {
                        Class<?> paramType = method.getParameterTypes()[0];
                        if (paramType.equals(CharSequence.class) || paramType.equals(Iterable.class) || CharSequence.class.isAssignableFrom(paramType)) {
                            return method;
                        }
                    }
                }

                // Handle varargs or exact parameter count matching
                if (method.isVarArgs() || method.getParameterCount() == args.length) {
                    if (isCompatibleMethod(method, args)) {
                        return method;
                    }
                }

                // Handle methods expecting an Iterable with multiple arguments
                if (args.length > 1 && Iterable.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    // Check if method can handle multiple arguments as an Iterable (e.g., List, Set)
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType.isAssignableFrom(Collection.class)) {
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





    public static void main(String[] args) {
        try {
            // Example: Integer Assertions
            int actualValue = 42;
            executeAssertions(
                    Assertions.assertThat(actualValue),
                    "isNotNull()",
                    "isGreaterThan(10)",
                    "isLessThanOrEqualTo(50)",
                    "isInstanceOf(Integer.class)",
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
                    "isNotEqualToIgnoringCase(\"assertj ggis powerful!\")"
            );

            String result= "The quick brown fox jumps over the lazy dog";

            // Execute assertions dynamically
            executeAssertions(
                    Assertions.assertThat(result),
                    "isNotNull()",
                    "contains(\"quick\", \"fox\", \"lazy\")",
                    "containsIgnoringCase(\"THE\")",
                    "doesNotContain(\"cat\", \"foxes\")",
                    "startsWith(\"The\")",
                    "endsWith(\"dog\")"
            );

                        executeAssertions(
                    Assertions.assertThat(Arrays.asList(1, 2, 3)),
                    "isNotEmpty()",
                    "hasSize(3)"
            );



            System.out.println("All assertions passed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
