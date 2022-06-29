package com.password4j.generator;

import com.password4j.AlgorithmFinder;

import java.time.Duration;
import java.time.Instant;

public class PasswordGenerator
{


    public String HOTP(byte[] key, long counter)
    {
        HOTPGenerator hotpGenerator = AlgorithmFinder.getHOTPGeneratorInstance();
        return hotpGenerator.generate(key, counter);
    }

    public String TOTP(byte[] key, Instant instant)
    {
        TOTPGenerator totpGenerator = AlgorithmFinder.getTOTPGeneratorInstance();
        return totpGenerator.generate(key, instant);
    }








}
