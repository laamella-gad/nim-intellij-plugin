package com.laamella.nim

import com.google.gson.JsonParser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.*
import com.intellij.workspaceModel.ide.legacyBridge.LegacyBridgeJpsEntitySourceFactory
import com.laamella.nim.settings.NimSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private data class NimDep(val name: String, val installUrl: String)

fun configureNimLibraries(project: Project) {
    val settings = NimSettings.getInstance()
    val projectDir = project.guessProjectDir()?.path ?: return

    ApplicationManager.getApplication().executeOnPooledThread {
        val deps = runNimbleDeps(settings, projectDir) ?: return@executeOnPooledThread

        ApplicationManager.getApplication().invokeLater {
            val workspaceModel = project.workspaceModel
            val urlManager = workspaceModel.getVirtualFileUrlManager()
            val entitySource = LegacyBridgeJpsEntitySourceFactory.getInstance(project)
                .createEntitySourceForProjectLibrary(null)
            val nimblePkgsDir = nimblePkgs2Dir(settings)
            val nimblePkgsDirUrl = VfsUtilCore.pathToUrl(nimblePkgsDir.toString())

            ApplicationManager.getApplication().runWriteAction {
                workspaceModel.updateProjectModel("Configure Nim libraries") { builder ->
                    val depNames = deps.map { it.name }.toSet()
                    val module = builder.entities(ModuleEntity::class.java).firstOrNull()

                    // Remove project libs that point to nimble pkgs2 but are no longer in deps
                    val toRemove = builder.entities(LibraryEntity::class.java)
                        .filter { it.tableId == LibraryTableId.ProjectLibraryTableId }
                        .filter { lib -> lib.roots.any { it.url.url.startsWith(nimblePkgsDirUrl) } }
                        .filter { it.name !in depNames }
                        .toList()

                    if (module != null && toRemove.isNotEmpty()) {
                        val removeIds = toRemove.map { it.symbolicId }.toSet()
                        builder.modifyModuleEntity(module) {
                            dependencies.removeAll { it is LibraryDependency && it.library in removeIds }
                        }
                    }
                    toRemove.forEach { builder.removeEntity(it) }

                    // Add or update each dep
                    for (dep in deps) {
                        val libId = LibraryId(dep.name, LibraryTableId.ProjectLibraryTableId)
                        val depUrl = urlManager.getOrCreateFromUrl(dep.installUrl)
                        val roots = listOf(
                            LibraryRoot(depUrl, LibraryRootTypeId.COMPILED),
                            LibraryRoot(depUrl, LibraryRootTypeId.SOURCES),
                        )
                        val existing = builder.resolve(libId)
                        if (existing == null) {
                            builder.addEntity(LibraryEntity(dep.name, LibraryTableId.ProjectLibraryTableId, roots, entitySource))
                        } else {
                            builder.modifyLibraryEntity(existing) {
                                this.roots.clear()
                                this.roots.addAll(roots)
                            }
                        }

                        if (module != null) {
                            val libDep = LibraryDependency(libId, false, DependencyScope.COMPILE)
                            if (module.dependencies.none { it is LibraryDependency && it.library == libId }) {
                                builder.modifyModuleEntity(module) {
                                    dependencies.add(libDep)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun runNimbleDeps(settings: NimSettings, projectDir: String): List<NimDep>? {
    val pb = ProcessBuilder(settings.nimble(), "deps", "--format:json")
        .directory(File(projectDir))
        .redirectError(ProcessBuilder.Redirect.DISCARD)
    if (settings.nimbleBinPath.isNotBlank()) {
        val currentPath = System.getenv("PATH") ?: ""
        pb.environment()["PATH"] = "${settings.nimbleBinPath}${File.pathSeparator}$currentPath"
    }
    val process = pb.start()
    val output = process.inputStream.bufferedReader().readText()
    if (process.waitFor() != 0) return null
    val jsonStart = output.indexOf('[')
    if (jsonStart < 0) return null
    return parseNimbleDeps(output.substring(jsonStart), settings)
}

private fun parseNimbleDeps(json: String, settings: NimSettings): List<NimDep> {
    val pkgs2Dir = nimblePkgs2Dir(settings)
    if (!Files.isDirectory(pkgs2Dir)) return emptyList()

    val result = mutableListOf<NimDep>()
    val array = JsonParser.parseString(json).asJsonArray
    for (el in array) {
        val obj = el.asJsonObject
        val name = obj.get("name").asString
        val resolvedTo = obj.get("resolvedTo").asString
        if (resolvedTo.isEmpty() || name == "nim") continue
        val dir = Files.newDirectoryStream(pkgs2Dir, "$name-$resolvedTo-*").use { it.firstOrNull() } ?: continue
        result += NimDep(name, VfsUtilCore.pathToUrl(dir.toAbsolutePath().toString()))
    }
    return result
}

private fun nimblePkgs2Dir(settings: NimSettings): Path =
    if (settings.nimbleBinPath.isNotBlank())
        Path.of(settings.nimbleBinPath).parent.resolve("pkgs2")
    else
        Path.of(System.getProperty("user.home"), ".nimble", "pkgs2")
