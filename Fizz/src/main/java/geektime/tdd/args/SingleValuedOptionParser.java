package geektime.tdd.args;

import java.util.List;
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
        int index = arguments.indexOf("-" + option.value());
        if (index == -1) return defaultValue;

        List<String> values = getFollowingValues(arguments, index);

        if (values.size() < 1)
            throw new InsufficientArgumentException(option.value());

        if (values.size() > 1)
            throw new TooManyArgumentsException(option.value());

        return valueParser.apply(arguments.get(index + 1));
    }

    private List<String> getFollowingValues(List<String> arguments, int index) {
        int followingFlag = IntStream.range(index + 1, arguments.size())
                .filter(it -> arguments.get(it).startsWith("-"))
                .findFirst().orElse(arguments.size());

        List<String> values = arguments.subList(index + 1, followingFlag);
        return values;
    }
}
