package com.laamella.nim

import com.intellij.lang.Commenter

class NimCommenter : Commenter {
    override fun getLineCommentPrefix() = "#"
    override fun getBlockCommentPrefix() = "#["
    override fun getBlockCommentSuffix() = "]#"
    override fun getCommentedBlockCommentPrefix() = null
    override fun getCommentedBlockCommentSuffix() = null
}
