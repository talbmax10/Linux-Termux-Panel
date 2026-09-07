package com.example.linuxtermuxpanel.execution

/**
 * نتيجة تنفيذ أمر.
 */
data class ExecutionResult(
    val output: String = "",
    val error: String = "",
    val exitCode: Int = -1
) {
    val success: Boolean
        get() = exitCode == 0
}
