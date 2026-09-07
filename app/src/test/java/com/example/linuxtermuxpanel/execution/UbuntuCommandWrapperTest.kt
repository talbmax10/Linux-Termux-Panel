package com.example.linuxtermuxpanel.execution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UbuntuCommandWrapperTest {

    @Test
    fun `wraps command inside proot-distro login`() {
        val wrapper = UbuntuCommandWrapper("proot-distro login ubuntu")

        val result = wrapper.wrap("ls -la")

        assertEquals("proot-distro login ubuntu -- bash -lc 'ls -la'", result)
    }

    @Test
    fun `escapes single quotes so the command does not break`() {
        val wrapper = UbuntuCommandWrapper("proot-distro login ubuntu")

        val result = wrapper.wrap("echo 'hello world'")

        assertEquals(
            "proot-distro login ubuntu -- bash -lc 'echo '\\''hello world'\\'''",
            result
        )
    }

    @Test
    fun `falls back to default login command when blank`() {
        val wrapper = UbuntuCommandWrapper("   ")

        val result = wrapper.wrap("uname -a")

        assertTrue(result.startsWith(UbuntuCommandWrapper.DEFAULT_LOGIN))
    }
}
