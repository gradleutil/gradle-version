package net.gradleutil.gradleversion

import com.github.zafarkhaja.semver.Version
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty

class GradleVersionPlugin implements Plugin<Project> {

    Grgit git
    String describedCommit
    String revision
    String masterRevision
    DirectoryProperty generatedResources
    String releaseBranchName = 'master'
    String remoteName = 'origin'
    boolean isSnapshot = true
    Project project

    void apply(Project project) {
        this.project = project
        generatedResources = generatedResources ?: project.layout.buildDirectory

        project.version = getVersionFromGit(project.version == 'unspecified' ? "0.0.0" : project.version.toString())

        def generateScmInfoFile = project.tasks.register('generateScmInfoFile') {
            doLast {
                generatedResources.mkdirs()
                def versionFile = new File(generatedResources.asFile.get(), "scm.info.properties")
                versionFile.text = """
                    git.tags=${git.tag.list().name}
                    git.branch=${git.branch.current().name}
                    git.dirty=${!git.status().isClean()}
                    git.remote.origin.url=${git.remote.list().find { it.name == 'origin' }.url}
                    git.commit.id=${git.head().id}
                    git.commit.id.abbreviated=${git.head().abbreviatedId}
                    git.commit.id.describe=${describedCommit}
                    git.commit.user.name=${git.head().author.name}
                    git.commit.user.email=${git.head().author.email}
                    git.commit.message.full=${git.head().fullMessage.trim()}
                    git.commit.message.short=${git.head().shortMessage.trim()}
                    git.commit.time=${git.head().dateTime}
                    """.stripIndent().trim()
            }
        }

        project.tasks.register('generateVersionFile') {
            dependsOn generateScmInfoFile
            doLast {
                generatedResources.mkdirs()
                def versionFile = new File(generatedResources.asFile.get(), "version.properties")
                versionFile.text = project.name + '.version=' + project.version
            }
        }

        project.tasks.register('showVersion') { doLast { println project.version } }

    }

    File getGitDir() {
        File gitDir
        gitDir = new File(project.projectDir, '.git')
        if (!gitDir.exists() || !gitDir.isDirectory()) {
            gitDir = new File(project.rootDir, '.git')
        }
        if (!gitDir.exists() || !gitDir.isDirectory()) {
            gitDir = new File('.git')
        }
        if (!gitDir.exists() || !gitDir.isDirectory()) {
            gitDir = new File('../.git')
        }
        if (!gitDir.exists() || !gitDir.isDirectory()) {
            gitDir = null
        }
        gitDir
    }

    Version getVersionFromGit(String initialVersion) {
        Version version
        version = Version.valueOf(initialVersion)
        if (!gitDir) {
            return version
        }
        try {
            git = Grgit.open(dir: gitDir)

            if (git.head()) {
                describedCommit = git.describe().toString().trim()
                revision = git.head().id
                def status = git.status()
                masterRevision = git.resolve.toCommit(git.resolve.toBranch(remoteName + '/' + releaseBranchName))?.id ?: git.resolve.toCommit(git.resolve.toBranch(releaseBranchName))?.id
                isSnapshot = (masterRevision != revision || status.unstaged.allChanges.size() != 0)
            } else {
                isSnapshot = true
            }

            if (isSnapshot && !initialVersion.toLowerCase().endsWith('-snapshot')) {
                version = version.incrementPatchVersion('SNAPSHOT')
            }

            project.logger.info "Building Version ${version} - Branch ${git.branch.current().name}"
            project.logger.info "Commit message: " + git.head()?.fullMessage ?: '[no commit message]'
        } catch (Exception e) {
            e.printStackTrace()
        }
        return version
    }
}
