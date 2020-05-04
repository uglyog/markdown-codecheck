package com.github.uglyog.markdowncodecheck

import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project

class MarkdownCodecheckPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val readme = File(project.projectDir, "README.md")
    if (readme.exists()) {
      project.tasks.register("checkReadme", CheckMarkdownTask::class.java) {
        it.markdownFile = readme
      }
    }
  }
}
