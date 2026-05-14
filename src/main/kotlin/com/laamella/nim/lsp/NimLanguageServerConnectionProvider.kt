package com.laamella.nim.lsp

import com.intellij.openapi.project.Project
import com.laamella.nim.settings.NimSettings
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider
import java.io.ByteArrayOutputStream
import java.io.FilterOutputStream
import java.io.OutputStream

class NimLanguageServerConnectionProvider(project: Project) : ProcessStreamConnectionProvider() {
    init {
        val path = NimSettings.getInstance().serverPath.ifBlank { "nimlangserver" }
        commands = listOf(path, "--stdio")
        workingDirectory = project.basePath
    }

    override fun getOutputStream(): OutputStream = LspIdOutputStream(super.getOutputStream()!!)
}

/**
 * nimlangserver only echoes "id" in responses when the request id was a JSON number.
 * lsp4j sends string ids ("1", "2", ...). This stream converts outgoing string ids to
 * numbers (1, 2, ...) so nimlangserver includes the id in its response and lsp4j can
 * match the response to the pending request.
 */
private class LspIdOutputStream(out: OutputStream) : FilterOutputStream(out) {
    private val buf = ByteArrayOutputStream()

    override fun write(b: Int) { buf.write(b); drain() }
    override fun write(b: ByteArray, off: Int, len: Int) { buf.write(b, off, len); drain() }
    override fun flush() = out.flush()

    private fun drain() {
        while (true) {
            val data = buf.toByteArray()
            val text = String(data, Charsets.UTF_8)
            val sep = text.indexOf("\r\n\r\n").takeIf { it >= 0 } ?: return
            val contentLength = text.substring(0, sep).lineSequence()
                .find { it.startsWith("Content-Length:", ignoreCase = true) }
                ?.substringAfter(":")?.trim()?.toIntOrNull() ?: return
            val bodyStart = sep + 4
            val bodyEnd = bodyStart + contentLength
            if (data.size < bodyEnd) return

            val body = text.substring(bodyStart, bodyEnd)
            val fixed = body.replace(Regex(""""id"\s*:\s*"(\d+)"""")) { """"id":${it.groupValues[1]}""" }
            val fixedBytes = fixed.toByteArray(Charsets.UTF_8)

            out.write("Content-Length: ${fixedBytes.size}\r\n\r\n".toByteArray(Charsets.UTF_8))
            out.write(fixedBytes)

            buf.reset()
            if (data.size > bodyEnd) buf.write(data, bodyEnd, data.size - bodyEnd)
        }
    }
}
