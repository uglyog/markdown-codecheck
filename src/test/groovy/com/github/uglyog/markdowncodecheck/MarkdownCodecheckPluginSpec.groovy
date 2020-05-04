package com.github.uglyog.markdowncodecheck

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class MarkdownCodecheckPluginSpec extends Specification {
  def 'plugin registers task'() {
    given:
    // Create a test project and apply the plugin
    def project = ProjectBuilder.builder().build()

    when:
    project.plugins.apply('com.github.uglyog.markdown-codecheck')

    then:
    project.tasks.findByName('checkReadme')
  }
}
