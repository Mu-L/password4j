/*
 *  (C) Copyright 2020 Password4j (http://password4j.com/).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.password4j;

import com.password4j.types.Argon2;
import com.password4j.types.Bcrypt;
import com.password4j.types.Hmac;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

import static org.junit.Assert.assertTrue;


public class PasswordTest
{

    private static final String PASSWORD = "password";
    private static final String SALT = "salt";
    private static final String PEPPER = "pepper";

    private static final SecureString SECURE_PASSWORD = new SecureString(PASSWORD.toCharArray());

    private static final Random RANDOM = new Random();

    @Test
    public void testCoherence()
    {
        // GIVEN

        // WHEN
        Hash hash1 = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withPBKDF2();
        Hash hash2 = Password.hash(PASSWORD).addPepper(PEPPER).withBcrypt();
        Hash hash3 = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withScrypt();

        // THEN
        assertTrue(Password.check(PASSWORD, hash1));
        assertTrue(Password.check(PASSWORD, hash2));
        assertTrue(Password.check(PASSWORD, hash3));
    }


    @Test
    public void testRawCheck1()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withCompressedPBKDF2();
        String hashed = hash.getResult();

        // WHEN
        PBKDF2Function strategy = CompressedPBKDF2Function.getInstanceFromHash(hashed);

        // THEN
        assertTrue(strategy.check(PEPPER + PASSWORD, hashed));
        assertTrue(Password.check(PASSWORD, hashed).addPepper(PEPPER).withCompressedPBKDF2());
    }

    @Test
    public void testRawCheck2()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).withBcrypt();
        String hashed = hash.getResult();

        // WHEN
        BcryptFunction strategy = AlgorithmFinder.getBcryptInstance();

        // THEN
        assertTrue(strategy.check(PEPPER + PASSWORD, hashed));
        assertTrue(Password.check(PASSWORD, hashed).addPepper(PEPPER).withBcrypt());
    }

    @Test
    public void testRawCheck3()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withScrypt();
        String hashed = hash.getResult();

        // WHEN
        ScryptFunction strategy = ScryptFunction.getInstanceFromHash(hashed);

        // THEN
        assertTrue(strategy.check(PEPPER + PASSWORD, hashed));
        assertTrue(Password.check(PASSWORD, hashed).addPepper(PEPPER).withScrypt());
    }


    @Test
    public void testRawCheck4()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withMessageDigest();
        String hashed = hash.getResult();

        // WHEN
        MessageDigestFunction strategy = MessageDigestFunction.getInstance("SHA-512");

        // THEN
        assertTrue(strategy.check(PEPPER + PASSWORD + SALT, hashed));
        assertTrue(Password.check(PASSWORD, hashed).addSalt(SALT).addPepper(PEPPER).withMessageDigest());
    }

    @Test
    public void testRawCheck5()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addSalt(SALT).withArgon2();
        String hashed = hash.getResult();

        // WHEN
        Argon2Function strategy = Argon2Function.getInstanceFromHash(hashed);

        // THEN
        assertTrue(strategy.check(PASSWORD, hashed));
        assertTrue(Password.check(PASSWORD, hashed).addSalt(SALT).withArgon2());
    }

    @Test
    public void testRawUpdate1()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withCompressedPBKDF2();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate().addNewSalt("newsalt").addNewPepper("newpepper").withCompressedPBKDF2();

        // THEN
        assertTrue(update.isVerified());
        Assert.assertEquals(Password.hash(PASSWORD).addPepper("newpepper").addSalt("newsalt").withCompressedPBKDF2().getResult(), update.getHash().getResult());
    }

    @Test
    public void testRawUpdate2()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).withBcrypt();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate().addNewSalt("$2a$07$W3mOfB5auMDG3EitumH0S.").addNewPepper("newpepper").withBcrypt();

        // THEN
        assertTrue(update.isVerified());
        Assert.assertEquals(Password.hash(PASSWORD).addPepper("newpepper").addSalt("$2a$07$W3mOfB5auMDG3EitumH0S.").withBcrypt().getResult(), update.getHash().getResult());
    }

    @Test
    public void testRawUpdate3()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withScrypt();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate().addNewSalt("newsalt").addNewPepper("newpepper").withScrypt();

        // THEN
        assertTrue(update.isVerified());
        Assert.assertEquals(Password.hash(PASSWORD).addPepper("newpepper").addSalt("newsalt").withScrypt().getResult(), update.getHash().getResult());
    }


    @Test
    public void testRawUpdate4()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withMessageDigest();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate().addNewSalt("newsalt").addNewPepper("newpepper").withMessageDigest();

        // THEN
        assertTrue(update.isVerified());
        Assert.assertEquals(Password.hash(PASSWORD).addPepper("newpepper").addSalt("newsalt").withMessageDigest().getResult(), update.getHash().getResult());
    }

    @Test
    public void testRawUpdate5()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withArgon2();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate().addNewSalt("newsalt").addNewPepper("newpepper").withArgon2();

        // THEN
        assertTrue(update.isVerified());
        Assert.assertEquals(Password.hash(PASSWORD).addPepper("newpepper").addSalt("newsalt").withArgon2().getResult(), update.getHash().getResult());
    }



    @Test
    public void testMigration1()
    {
        // GIVEN
        Hash oldHash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withCompressedPBKDF2();

        // WHEN
        boolean oldCheck = Password.check(PASSWORD, oldHash.getResult()).addPepper(PEPPER).withCompressedPBKDF2();
        Hash newHash = Password.hash(PASSWORD).addSalt(PEPPER).withScrypt();
        boolean newCheck = Password.check(PASSWORD, newHash.getResult()).withScrypt();


        // THEN
        assertTrue(oldCheck);
        assertTrue(newCheck);

    }

    @Test
    public void testMigration2()
    {
        // GIVEN
        Hash oldHash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withCompressedPBKDF2();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, oldHash.getResult()).addPepper(PEPPER)
                .andUpdate()
                .with(AlgorithmFinder.getCompressedPBKDF2Instance(), AlgorithmFinder.getScryptInstance());

        boolean newCheck = Password.check(PASSWORD, update.getHash().getResult()).addPepper(PEPPER).withScrypt();


        // THEN
        assertTrue(update.isVerified());
        assertTrue(newCheck);
    }

    @Test
    public void testMigration3()
    {
        // GIVEN
        Hash oldHash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withMessageDigest();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, oldHash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate()
                .with(AlgorithmFinder.getMessageDigestInstance(), AlgorithmFinder.getScryptInstance());

        boolean newCheck = Password.check(PASSWORD, update.getHash().getResult()).addPepper(PEPPER).withScrypt();


        // THEN
        assertTrue(update.isVerified());
        assertTrue(newCheck);
    }

    @Test
    public void testMigration4()
    {
        // GIVEN
        Hash oldHash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withMessageDigest();

        // WHEN
        HashUpdate update = Password.check(PASSWORD, oldHash.getResult()).addPepper(PEPPER).addSalt(SALT)
                .andUpdate()
                .with(AlgorithmFinder.getMessageDigestInstance(), AlgorithmFinder.getArgon2Instance());

        boolean newCheck = Password.check(PASSWORD, update.getHash().getResult()).addPepper(PEPPER).withArgon2();


        // THEN
        assertTrue(update.isVerified());
        assertTrue(newCheck);
    }


    @Test
    public void testRandomSalt()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addRandomSalt(12).withCompressedPBKDF2();

        // WHEN
        boolean check1 = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).withCompressedPBKDF2();


        // THEN
        assertTrue(check1);
        assertTrue(hash.getSalt() != null && hash.getSalt().length() > 0);
    }


    @Test
    public void testCustomSalt()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withPBKDF2();

        // WHEN
        boolean check1 = Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT).withPBKDF2();


        // THEN
        assertTrue(check1);
        assertTrue(hash.getSalt() != null && hash.getSalt().length() > 0);
    }


    @Test
    public void testHashingFunction()
    {
        // GIVEN


        // WHEN
        Hash hash1 = Password.hash(PASSWORD).withPBKDF2();
        Hash hash2 = Password.hash(PASSWORD).withBcrypt();
        Hash hash3 = Password.hash(PASSWORD).withScrypt();
        Hash hash4 = Password.hash(PASSWORD).withCompressedPBKDF2();
        Hash hash5 = Password.hash(PASSWORD).withMessageDigest();
        Hash hash6 = Password.hash(PASSWORD).withArgon2();



        // THEN
        assertTrue(hash1.getHashingFunction() instanceof PBKDF2Function);
        assertTrue(hash2.getHashingFunction() instanceof BcryptFunction);
        assertTrue(hash3.getHashingFunction() instanceof ScryptFunction);
        assertTrue(hash4.getHashingFunction() instanceof CompressedPBKDF2Function);
        assertTrue(hash5.getHashingFunction() instanceof MessageDigestFunction);
        assertTrue(hash6.getHashingFunction() instanceof Argon2Function);
    }

    @Test(expected = BadParametersException.class)
    public void testBad1()
    {
        Password.hash(null);
    }

    @Test(expected = BadParametersException.class)
    public void testBad3()
    {
        Password.check(null, (String)null);
    }

    @Test(expected = BadParametersException.class)
    public void testBad4()
    {
        Password.check(null, PASSWORD);
    }

    @Test(expected = BadParametersException.class)
    public void testBad5()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper(PEPPER).addSalt(SALT).withPBKDF2();

        // WHEN
        Password.check(PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT).andUpdate().addNewRandomSalt(-1).withPBKDF2();
    }

    @Test(expected = BadParametersException.class)
    public void testBad6()
    {
        Password.hash(PASSWORD).addRandomSalt(-1);
    }

    @Test(expected = BadParametersException.class)
    public void testBad7()
    {
        Password.check(null, (Hash)null);
    }

    @Test(expected = BadParametersException.class)
    public void testBad8()
    {
        Password.check(PASSWORD, new Hash(null, null, null, null));
    }

    @Test
    public void testConfigurablePepper()
    {
        // GIVEN
        Hash hash = Password.hash(PASSWORD).addPepper().withScrypt();

        // WHEN
        boolean result = Password.check(PASSWORD, hash.getResult()).addPepper().withScrypt();

        // THEN
        Assert.assertEquals(PropertyReader.readString("global.pepper", null, null), hash.getPepper());
        assertTrue(result);
    }

    @Test
    public void testSecureNeverNull()
    {
        // GIVEN
        PropertyReader.properties.put("global.random.strong", "true");

        // WHEN
        SecureRandom sr = AlgorithmFinder.getSecureRandom();

        // THEN
        Assert.assertNotNull(sr);

        PropertyReader.properties.put("global.random.strong", "false");

    }

    @Test
    public void testCoherenceSecureString()
    {
        // GIVEN

        // WHEN
        Hash hash1 = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addSalt(SALT).withPBKDF2();
        Hash hash2 = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).withBcrypt();
        Hash hash3 = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addSalt(SALT).withScrypt();

        // THEN
        assertTrue(Password.check(SECURE_PASSWORD, hash1));
        assertTrue(Password.check(SECURE_PASSWORD, hash2));
        assertTrue(Password.check(SECURE_PASSWORD, hash3));
    }


    @Test
    public void testRawCheck1SecureString()
    {
        // GIVEN
        Hash hash = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addSalt(SALT).withCompressedPBKDF2();
        String hashed = hash.getResult();

        // WHEN
        PBKDF2Function strategy = CompressedPBKDF2Function.getInstanceFromHash(hashed);

        // THEN
        assertTrue(strategy.check(Utils.append(PEPPER, SECURE_PASSWORD), hashed));
        assertTrue(Password.check(SECURE_PASSWORD, hashed).addPepper(PEPPER).withCompressedPBKDF2());
    }

    @Test
    public void testRawCheck2SecureString()
    {
        // GIVEN
        Hash hash = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).withBcrypt();
        String hashed = hash.getResult();

        // WHEN
        BcryptFunction strategy = AlgorithmFinder.getBcryptInstance();

        // THEN
        assertTrue(strategy.check(Utils.append(PEPPER, SECURE_PASSWORD), hashed));
        assertTrue(Password.check(SECURE_PASSWORD, hashed).addPepper(PEPPER).withBcrypt());
    }

    @Test
    public void testRawCheck3SecureString()
    {
        // GIVEN
        Hash hash = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addSalt(SALT).withScrypt();
        String hashed = hash.getResult();

        // WHEN
        ScryptFunction strategy = ScryptFunction.getInstanceFromHash(hashed);

        // THEN
        assertTrue(strategy.check(Utils.append(PEPPER, SECURE_PASSWORD), hashed));
        assertTrue(Password.check(SECURE_PASSWORD, hashed).addPepper(PEPPER).withScrypt());
    }

    @Test
    public void testMigrationSecureString()
    {
        // GIVEN
        Hash oldHash = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addSalt(SALT).withCompressedPBKDF2();

        // WHEN
        boolean oldCheck = Password.check(SECURE_PASSWORD, oldHash.getResult()).addPepper(PEPPER).withCompressedPBKDF2();
        Hash newHash = Password.hash(SECURE_PASSWORD).addSalt(PEPPER).withScrypt();
        boolean newCheck = Password.check(SECURE_PASSWORD, newHash.getResult()).withScrypt();


        // THEN
        assertTrue(oldCheck);
        assertTrue(newCheck);

    }

    @Test
    public void testRandomSaltSecureString()
    {
        // GIVEN
        Hash hash = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addRandomSalt(12).withCompressedPBKDF2();

        // WHEN
        boolean check1 = Password.check(SECURE_PASSWORD, hash.getResult()).addPepper(PEPPER).withCompressedPBKDF2();


        // THEN
        assertTrue(check1);
        assertTrue(hash.getSalt() != null && hash.getSalt().length() > 0);
    }


    @Test
    public void testCustomSaltSecureString()
    {
        // GIVEN
        Hash hash = Password.hash(SECURE_PASSWORD).addPepper(PEPPER).addSalt(SALT).withPBKDF2();

        // WHEN
        boolean check1 = Password.check(SECURE_PASSWORD, hash.getResult()).addPepper(PEPPER).addSalt(SALT).withPBKDF2();


        // THEN
        assertTrue(check1);
        assertTrue(hash.getSalt() != null && hash.getSalt().length() > 0);
    }


    @Test
    public void testHashingFunctionSecureString()
    {
        // GIVEN


        // WHEN
        Hash hash1 = Password.hash(SECURE_PASSWORD).withPBKDF2();
        Hash hash2 = Password.hash(SECURE_PASSWORD).withBcrypt();
        Hash hash3 = Password.hash(SECURE_PASSWORD).withScrypt();
        Hash hash4 = Password.hash(SECURE_PASSWORD).withCompressedPBKDF2();


        // THEN
        assertTrue(hash1.getHashingFunction() instanceof PBKDF2Function);
        assertTrue(hash2.getHashingFunction() instanceof BcryptFunction);
        assertTrue(hash3.getHashingFunction() instanceof ScryptFunction);
        assertTrue(hash4.getHashingFunction() instanceof CompressedPBKDF2Function);
    }



    @Test(expected = BadParametersException.class)
    public void testBad6SecureString()
    {
        Password.hash(SECURE_PASSWORD).addRandomSalt(-1);
    }


    @Test(expected = BadParametersException.class)
    public void testBad8SecureString()
    {
        Password.check(SECURE_PASSWORD, new Hash(null, null, null, null));
    }

    @Test
    public void testConfigurablePepperSecureString()
    {
        // GIVEN
        Hash hash = Password.hash(SECURE_PASSWORD).addPepper().withScrypt();

        // WHEN
        boolean result = Password.check(SECURE_PASSWORD, hash.getResult()).addPepper().withScrypt();

        // THEN
        Assert.assertEquals(PropertyReader.readString("global.pepper", null, null), hash.getPepper());
        assertTrue(result);
    }

    @Test
    public void testHashChecker()
    {
        // GIVEN
        HashChecker hc = new HashChecker(null, "hash");

        // WHEN
        boolean result = hc.with(AlgorithmFinder.getPBKDF2Instance());

        // THEN
        Assert.assertFalse(result);
    }

    @Test
    public void testHmac()
    {

        for(Hmac hmac : Hmac.values())
        {
            Assert.assertEquals("PBKDF2WithHmac" + hmac.name(), hmac.toString());
        }

    }

    @Test(expected = BadParametersException.class)
    public void testBadUpdate1()
    {
        new HashUpdater(null, null).with(AlgorithmFinder.getCompressedPBKDF2Instance(), null);
    }

    @Test(expected = BadParametersException.class)
    public void testBadUpdate2()
    {
        new HashUpdater(null, null).with(null, AlgorithmFinder.getCompressedPBKDF2Instance());
    }

    @Test
    public void testGenericUpdate1()
    {
        String password = "password";
        String salt = "salt";
        String pepper = "pepper";
        String prefix = "new";

        Hash hash = Password.hash(password).addSalt(salt).addPepper(pepper).withCompressedPBKDF2();

        HashUpdate update = Password.check(password, hash.getResult())
                .addSalt(salt)
                .addPepper(pepper)
                .andUpdate().addNewSalt(prefix + salt).addNewPepper(prefix + salt).withCompressedPBKDF2();

        assertTrue(update.isVerified());
        Assert.assertEquals(salt, hash.getSalt());
        Assert.assertEquals(pepper, hash.getPepper());
        Assert.assertEquals(prefix + salt, update.getHash().getSalt());
        Assert.assertEquals(prefix + salt, update.getHash().getSalt());

    }

    @Test
    public void testGenericUpdate2()
    {
        String password = "password";

        Hash hash = Password.hash(password).withCompressedPBKDF2();

        HashUpdate update = Password.check(password, hash.getResult())
                .andUpdate().addNewSalt(hash.getSalt()).withCompressedPBKDF2();

        assertTrue(update.isVerified());
        Assert.assertEquals(hash.getSalt(), update.getHash().getSalt());
        Assert.assertEquals(hash.getPepper(), update.getHash().getPepper());

    }


    @Test
    public void testGenericUpdate3()
    {
        String password = "password";

        Hash hash = Password.hash(password).withPBKDF2();

        HashUpdate update = Password.check(password, "hash").addSalt("salt")
                .andUpdate().addNewSalt(hash.getSalt()).withPBKDF2();

        Assert.assertFalse(update.isVerified());
        Assert.assertNotNull(update);
        Assert.assertNull(update.getHash());
    }

    @Test
    public void testGenericUpdate4()
    {
        String password = "password";

        try {
            Hash hash = Password.hash(password).withPBKDF2();

            HashUpdate update = Password.check(password, "hash")
                    .andUpdate().addNewSalt(hash.getSalt()).withPBKDF2();
            Assert.assertFalse(update.isVerified());
            Assert.assertNotNull(update);
            Assert.assertNull(update.getHash());
        } catch (Exception ex) {
            assertTrue(ex instanceof BadParametersException);
        }
    }


    @Test
    public void testGenericUpdate5()
    {
        String password = "password";

        Hash hash = Password.hash(password).withCompressedPBKDF2();

        HashUpdate updateSalt = Password.check(password, hash.getResult())
                .andUpdate().addNewRandomSalt().withCompressedPBKDF2();
        HashUpdate updateFixedSalt = Password.check(password, hash.getResult())
                .andUpdate().addNewRandomSalt(11).withCompressedPBKDF2();

        HashUpdate updateFixedSaltPepper = Password.check(password, hash.getResult())
                .andUpdate().addNewRandomSalt(11).addNewPepper().withCompressedPBKDF2();


        assertTrue(updateSalt.isVerified() && updateFixedSalt.isVerified() && updateFixedSaltPepper.isVerified());
        assertTrue(updateSalt.getHash().getPepper() == null && updateFixedSalt.getHash().getPepper() == null);
        assertTrue(updateSalt.getHash().getSalt() != null && updateFixedSalt.getHash().getSalt() != null && updateFixedSaltPepper.getHash().getSalt() != null);
        Assert.assertEquals(PropertyReader.readString("global.pepper", null, null), updateFixedSaltPepper.getHash().getPepper());

    }

    @Test
    public void testBcryptNonStandardParams()
    {
        final String testHash = "$2b$16$.1FczuSNl2iXHmLojhwBZO9vCfA5HIqrONkefhvn2qLQpth3r7Jwe";
        assertTrue(Password.check("s$cret12", testHash).with(BcryptFunction.getInstanceFromHash(testHash)));
    }

    @Test
    public void testScryptNonStandardParams()
    {
        /*
         * This password hash was generated using com.lambdaworks:scrypt, which has a derived key length (dkLen) of 32 bytes.
         */
        final String testHash = "$e0801$fl+gNAicpGG4gLMkUTCvLw==$N5wE1IKsr4LPBoetJVW6jLzEH4kTVXuKGafvAA8Z+88=";
        assertTrue(Password.check("Hello world!", testHash).with(ScryptFunction.getInstanceFromHash(testHash)));
    }

    @Test
    public void testArgon2NonstandardParams()
    {
        /*
         * This password hash comes from the Argon2 C reference implementation (https://github.com/P-H-C/phc-winner-argon2).
         */
        final String testHash = "$argon2i$v=19$m=65536,t=2,p=4$c29tZXNhbHQ$RdescudvJCsgt3ub+b+dWRWJTmaaJObG";
        assertTrue(Password.check("password", testHash).with(Argon2Function.getInstanceFromHash(testHash)));
    }


    @Test
    public void testRandomPasswords()
    {
        int c = 1;
        int max = 50;
        PBKDF2Function pbkdf2 = PBKDF2Function.getInstance(Hmac.SHA1, 3, 128);
        BcryptFunction bcrypt = BcryptFunction.getInstance(10);
        ScryptFunction scrypt = ScryptFunction.getInstance(16384, 8, 1);
        Argon2Function argon2 = Argon2Function.getInstance(1024, 3, 1, 32, Argon2.ID);

        HashingFunction[] functions = new HashingFunction[]{pbkdf2, bcrypt, scrypt, argon2};

        for(int i = 0; i < max; i++, c++)
        {
            int length = RANDOM.nextInt(c);
            String password = Utils.randomPrintable(length);
            String salt = Utils.randomPrintable(32);
            String pepper =Utils.randomPrintable(16);

            for(HashingFunction function : functions)
            {
                Hash hash;
                if(function instanceof BcryptFunction)
                {
                    hash = Password.hash(password).addPepper(pepper).with(function);
                }
                else
                {
                    hash = Password.hash(password).addSalt(salt).addPepper(pepper).with(function);
                }

                String message = String.format("[%d/%d] %s: Failed\nPASSWORD(%d) %s\nSALT(%d): %s\nPEPPER(%d): %s\n\n%s",
                        c, max, function, password.length(), password, salt.length(), salt, pepper.length(), pepper, hash.getResult());
                assertTrue(message, Password.check(password, hash));
            }
        }
    }

    @Test
    public void real()
    {
        BcryptFunction bcrypt = BcryptFunction.getInstance(Bcrypt.B, 12);

        boolean verified = Password.check("my password", "$2b$12$.z6oEtf4KGlPk9y4uzEsKuF.4MfAv9NQCrqXQevjYy0DMvVXZWcK2")
                .addPepper("shared-secret")
                .with(bcrypt);

        Assert.assertTrue(verified);
    }

    @Test
    public void testBanner()
    {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

        PropertyReader.properties.setProperty("global.banner", "false");

        Utils.printBanner(new PrintStream(outputStreamCaptor));
        Assert.assertEquals(0, outputStreamCaptor.toString().length());

        PropertyReader.properties.setProperty("global.banner", "true");

    }

    @Test
    public void testBanner2()
    {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

        Provider[] oldProviders = Security.getProviders();
        for (Provider provider : oldProviders)
        {
            for (Provider.Service service : provider.getServices())
            {
                if ("SecretKeyFactory".equals(service.getType()) && service.getAlgorithm().startsWith("PBKDF2"))
                {
                    Security.removeProvider(provider.getName());
                }
            }
        }

        Utils.printBanner(new PrintStream(outputStreamCaptor));
        Assert.assertTrue(outputStreamCaptor.toString().indexOf("❌") > 0);
        Assert.assertTrue(outputStreamCaptor.toString().indexOf(System.getProperty("java.vm.name")) > 0);

        for (Provider provider : oldProviders)
        {
            if (Security.getProvider(provider.getName()) == null)
            {
                Security.addProvider(provider);
            }
        }

    }

    @Test
    public void testMultiUnicode()
    {
        String multiUnicode = "(っ＾▿＾)۶\uD83C\uDF78\uD83C\uDF1F\uD83C\uDF7A٩(˘◡˘ ) ❌❌ ❌❌❌";
        String salt = "(っ＾▿＾)\uD83D\uDCA8 ❌❌ ❌❌❌";
        String pepper = "O̲ppa̲ (っ-̶●̃益●̶̃)っ ,︵‿ S̲t̲yl̲e̲  (͠≖ ͜ʖ͠≖)\uD83D\uDC4C ❌❌ ❌❌❌";

        Hash hash = Password.hash(multiUnicode).addSalt(salt).addPepper(pepper).withArgon2();
        Hash hash2 = Password.hash(multiUnicode).addSalt(salt).addPepper(pepper).withArgon2();

        Assert.assertTrue(Password.check(multiUnicode, hash));
        Assert.assertEquals(hash, hash2);
    }


}
