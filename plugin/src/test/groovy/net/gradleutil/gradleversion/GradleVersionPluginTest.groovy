package net.gradleutil.gradleversion

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradleVersionPluginTest extends Specification {

    def "plugin registers task"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        def plugin = project.plugins.apply(GradleVersionPlugin)

        then:
        project.tasks.findByName("showVersion") != null

        if (plugin.git.status().isClean()) {
            plugin.getVersionFromGit("0.0.0").toString() == '0.0.0'
        } else {
            plugin.getVersionFromGit("0.0.0").toString() == '0.0.1-SNAPSHOT'
        }
    }

}
