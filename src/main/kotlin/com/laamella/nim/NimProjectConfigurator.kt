package com.laamella.nim

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

enum class NimDirKind { SRC, TEST, EXCLUDE }

/**
 * Evaluates the Nim project's .nimble project file on project startup
 * and whenever the .nimble file is created or modified.
 */
class NimProjectConfigurator : ProjectActivity {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater { configureNimProject(project) }
    }
}

/** Reacts to .nimble file changes and re-runs project configuration. Registered via plugin.xml projectListeners. */
class NimNimbleFileListener(private val project: Project) : BulkFileListener {
    override fun after(events: List<VFileEvent>) {
        val projectDir = project.guessProjectDir() ?: return
        val affected = events.any { e -> e.file?.parent?.url == projectDir.url && e.file?.extension == "nimble" }
        if (affected) ApplicationManager.getApplication().invokeLater { configureNimProject(project) }
    }
}

fun configureNimProject(project: Project) {
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
    // Nim projects are single-module by convention; create one if the project has none (e.g. new project wizard)
    val module = ModuleManager.getInstance(project).modules.firstOrNull()
        ?: ApplicationManager.getApplication().runWriteAction(Computable {
            ModuleManager.getInstance(project).newModule(
                "${project.basePath}/${project.name}.iml",
                ModuleTypeManager.getInstance().defaultModuleType.id
            )
        })

    fun findOrCreateDir(key: String, nimbleConfig: Map<String, String>) =
        ApplicationManager.getApplication().runWriteAction(Computable {
            val dirName = nimbleConfig[key] ?: return@Computable null
            projectDir.findChild(dirName) ?: projectDir.createChildDirectory(null, dirName)
        })

    fun ContentEntry.markAs(dir: VirtualFile, kind: NimDirKind) {
        when (kind) {
            NimDirKind.SRC ->
                if (sourceFolders.none { it.url == dir.url }) addSourceFolder(dir, false)
            NimDirKind.TEST ->
                if (sourceFolders.none { it.url == dir.url }) addSourceFolder(dir, true)
            NimDirKind.EXCLUDE ->
                if (excludeFolderUrls.none { it == dir.url }) addExcludeFolder(dir)
        }
    }

    ModuleRootModificationUtil.updateModel(module) { model ->
        val contentEntry = model.contentEntries.find { it.url == projectDir.url }
            ?: model.addContentEntry(projectDir)
        findOrCreateDir("srcDir", nimbleMap)?.let { contentEntry.markAs(it, NimDirKind.SRC) }
        findOrCreateDir("binDir", nimbleMap)?.let { contentEntry.markAs(it, NimDirKind.EXCLUDE) }
        projectDir.findChild("tests")?.let { contentEntry.markAs(it, NimDirKind.TEST) }
    }

    configureNimLibraries(project)
    configureNimStdlib(project)

    NotificationGroupManager.getInstance()
        .getNotificationGroup("Nim")
        .createNotification("Nimble project refreshed", NotificationType.INFORMATION)
        .notify(project)
}
