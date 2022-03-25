package geektime.tdd.args;

import java.util.List;
import java.util.function.Function;

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

        // 达到列表末尾？
        // 通过注释还是通过方法名来表明意图
        if (isReachEndOfList(arguments, index) ||
                isFollowedByOtherFlag(arguments, index))
            throw new InsufficientArgumentException(option.value());

        if (secondArgumentIsNotFollowedByAFlag(arguments, index)
        ) throw new TooManyArgumentsException(option.value());

        return valueParser.apply(arguments.get(index + 1));
    }

    private boolean secondArgumentIsNotFollowedByAFlag(List<String> arguments, int index) {
        return index + 2 < arguments.size()
                && !arguments.get(index + 2).contains("-");
    }

    private boolean isFollowedByOtherFlag(List<String> arguments, int index) {
        return arguments.get(index + 1).startsWith("-");
    }

    private boolean isReachEndOfList(List<String> arguments, int index) {
        return index + 1 == arguments.size();
    }
}
