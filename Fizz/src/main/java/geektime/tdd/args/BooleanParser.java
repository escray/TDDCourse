package geektime.tdd.args;

import java.util.List;

import static geektime.tdd.args.SingleValuedOptionParser.getValues;

class BooleanParser implements OptionParser<Boolean> {
    @Override
    public Boolean parse(List<String> arguments, Option option) {
        return getValues(arguments, option, 0)
                .map(it -> true).orElse(false);
    }
}
