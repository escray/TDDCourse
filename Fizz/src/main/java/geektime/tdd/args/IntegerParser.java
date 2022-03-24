package geektime.tdd.args;

import java.util.List;
import java.util.function.Function;

class IntegerParser implements OptionParser {
    Function<String, Object> valueParser = Integer::parseInt;

    private IntegerParser() {

    }

    public IntegerParser(Function<String, Object> valueParser) {
        this.valueParser = valueParser;
    }

    public static OptionParser createIntegerParser() {
        return new IntegerParser();
    }

    @Override
    public Object parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        String value = arguments.get(index + 1);
        return valueParser.apply(value);
    }
}
