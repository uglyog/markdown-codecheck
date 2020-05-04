package com.github.uglyog.markdowncodecheck

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class MarkdownCodecheckPluginSpec extends Specification {
  def 'plugin registers task'() {
    given:
    def projectDir = new File('build/test/MarkdownCodecheckPluginSpec')
    projectDir.mkdirs()
    new File(projectDir, 'README.md').text = ''
    def project = ProjectBuilder.builder().withProjectDir(projectDir).build()

    when:
    project.plugins.apply('com.github.uglyog.markdown-codecheck')

    then:
    project.tasks.findByName('checkReadme')
  }

  def "plugin won't register task if there is no readme file in the project"() {
    given:
    def project = ProjectBuilder.builder().build()

    when:
    project.plugins.apply('com.github.uglyog.markdown-codecheck')

    then:
    !project.tasks.findByName('checkReadme')
  }
}
