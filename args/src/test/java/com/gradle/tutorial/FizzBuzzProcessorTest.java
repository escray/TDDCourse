package com.gradle.tutorial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class FizzBuzzProcessorTest {
    FizzBuzzProcessor fb;

    @TestFactory
    public void factory() {
        fb = new FizzBuzzProcessor();
    }

    @Test
    public void FizzBuzzNormalNumbers() {
        Assertions.assertEquals("1", fb.convert(1));
        Assertions.assertEquals("2", fb.convert(2));
    }

    @Test
    public void FizzBuzzThreeNumbers() {
        Assertions.assertEquals("Fizz", fb.convert(3));
    }

    @Test
    public void FizzBuzzFiveNumbers() {
        Assertions.assertEquals("Buzz", fb.convert(5));
    }

    @Test
    public void FizzBuzzFifteenNumbers() {
        Assertions.assertEquals("FizzBuzz", fb.convert(15));
    }
}