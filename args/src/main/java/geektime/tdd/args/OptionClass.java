package geektime.tdd.args;

import geektime.tdd.args.Exceptions.IllegalOptionException;
import geektime.tdd.args.Exceptions.UnsupportedOptionTypeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class OptionClass<T> {
    private static Map<Class<?>, OptionParser> parsers;
    private Class<T> optionsClass;

    public OptionClass(Class<T> optionsClass, Map<Class<?>, OptionParser> parsers) {
        this.optionsClass = optionsClass;
        this.parsers = parsers;
    }

    public T parse(String[] args) {
        try {
            List<String> arguments = Arrays.asList(args);

            Constructor<?> constructor = this.optionsClass.getDeclaredConstructors()[0];

            Object[] values = Arrays.stream(constructor.getParameters()).map(it -> parseOption(arguments, it)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(List<String> arguments, Parameter parameter) {

        if (!parameter.isAnnotationPresent(Option.class))
            throw new IllegalOptionException(parameter.getName());
        Option option = parameter.getAnnotation(Option.class);


        if (!parsers.containsKey(parameter.getType())) {
            throw new UnsupportedOptionTypeException(option.value(), parameter.getType());
        }
        return parsers.get(parameter.getType()).parse(arguments, option);
    }
}
