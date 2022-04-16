package geektime.tdd.args;

import java.util.List;

class BooleanOptionParser implements OptionParserOrigin {
    @Override
    public Object parse(List<String> arguments, Option option) {
        return arguments.contains("-" + option.value());
    }
}
