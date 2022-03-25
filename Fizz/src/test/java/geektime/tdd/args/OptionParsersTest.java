package geektime.tdd.args;

import geektime.tdd.args.Exceptions.InsufficientArgumentException;
import geektime.tdd.args.Exceptions.TooManyArgumentsException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static geektime.tdd.args.OptionParsersTest.BooleanOptionParserTest.option;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

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
        public void should_parse_value_if_flag_present() {
            Object parsed = new Object();
            Function<String, Object> parse = (it) -> parsed;
            Object whatever = new Object();
            assertSame(parsed, OptionParsers.unary(whatever, parse).parse(asList("-p", "8080"), option("p")));
            assertEquals(8080, OptionParsers.unary(0, Integer::parseInt).parse(asList("-p", "8080"), option("p")));
        }

        private Object from(String string) {
            return null;
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
}