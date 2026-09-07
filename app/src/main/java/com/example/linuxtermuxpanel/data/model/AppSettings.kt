package com.example.linuxtermuxpanel.data.model

/**
 * إعدادات التطبيق القابلة للحفظ في DataStore.
 */
data class AppSettings(
    val termuxPackageName: String = DEFAULT_TERMUX_PACKAGE,
    val ubuntuLoginCommand: String = DEFAULT_UBUNTU_LOGIN,
    val autoWrapUbuntuCommands: Boolean = true,
    val timeoutSeconds: Int = 30
) {
    /** مسار مجلد usr داخل Termux (يعتمد على اسم الحزمة). */
    val termuxPrefixPath: String
        get() = "/data/data/$termuxPackageName/files/usr"

    /** مسار مجلد المنزل داخل Termux. */
    val termuxHomePath: String
        get() = "/data/data/$termuxPackageName/files/home"

    companion object {
        const val DEFAULT_TERMUX_PACKAGE = "com.termux"
        const val DEFAULT_UBUNTU_LOGIN = "proot-distro login ubuntu"
    }
}

/** البيئات المدعومة لتنفيذ الأوامر. */
object Environments {
    const val TERMUX = "Termux"
    const val UBUNTU = "Ubuntu"

    val all: List<String> = listOf(TERMUX, UBUNTU)
}
