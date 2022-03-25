package geektime.tdd.args;

import java.util.List;

public class IllegalValueException extends RuntimeException {
    public IllegalValueException(String value, List<String> values) {
    }
}
