package com.example.linuxtermuxpanel.execution

/**
 * يلفّ الأمر لتشغيله داخل Ubuntu (proot-distro) من داخل Termux.
 *
 * النتيجة تكون بالشكل:
 *   proot-distro login ubuntu -- bash -lc '<الأمر>'
 */
class UbuntuCommandWrapper(
    private val ubuntuLoginCommand: String
) {
    fun wrap(command: String): String {
        val login = ubuntuLoginCommand.trim().ifBlank { DEFAULT_LOGIN }
        // تهريب علامات الاقتباس المفردة حتى لا ينكسر الأمر داخل bash -lc '...'
        val escapedCommand = command.replace("'", "'\\''")
        return "$login -- bash -lc '$escapedCommand'"
    }

    companion object {
        const val DEFAULT_LOGIN = "proot-distro login ubuntu"
    }
}
