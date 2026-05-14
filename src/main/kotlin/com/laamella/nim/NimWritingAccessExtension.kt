package com.laamella.nim

import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.vfs.VirtualFile

class NimWritingAccessExtension : NonProjectFileWritingAccessExtension {
    override fun isWritable(file: VirtualFile) = file.fileType == NimFileType
}
