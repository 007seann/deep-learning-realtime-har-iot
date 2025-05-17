package com.specknet.pdiotapp

import com.specknet.pdiotapp.utils.HashedPasswordGenerator
import org.junit.Test

class HashedPasswordGeneratorTest {

    @Test
    fun generateRandomSaltTest() {
        val pair1 = HashedPasswordGenerator.hashPassword("test")
        val pair2 = HashedPasswordGenerator.hashPassword("password")
        assert(HashedPasswordGenerator.verifyPassword("test", pair1.second))
        assert(HashedPasswordGenerator.verifyPassword("password", pair2.second))
    }

}