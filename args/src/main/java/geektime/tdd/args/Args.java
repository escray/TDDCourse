package geektime.tdd.args;

import geektime.tdd.args.Exceptions.IllegalOptionException;
import geektime.tdd.args.Exceptions.UnsupportedOptionTypeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Args {
    public static <T> T parse(Class<T> optionsClass, String... args) {
        try {
            List<String> arguments = Arrays.asList(args);
            Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];

            Object[] values = Arrays.stream(constructor.getParameters()).map(it -> parseOption(arguments, it, PARSERS)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(List<String> arguments, Parameter parameter, Map<Class<?>, OptionParser> parsers) {

        if (!parameter.isAnnotationPresent(Option.class))
            throw new IllegalOptionException(parameter.getName());
        Option option = parameter.getAnnotation(Option.class);
        if (!parsers.containsKey(parameter.getType())) {
            throw new UnsupportedOptionTypeException(option.value(), parameter.getType());
        }
        return parsers.get(parameter.getType()).parse(arguments, option);
    }

    private static Map<Class<?>, OptionParser> PARSERS = Map.of(
            boolean.class, OptionParsers.bool(),
            int.class, OptionParsers.unary(0, Integer::parseInt),
            String.class, OptionParsers.unary("", String::valueOf),
            String[].class, OptionParsers.list(String[]::new, String::valueOf),
            Integer[].class, OptionParsers.list(Integer[]::new, Integer::parseInt)
    );
}
