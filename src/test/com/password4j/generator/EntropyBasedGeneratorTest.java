package com.password4j.generator;

import org.junit.Test;

import java.util.Arrays;

public class EntropyBasedGeneratorTest
{


    @Test
    public void test()
    {
        EntropyBasedGenerator generator = EntropyBasedGenerator.getInstance(EntropyBasedGenerator.LOWERCASE_LETTERS);

        System.out.println(generator.calculateEntropy("/dev/null"));

    }
}
