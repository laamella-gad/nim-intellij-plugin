package com.laamella.nim.projectconfig

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable

fun configureNimModule(project: Project): Module {
    // Nim projects are single-module by convention; create one if the project has none (e.g. new project wizard)
    return ModuleManager.getInstance(project).modules.firstOrNull()
        ?: ApplicationManager.getApplication().runWriteAction(Computable {
            ModuleManager.getInstance(project).newModule(
                "${project.basePath}/${project.name}.iml",
                ModuleTypeManager.getInstance().defaultModuleType.id
            )
        })
}
