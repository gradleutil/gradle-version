
package net.gradleutil.gradleversion

import org.ajoberstar.grgit.Grgit
import spock.lang.Specification
import spock.lang.TempDir
import org.gradle.testkit.runner.GradleRunner


class GradleVersionPluginFunctionalTest extends Specification {
    @TempDir
    private File projectDir

    private getBuildFile() {
        new File(projectDir, "build.gradle")
    }

    private getSettingsFile() {
        new File(projectDir, "settings.gradle")
    }

    private getGitDir() {
        new File(projectDir, ".git")
    }

    def "can run task"() {
        given:
        settingsFile << ""
        buildFile << """
plugins {
    id('net.gradleutil.gradle-version')
}
"""

        when:
        println "init git repo ${gitDir}"
        Grgit.init(dir:gitDir).close()
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("showVersion","-iS")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("0.0.1-SNAPSHOT")

    }
}
