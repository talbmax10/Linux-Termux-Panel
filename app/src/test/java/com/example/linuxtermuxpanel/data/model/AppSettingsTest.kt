package com.example.linuxtermuxpanel.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {

    @Test
    fun `default paths follow the termux package name`() {
        val settings = AppSettings()

        assertEquals("/data/data/com.termux/files/usr", settings.termuxPrefixPath)
        assertEquals("/data/data/com.termux/files/home", settings.termuxHomePath)
    }

    @Test
    fun `paths follow a custom termux package name`() {
        val settings = AppSettings(termuxPackageName = "com.termux.fdroid")

        assertEquals("/data/data/com.termux.fdroid/files/usr", settings.termuxPrefixPath)
        assertEquals("/data/data/com.termux.fdroid/files/home", settings.termuxHomePath)
    }

    @Test
    fun `supported environments are termux and ubuntu`() {
        assertEquals(listOf("Termux", "Ubuntu"), Environments.all)
    }
}
