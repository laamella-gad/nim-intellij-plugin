package com.laamella.nim.projectconfig

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile

fun configureNimDirectories(project: Project, module: Module) {
    val projectDir = project.guessProjectDir() ?: return
    val nimbleFile = projectDir.children.find { it.extension == "nimble" } ?: return
    val nimble = String(nimbleFile.contentsToByteArray())
    // Nimble files are Nim source, not a standard config format — hand-parse the simple key = "value" assignments
    val nimbleMap = buildMap {
        for (line in nimble.lines()) {
            val eqIdx = line.indexOf('=')
            if (eqIdx < 0) continue
            val key = line.substring(0, eqIdx).trim()
            val rawValue = line.substring(eqIdx + 1).trim()
            val value = Regex("""["']([^"']+)["']""").find(rawValue)?.groupValues?.get(1) ?: rawValue
            if (key.isNotEmpty()) put(key, value)
        }
    }

    ModuleRootModificationUtil.updateModel(module) { model ->
        val contentEntry = model.contentEntries.find { it.url == projectDir.url }
            ?: model.addContentEntry(projectDir)
        findOrCreateDir(projectDir, "srcDir", nimbleMap)?.let { contentEntry.markAs(it, NimDirKind.SRC) }
        findOrCreateDir(projectDir, "binDir", nimbleMap)?.let { contentEntry.markAs(it, NimDirKind.EXCLUDE) }
        projectDir.findChild("tests")?.let { contentEntry.markAs(it, NimDirKind.TEST) }
    }
}

private fun findOrCreateDir(projectDir: VirtualFile, key: String, nimbleConfig: Map<String, String>) =
    ApplicationManager.getApplication().runWriteAction(Computable {
        val dirName = nimbleConfig[key] ?: return@Computable null
        projectDir.findChild(dirName) ?: projectDir.createChildDirectory(null, dirName)
    })

private fun ContentEntry.markAs(dir: VirtualFile, kind: NimDirKind) {
    when (kind) {
        NimDirKind.SRC ->
            if (sourceFolders.none { it.url == dir.url }) addSourceFolder(dir, false)

        NimDirKind.TEST ->
            if (sourceFolders.none { it.url == dir.url }) addSourceFolder(dir, true)

        NimDirKind.EXCLUDE ->
            if (excludeFolderUrls.none { it == dir.url }) addExcludeFolder(dir)
    }
}

