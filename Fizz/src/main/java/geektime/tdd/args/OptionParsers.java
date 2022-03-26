package geektime.tdd.args;

import geektime.tdd.args.Exceptions.IllegalValueException;
import geektime.tdd.args.Exceptions.InsufficientArgumentException;
import geektime.tdd.args.Exceptions.TooManyArgumentsException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

class OptionParsers {

    public static OptionParser<Boolean> bool() {
        return (arguments, option) ->
                getValues(arguments, option, 0)
                        .map(it -> true).orElse(false);
    }

    public static <T> OptionParser<T> unary(T defaultValue, Function<String, T> valueParser) {
        return (arguments, option) -> getValues(arguments, option, 1)
                        .map(it -> parseValue(option, it.get(0), valueParser))
                        .orElse(defaultValue);
    }

    public static <T> OptionParser<T[]> list(IntFunction<T[]> generator, Function<String, T> valueParser) {
        return (arguments, option) -> getValues(arguments, option)
                .map(it -> it.stream().map(value -> parseValue(option, value, valueParser))
                        .toArray(generator)).orElse(generator.apply(0));
    }

    private static Optional<List<String>> getValues(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        return Optional.ofNullable(index == -1 ? null : getFollowingValues(arguments, index));
    }

    private static Optional<List<String>> getValues(List<String> arguments, Option option, int expectedSize) {
        return getValues(arguments, option).map(it -> checkSize(option, expectedSize, it));
    }

    private static List<String> checkSize(Option option, int expectedSize, List<String> values) {
        if (values.size() < expectedSize) throw new InsufficientArgumentException(option.value());
        if (values.size() > expectedSize) throw new TooManyArgumentsException(option.value());
        return values;
    }

    private static <T> T parseValue(Option option, String value, Function<String, T> valueParser) {
        try {
            return valueParser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(option.value(), value);
        }
    }

    private static List<String> getFollowingValues(List<String> arguments, int index) {
        return arguments.subList(index + 1, IntStream.range(index + 1, arguments.size())
                .filter(it -> arguments.get(it).matches("^-[a-zA-Z-]+$"))
                .findFirst().orElse(arguments.size()));
    }
}