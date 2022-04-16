package geektime.tdd.args;

import java.util.List;

class StringOptionParser implements OptionParserOrigin {
    @Override
    public Object parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        return arguments.get(index + 1);
    }
}
