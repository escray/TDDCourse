package geektime.tdd.args;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ReflectionBasedOptionClassTest {
    @Test
    public void should_treat_parameter_with_option_as_option() {
        OptionClassLondon<IntOption> optionClass = new ReflectionBasedOptionClass(IntOption.class);
        assertArrayEquals(new String[]{"p"}, optionClass.getOptionNames());
    }

    static record IntOption(@Option("p") int port) {
    }

}
