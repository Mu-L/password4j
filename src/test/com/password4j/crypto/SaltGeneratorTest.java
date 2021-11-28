package com.password4j.crypto;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.password4j.BadParametersException;
import com.password4j.P4jPropertyReader;


public class SaltGeneratorTest
{

    @Before
    public void init()
    {
        P4jPropertyReader.properties.setProperty("global.random.strong", "false");
        AlgorithmFinder.initialize();
    }

    @Test
    public void testSaltLength()
    {
        // GIVEN
        int length = 23;

        // WHEN
        byte[] salt = SaltGenerator.generate(length);

        // THEN
        Assert.assertNotNull(salt);
        Assert.assertEquals(length, salt.length);
    }


    @Test
    public void testSaltNoLength()
    {
        // GIVEN

        // WHEN
        byte[] salt = SaltGenerator.generate();

        // THEN
        Assert.assertNotNull(salt);
        Assert.assertEquals(64, salt.length);
    }

    @Test(expected = BadParametersException.class)
    public void testSaltNegativeLength()
    {
        // GIVEN

        // WHEN
        SaltGenerator.generate(-3);

        // THEN

    }

    @Test
    public void testSaltZeroLength()
    {
        // GIVEN
        int length = 0;

        // WHEN
        byte[] salt = SaltGenerator.generate(length);

        // THEN
        Assert.assertNotNull(salt);
        Assert.assertEquals(length, salt.length);
    }

    @Test
    public void testStrongRandom()
    {
        // GIVEN

        P4jPropertyReader.properties.setProperty("global.random.strong", "true");

        // WHEN
        AlgorithmFinder.initialize();

        // THEN

        try
        {
            assertEquals(SecureRandom.getInstanceStrong().getAlgorithm(), AlgorithmFinder.getSecureRandom().getAlgorithm());

        }
        catch (NoSuchAlgorithmException e)
        {
            //
        }

    }

    @Test
    public void testStrongRandom2()
    {
        // GIVEN
        P4jPropertyReader.properties.setProperty("global.random.strong", "true");

        // WHEN

        String old = getSecurityProperty();
        String strongAlg;
        try
        {
            strongAlg =  SecureRandom.getInstanceStrong().getAlgorithm();
        }
        catch (Exception e)
        {
            return;
        }

        setSecurityProperty("not and algorithm");
        AlgorithmFinder.initialize();

        // THEN


        assertNotEquals(strongAlg, AlgorithmFinder.getSecureRandom().getAlgorithm());
        setSecurityProperty(old);

    }

    private String getSecurityProperty()
    {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> Security.getProperty("securerandom.strongAlgorithms"));
    }

    private void setSecurityProperty(String value)
    {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Security.setProperty("securerandom.strongAlgorithms", value);
            return null;
        });
    }


}
