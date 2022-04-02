package geektime.tdd.args;

import geektime.tdd.args.Exceptions.IllegalValueException;
import geektime.tdd.args.Exceptions.InsufficientArgumentException;
import geektime.tdd.args.Exceptions.TooManyArgumentsException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionParsersTest {

    @Nested
    class UnaryOptionParser {

        @Test // Sad Path
        public void should_not_accept_extra_argument_for_single_valued_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                OptionParsers.unary(0, Integer::parseInt).parse(asList("-p", "8080", "8081"), option("p"));
            });

            assertEquals("p", e.getOption());
        }

        @Test
        public void should_not_accept_extra_argument_for_string_valued_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                OptionParsers.unary("", String::valueOf).parse(asList("-d", "/usr/logs", "/usr/vars"), option("d"));
            });

            assertEquals("d", e.getOption());
        }

        @ParameterizedTest // Sad Path
        @ValueSource(strings = {"-p -l", "-p"})
        public void should_not_accept_insufficient_argument_for_single_valued_option(String arguments) {
            InsufficientArgumentException e = assertThrows(InsufficientArgumentException.class, () -> {
                OptionParsers.unary(0, Integer::parseInt).parse(asList(arguments.split(" ")), option("p"));
            });
            assertEquals("p", e.getOption());
        }

        @Test // Default
        public void should_set_default_value_to_0_for_int_option() {
            Function<String, Object> whatever = (it) -> null;
            Object defaultValue = new Object();
            assertSame(defaultValue, OptionParsers.unary(defaultValue, whatever).parse(asList(), option("p")));
        }

        @Test // Happy path
        // State Verification
        public void should_parse_value_if_flag_present() {
            Object parsed = new Object();
            Function<String, Object> parse = (it) -> parsed;
            Object whatever = new Object();
            assertSame(parsed, OptionParsers.unary(whatever, parse).parse(asList("-p", "8080"), option("p")));
            assertEquals(8080, OptionParsers.unary(0, Integer::parseInt).parse(asList("-p", "8080"), option("p")));
        }

        // Behavior Verification
        @Test
        public void should_parse_value_if_flag_present_behavior() {
            Function parser = mock(Function.class);
            OptionParsers.unary(any(), parser).parse(asList("-p", "8080"), option("p"));

            verify(parser).apply("8080");
        }
    }

    @Nested
    class BooleanOptionParserTest {
        @Test // Sad Path
        public void should_not_accept_extra_argument_for_boolean_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                OptionParsers.bool().parse(asList("-l", "t"), option("l"));
            });
            assertEquals("l", e.getOption());
        }

        @Test
        public void should_not_accept_extra_arguments_for_boolean_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                OptionParsers.bool().parse(asList("-l", "t", "f"), option("l"));
            });

            assertEquals("l", e.getOption());
        }

        @Test // Default Value
        public void should_set_default_value_to_false_if_option_not_present() {
            assertFalse(OptionParsers.bool().parse(asList(), option("l")));
        }

        @Test // Happy Path
        public void should_set_default_value_to_false_if_option_present() {
            assertTrue(OptionParsers.bool().parse(asList("-l"), option("l")));
        }
    }

    @Nested
    class ListOptionParser {
        // DONE: -g "this" "is" {"this", "is"}
        // State Verification
        @Test
        public void should_parse_list_value() {
            String[] value = OptionParsers.list(String[]::new, String::valueOf).parse(asList("-g", "this", "is"), option("g"));
            assertArrayEquals(new String[]{"this", "is"}, value);
        }

        // Behavior
        @Test
        public void should_parse_list_value_behavior() {
            Function parser = mock(Function.class);

            OptionParsers.list(Object[]::new, parser).parse(asList("-g", "this", "is"), option("g"));

            InOrder order = inOrder(parser, parser);
            order.verify(parser).apply("this");
            order.verify(parser).apply("is");
        }

        @Test
        public void should_not_treat_negative_int_as_flag() {
            Integer[] value = OptionParsers.list(Integer[]::new, Integer::parseInt).parse(asList("-g", "-1", "-2"), option("g"));
            assertArrayEquals(new Integer[]{-1, -2}, value);
        }

        // TODO: default value []
        @Test
        public void should_use_empty_array_as_default_value() {
            String[] value = OptionParsers.list(String[]::new, String::valueOf).parse(asList(), option("g"));
            assertEquals(0, value.length);
        }

        // TODO: -d a throw exception
        @Test
        public void should_throw_exception_if_value_parser_cant_parse_value() {
            Function<String, String> parser = (it) -> {
                throw new RuntimeException();
            };
            IllegalValueException e = assertThrows(IllegalValueException.class, () ->
                    OptionParsers.list(String[]::new, parser).parse(asList("-g", "this", "is"), option("g")));
            assertEquals("g", e.getOption());
            assertEquals("this", e.getValue());

        }
    }

    static Option option(String value) {
        return new Option() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Option.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }
}