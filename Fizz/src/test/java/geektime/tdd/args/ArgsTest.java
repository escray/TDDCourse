package geektime.tdd.args;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArgsTest {
    // -l -p 8080 /usr/logs
    // list(index) [-l], [-p, 8080], [-d, /usr/logs]
    // map {-l: [], -p:[8080], -d:[/usr/logs]}
    // single option:
    // - Bool -l
    @Test
    public void should_set_boolean_option_to_true_if_flag_present(){
        BooleanOption option = Args.parse(BooleanOption.class, "-l");

        assertTrue(option.logging());
    }

    @Test
    public void should_set_boolean_option_to_false_if_flag_not_present(){
        BooleanOption option  = Args.parse(BooleanOption.class);

        assertFalse(option.logging());
    }

    static record BooleanOption(@Option("l") boolean logging) {}

    // - Integer -p 8080
    @Test
    public void should_parse_int_as_option_value(){
        IntOption option = Args.parse(IntOption.class, "-p", "8080");
        assertEquals(8080, option.port());
    }

    static record IntOption(@Option("p") int port) {}

    // - String -d /usr/logs
    @Test
    public void should_parse_string_as_option_value() {
        StringOption option = Args.parse(StringOption.class, "-d", "/usr/logs");
        assertEquals("/usr/logs", option.directory());
    }
    static record StringOption(@Option("d") String directory) {}
    // TODO: multi options: -l -p 8080 -d /usr/logs
    @Test
    public void should_parse_multi_options() {
        MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/logs");
        assertTrue(options.logging());
    }

    // sad path:
    // TODO: - bool -l t / -l t f
    // TODO: - int -p / -p 8080 8081
    // TODO: - string -d / -d /usr/logs /usr/vars
    // default value
    // TODO: - bool : false
    // TODO: - int : 0
    // TODO: - string : ""



    @Test
    @Disabled
    public void should_example_1(){
        MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/logs");
        assertTrue(options.logging());
        assertEquals(8080, options.port());
        assertEquals("/usr/logs", options.directory());
    }

    @Test
    @Disabled
    public void should_example_2(){
        ListOptions options = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");

        assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
        assertArrayEquals(new int[]{1, 2, -3, 5}, options.decimals());
    }

    static record MultiOptions(@Option("l")boolean logging, @Option("p")int port, @Option("d")String directory) {
    }

    static record ListOptions(@Option("g") String[] group, @Option("d") int[] decimals) {
    }

//    @Test
//    @Disabled
//    public void should() {
//        Arguments args = Args.parse("l:b, p:d, d:s", "-l", "-p", "8080", "-d", "/usr/logs");
//        args.getBool("l");
//        args.getInt("p");
//
//        Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");
//        options.logging();
//        options.port();
//    }
 }