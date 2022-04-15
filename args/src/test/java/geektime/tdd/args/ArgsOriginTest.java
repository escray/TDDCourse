package geektime.tdd.args;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArgsOriginTest {
    @Test
    public void should_set_boolean_option_to_true_if_flag_present() {
        BooleanOption option = ArgsOrigin.parse(BooleanOption.class, "-l");
        assertTrue(option.logging());
    }

    @Test
    public void should_set_boolean_option_to_false_if_flag_not_present() {
        BooleanOption option = ArgsOrigin.parse(BooleanOption.class);
        assertFalse(option.logging());
    }

    @Test
    public void should_parse_int_as_option_value() {
        IntOption option = ArgsOrigin.parse(IntOption.class, "-p", "8080");
        assertEquals(8080, option.port());
    }

    @Test
    public void should_get_string_as_option_value() {
        StringOption option = ArgsOrigin.parse(StringOption.class, "-d", "/usr/logs");
        assertEquals("/usr/logs", option.directory());
    }

    @Test
    public void should_parse_multi_options() {

    }



    record BooleanOption(@Option("l") boolean logging) {
    }

    record IntOption(@Option("p") int port) {
    }

    record StringOption(@Option("d") String directory) {
    }
}
