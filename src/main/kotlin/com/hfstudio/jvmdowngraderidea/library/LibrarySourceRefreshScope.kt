package com.hfstudio.jvmdowngraderidea.library

internal object LibrarySourceRefreshScope {
    fun shouldUpdate(extension: String?, isLibrarySource: Boolean): Boolean =
        extension == "java" && isLibrarySource
}
