package geektime.tdd.args;

import java.util.List;

interface OptionParserOrigin {
    Object parse(List<String> arguments, Option option);
}
