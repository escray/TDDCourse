package geektime.tdd.args;

import geektime.tdd.args.Exceptions.IllegalValueException;
import geektime.tdd.args.Exceptions.InsufficientArgumentException;
import geektime.tdd.args.Exceptions.TooManyArgumentsException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

class SingleValuedOptionParser<T> implements OptionParser<T> {
    Function<String, T> valueParser;
    T defaultValue;

    public SingleValuedOptionParser(T defaultValue, Function<String, T> valueParser) {
        this.valueParser = valueParser;
        this.defaultValue = defaultValue;
    }

    @Override
    public T parse(List<String> arguments, Option option) {
        return getValues(arguments, option, 1).map(it -> parseValue(option, it.get(0))).orElse(defaultValue);
    }

    static Optional<List<String>> getValues(List<String> arguments, Option option, int expectedSize) {
        int index = arguments.indexOf("-" + option.value());
        if (index == -1) return Optional.empty();

        List<String> values = getFollowingValues(arguments, index);

        if (values.size() < expectedSize) throw new InsufficientArgumentException(option.value());
        if (values.size() > expectedSize) throw new TooManyArgumentsException(option.value());
        return Optional.of(values);
    }

    private T parseValue(Option option, String value) {
        try {
            return valueParser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(option.value(), value);
        }
    }

    static List<String> getFollowingValues(List<String> arguments, int index) {
        return arguments.subList(index + 1, IntStream.range(index + 1, arguments.size())
                .filter(it -> arguments.get(it).startsWith("-"))
                .findFirst().orElse(arguments.size()));
    }
}