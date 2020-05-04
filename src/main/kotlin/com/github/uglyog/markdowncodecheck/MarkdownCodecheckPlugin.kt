package com.github.uglyog.markdowncodecheck

import org.gradle.api.Plugin
import org.gradle.api.Project

class MarkdownCodecheckPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.tasks.register("checkReadme") { task ->
      task.doLast {
        println("Hello from plugin 'markdown_codecheck.checkReadme'")
      }
    }
  }
}
