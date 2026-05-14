package com.laamella.nim

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator

class NimProjectConfigurator : DirectoryProjectConfigurator {
    override fun configureProject(
        project: Project,
        baseDir: VirtualFile,
        moduleRef: Ref<Module>,
        isProjectCreatedWithWizard: Boolean
    ) {
        val nimbleFile = baseDir.children.find { it.extension == "nimble" } ?: return
        val nimble = String(nimbleFile.contentsToByteArray())
        val srcRoot = baseDir.findChild(parseDir(nimble, "srcDir") ?: "src") ?: baseDir
        val binRoot = parseDir(nimble, "binDir")?.let { baseDir.findChild(it) }
        val module = moduleRef.get() ?: return
        ModuleRootModificationUtil.updateModel(module) { model ->
            val contentEntry = model.contentEntries.find { it.url == baseDir.url }
                ?: model.addContentEntry(baseDir)
            if (contentEntry.sourceFolders.none { it.url == srcRoot.url })
                contentEntry.addSourceFolder(srcRoot, false)
            if (binRoot != null && contentEntry.excludeFolderUrls.none { it == binRoot.url })
                contentEntry.addExcludeFolder(binRoot)
        }
    }

    private fun parseDir(nimble: String, key: String): String? =
        Regex("""^\s*$key\s*=\s*["']([^"']+)["']""", RegexOption.MULTILINE)
            .find(nimble)?.groupValues?.get(1)
}
