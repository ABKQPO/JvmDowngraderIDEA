package com.hfstudio.jvmdowngraderidea

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

internal object JvmDowngraderBundle : DynamicBundle("messages.JvmDowngraderBundle") {
    private const val BUNDLE = "messages.JvmDowngraderBundle"

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
