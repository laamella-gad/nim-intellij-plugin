package com.laamella.nim.projectconfig

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

internal data class NimDep(val name: String, val installUrl: String)

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
            val nimblePkgsDir = nimblePkgs2Dir(settings.nimbleBinPath)
            val nimblePkgsDirUrl = VfsUtilCore.pathToUrl(nimblePkgsDir.toString())

            ApplicationManager.getApplication().runWriteAction {
                workspaceModel.updateProjectModel("Configure Nim libraries") { builder ->
                    val depNames = deps.map { it.name }.toSet()
                    val module = builder.entities(ModuleEntity::class.java).firstOrNull()

                    // Remove project libs that point to nimble pkgs2 but are no longer in deps
                    val toRemove = builder.entities(LibraryEntity::class.java)
                        .filter { it.tableId == LibraryTableId.ProjectLibraryTableId }
                        .filter { lib -> lib.roots.any { it.url.url.startsWith(nimblePkgsDirUrl) } }
                        .filter { it.name !in depNames && it.name != "Nim" && !it.name.startsWith("Nim ") }
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
    val json = extractJsonArray(output) ?: return null
    return parseNimbleDeps(json, nimblePkgs2Dir(settings.nimbleBinPath))
}

/**
 * `nimble deps --format:json` sometimes prefixes its JSON array with plain-text log lines
 * (e.g. "[Warning]: no lockfile found, computing dependencies"), whose stray brackets confuse a
 * naive `indexOf('[')`. Scan for every top-level, bracket-balanced `[...]` span (respecting
 * quoted strings) and return the last one, since the real payload is printed last.
 */
internal fun extractJsonArray(output: String): String? {
    var lastMatch: String? = null
    var i = 0
    while (i < output.length) {
        if (output[i] == '[') {
            val end = matchingBracketIndex(output, i)
            if (end != null) {
                lastMatch = output.substring(i, end + 1)
                i = end + 1
                continue
            }
        }
        i++
    }
    return lastMatch
}

private fun matchingBracketIndex(s: String, start: Int): Int? {
    var depth = 0
    var inString = false
    var escape = false
    for (i in start until s.length) {
        val c = s[i]
        if (inString) {
            when {
                escape -> escape = false
                c == '\\' -> escape = true
                c == '"' -> inString = false
            }
        } else {
            when (c) {
                '"' -> inString = true
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
    }
    return null
}

internal fun parseNimbleDeps(json: String, pkgs2Dir: Path): List<NimDep> {
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

fun configureNimStdlib(project: Project) {
    val settings = NimSettings.getInstance()
    ApplicationManager.getApplication().executeOnPooledThread {
        val (stdlibDir, version) = findNimStdlibDir(settings) ?: return@executeOnPooledThread
        val libName = "Nim $version"

        ApplicationManager.getApplication().invokeLater {
            val workspaceModel = project.workspaceModel
            val urlManager = workspaceModel.getVirtualFileUrlManager()
            val entitySource = LegacyBridgeJpsEntitySourceFactory.getInstance(project)
                .createEntitySourceForProjectLibrary(null)

            ApplicationManager.getApplication().runWriteAction {
                workspaceModel.updateProjectModel("Configure Nim stdlib") { builder ->
                    val libId = LibraryId(libName, LibraryTableId.ProjectLibraryTableId)
                    val stdlibUrl = urlManager.getOrCreateFromUrl(VfsUtilCore.pathToUrl(stdlibDir.toString()))
                    val roots = listOf(
                        LibraryRoot(stdlibUrl, LibraryRootTypeId.COMPILED),
                        LibraryRoot(stdlibUrl, LibraryRootTypeId.SOURCES),
                    )

                    // Remove stale "Nim <old-version>" libs left behind by a toolchain upgrade
                    val staleNimLibs = builder.entities(LibraryEntity::class.java)
                        .filter { it.tableId == LibraryTableId.ProjectLibraryTableId }
                        .filter { it.name == "Nim" || it.name.startsWith("Nim ") }
                        .filter { it.name != libName }
                        .toList()
                    val module = builder.entities(ModuleEntity::class.java).firstOrNull()
                    if (module != null && staleNimLibs.isNotEmpty()) {
                        val removeIds = staleNimLibs.map { it.symbolicId }.toSet()
                        builder.modifyModuleEntity(module) {
                            dependencies.removeAll { it is LibraryDependency && it.library in removeIds }
                        }
                    }
                    staleNimLibs.forEach { builder.removeEntity(it) }

                    val existing = builder.resolve(libId)
                    if (existing == null) {
                        builder.addEntity(LibraryEntity(libName, LibraryTableId.ProjectLibraryTableId, roots, entitySource))
                    } else {
                        builder.modifyLibraryEntity(existing) {
                            this.roots.clear()
                            this.roots.addAll(roots)
                        }
                    }

                    if (module != null) {
                        val libDep = LibraryDependency(libId, false, DependencyScope.COMPILE)
                        if (module.dependencies.none { it is LibraryDependency && it.library == libId }) {
                            builder.modifyModuleEntity(module) { dependencies.add(libDep) }
                        }
                    }
                }
            }
        }
    }
}

private fun findNimStdlibDir(settings: NimSettings): Pair<Path, String>? {
    val version = runNimVersion(settings) ?: return null
    val pkgs2 = nimblePkgs2Dir(settings.nimbleBinPath)
    if (!Files.isDirectory(pkgs2)) return null
    val nimDir = Files.newDirectoryStream(pkgs2, "nim-$version-*").use { it.firstOrNull() } ?: return null
    val libDir = nimDir.resolve("lib")
    return if (Files.isDirectory(libDir)) libDir to version else null
}

private fun runNimVersion(settings: NimSettings): String? = runCatching {
    val pb = ProcessBuilder(settings.nim(), "--version")
        .redirectError(ProcessBuilder.Redirect.DISCARD)
    if (settings.nimbleBinPath.isNotBlank()) {
        val currentPath = System.getenv("PATH") ?: ""
        pb.environment()["PATH"] = "${settings.nimbleBinPath}${File.pathSeparator}$currentPath"
    }
    pb.start().inputStream.bufferedReader().readLine()?.let { parseNimVersion(it) }
}.getOrNull()

internal fun parseNimVersion(firstLine: String): String? =
    Regex("""Version (\d+\.\d+\.\d+)""").find(firstLine)?.groupValues?.get(1)

internal fun nimblePkgs2Dir(nimbleBinPath: String): Path =
    if (nimbleBinPath.isNotBlank())
        Path.of(nimbleBinPath).parent.resolve("pkgs2")
    else
        Path.of(System.getProperty("user.home"), ".nimble", "pkgs2")
