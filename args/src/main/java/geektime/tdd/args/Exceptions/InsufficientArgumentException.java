package geektime.tdd.args.Exceptions;

public class InsufficientArgumentException extends RuntimeException {
    private final String option;

    public InsufficientArgumentException(String option) {
        super(option);
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
