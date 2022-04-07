package geektime.tdd.args;

import geektime.tdd.args.Exceptions.IllegalOptionException;
import geektime.tdd.args.Exceptions.UnsupportedOptionTypeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Args<T> {
    // 默认的配置
    private static Map<Class<?>, OptionParser> PARSERS = Map.of(
            boolean.class, OptionParsers.bool(),
            int.class, OptionParsers.unary(0, Integer::parseInt),
            String.class, OptionParsers.unary("", String::valueOf),
            String[].class, OptionParsers.list(String[]::new, String::valueOf),
            Integer[].class, OptionParsers.list(Integer[]::new, Integer::parseInt)
    );

    // 对外的 API 接口
    public static <T> T parse(Class<T> optionsClass, String... args) {
        return new Args<T>(optionsClass, PARSERS).parse(args);
        //return parse(optionsClass, PARSERS, args);
    }

    private Class<T> optionsClass;
    private Map<Class<?>, OptionParser> parsers;

    public Args(Class<T> optionsClass, Map<Class<?>, OptionParser> parsers) {
        this.optionsClass = optionsClass;
        this.parsers = parsers;
    }

    public T parse(String... args) {
        try {
            List<String> arguments = Arrays.asList(args);
            Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];

            Object[] values = Arrays.stream(constructor.getParameters()).map(it -> parseOption(arguments, it)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object parseOption(List<String> arguments, Parameter parameter) {
        if (!parameter.isAnnotationPresent(Option.class))
            throw new IllegalOptionException(parameter.getName());
        Option option = parameter.getAnnotation(Option.class);
        if (!parsers.containsKey(parameter.getType())) {
            throw new UnsupportedOptionTypeException(option.value(), parameter.getType());
        }
        return parsers.get(parameter.getType()).parse(arguments, option);
    }
}
