package net.gradleutil.gradleversion

import org.ajoberstar.grgit.Grgit
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

class GradleVersionPluginTest extends Specification {
    @Shared
    @TempDir
    private File tmpGitDir, tmpDirEmpty

    @Shared
    File originRepoDir, clonedRepoDir, originFile, clonedFile


    def setupSpec() {
        if (tmpGitDir.exists()) {
            tmpGitDir.deleteDir()
        }
        def projectDir = Grgit.open().repository.rootDir.parentFile.parentFile.path
        originRepoDir = new File(tmpGitDir, 'originRepo')
        originFile = new File(originRepoDir, 'test_git.txt')
        clonedRepoDir = new File(tmpGitDir, 'checkedOutRepo')
        clonedFile = new File(clonedRepoDir, 'test_git.txt')
    }

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
