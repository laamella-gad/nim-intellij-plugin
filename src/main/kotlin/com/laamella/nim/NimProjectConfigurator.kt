package com.laamella.nim

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.LocalFileSystem

class NimProjectConfigurator : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        val basePath = project.basePath ?: return
        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return
        val nimbleFile = baseDir.children.find { it.extension == "nimble" } ?: return
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
        val content = nimbleMap.entries.joinToString("<br>") { (k, v) -> "$k = $v" }
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Nim")
            .createNotification("Nim project configured from ${nimbleFile.name}", content, NotificationType.INFORMATION)
            .notify(project)
    }
}
