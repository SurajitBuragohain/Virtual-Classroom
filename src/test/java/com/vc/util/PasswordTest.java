package com.vc.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordTest {

    @Test
    void hashAndVerifyCorrectPassword() {
        String password = "Test@12345";
        String hash = Password.hash(password);

        assertNotNull(hash);
        assertTrue(hash.contains(":"));
        assertTrue(Password.verify(password, hash));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String hash = Password.hash("Test@12345");

        assertFalse(Password.verify("Wrong@12345", hash));
    }

    @Test
    void samePasswordGetsDifferentSaltedHashes() {
        String first = Password.hash("Test@12345");
        String second = Password.hash("Test@12345");

        assertNotEquals(first, second);
        assertTrue(Password.verify("Test@12345", first));
        assertTrue(Password.verify("Test@12345", second));
    }

    @Test
    void invalidStoredHashIsRejected() {
        assertFalse(Password.verify("Test@12345", "not-a-valid-hash"));
        assertFalse(Password.verify(null, "anything"));
        assertFalse(Password.verify("Test@12345", null));
    }
}
