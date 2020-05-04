package com.github.uglyog.markdowncodecheck

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class MarkdownCodecheckPluginFunctionalSpec extends Specification {
    def 'can run task'() {
      given:
        // Setup the test build
        def projectDir = new File('build/functionalTest')
        projectDir.mkdirs()
        new File(projectDir, 'settings.gradle').text = ''
        new File(projectDir, 'build.gradle').text = """
            plugins {
                id('com.github.uglyog.markdown_codecheck')
            }
        """

      when:
        // Run the build
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments('checkReadme')
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        // Verify the result
        result.output.contains("Hello from plugin 'markdown_codecheck.checkReadme'")
    }
}
