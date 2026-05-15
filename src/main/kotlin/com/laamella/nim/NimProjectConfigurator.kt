package com.laamella.nim

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.LocalFileSystem

class NimProjectConfigurator : StartupActivity.DumbAware {
    private val log = Logger.getInstance(NimProjectConfigurator::class.java)

    override fun runActivity(project: Project) {
        log.info("NimProjectConfigurator fired for ${project.name}")
        val basePath = project.basePath ?: return
        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return
        val nimbleFile = baseDir.children.find { it.extension == "nimble" }
        if (nimbleFile == null) {
            log.info("No .nimble file found in $basePath")
            return
        }
        val nimble = String(nimbleFile.contentsToByteArray())
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
        log.info("Nim project config (${nimbleFile.name}): $nimbleMap")
        val srcRoot = baseDir.findChild(nimbleMap["srcDir"] ?: "src") ?: baseDir
        val binRoot = nimbleMap["binDir"]?.let { baseDir.findChild(it) }
        val module = ModuleManager.getInstance(project).modules.firstOrNull() ?: return
        ModuleRootModificationUtil.updateModel(module) { model ->
            val contentEntry = model.contentEntries.find { it.url == baseDir.url }
                ?: model.addContentEntry(baseDir)
            if (contentEntry.sourceFolders.none { it.url == srcRoot.url })
                contentEntry.addSourceFolder(srcRoot, false)
            if (binRoot != null && contentEntry.excludeFolderUrls.none { it == binRoot.url })
                contentEntry.addExcludeFolder(binRoot)
        }
    }
}
