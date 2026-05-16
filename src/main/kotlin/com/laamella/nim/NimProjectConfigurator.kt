package com.laamella.nim

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.startup.ProjectActivity

/**
 * Evaluates the Nim project's .nimble project file on project startup.
 * TODO also run when the nimble file changes or is created (or deleted)
 */
class NimProjectConfigurator : ProjectActivity {
    override suspend fun execute(project: Project) {
        val projectDir = project.guessProjectDir() ?: return
        // TODO show notification if this fails
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
        // Fall back to projectDir when srcDir is absent or the directory doesn't exist yet
        val srcRoot = projectDir.findChild(nimbleMap["srcDir"] ?: "src") ?: projectDir
        val binRoot = nimbleMap["binDir"]?.let { projectDir.findChild(it) }
        // TODO show notification if this fails
        // Nim projects are single-module by convention
        val module = ModuleManager.getInstance(project).modules.firstOrNull() ?: return
        ModuleRootModificationUtil.updateModel(module) { model ->
            // Guard against duplicates on repeated project opens
            val contentEntry = model.contentEntries.find { it.url == projectDir.url }
                ?: model.addContentEntry(projectDir)
            if (contentEntry.sourceFolders.none { it.url == srcRoot.url })
                contentEntry.addSourceFolder(srcRoot, false)
            if (binRoot != null && contentEntry.excludeFolderUrls.none { it == binRoot.url })
                contentEntry.addExcludeFolder(binRoot)
        }
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Nim")
            .createNotification("Nimble project refreshed", NotificationType.INFORMATION)
            .notify(project)
    }
}
