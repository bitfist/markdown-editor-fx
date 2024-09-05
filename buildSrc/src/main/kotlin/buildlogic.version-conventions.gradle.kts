import git.semver.plugin.changelog.ChangeLogFormatter
import git.semver.plugin.changelog.ChangeLogTextFormatter
import java.io.ByteArrayOutputStream

plugins {
    id("com.github.jmongard.git-semver-plugin")
}

semver {
    patchPattern = "\\A(fix|chore|docs|refactor|ci|build|test|deps)(?:\\([^()]+\\))?:"

    val server = System.getenv("GITHUB_SERVER_URL") ?: "https://github.com"
    val repository = System.getenv("GITHUB_REPOSITORY") ?: "bitfist/markdown-editor-fx"
    val repositoryUrl = "$server/$repository"

    fun ChangeLogTextFormatter.appendChangeLongEntry() {
        val longHash = hash().take(40)
        val shortHash = longHash.take(8)

        append("- [$shortHash]($repositoryUrl/commit/$longHash) ").append(scope()).appendLine(header())
    }

    changeLogFormat = ChangeLogFormatter {
        constants.headerTexts["refactor"] = "### Refactorings \uD83D\uDD28"

        appendLine(constants.header).appendLine()

        withType("release") {
            skip()
        }

        // breaking
        withBreakingChanges {
            appendLine(constants.breakingChange)
            formatChanges {
                appendChangeLongEntry()
            }
            appendLine()
        }

        // fix, feat, refactor
        withType(types = arrayOf("feat", "fix", "refactor")) {
            appendLine(constants.headerTexts[groupKey])
            with({ constants.headerTexts.containsKey(it.scope) }) {
                formatChanges {
                    appendChangeLongEntry()
                }
            }
            formatChanges {
                appendChangeLongEntry()
            }
            appendLine()
        }

        // chores and other known changes
        groupBySorted({ constants.headerTexts[it.scope] ?: constants.headerTexts[it.type] }) {
            appendLine(groupKey)
            with({ constants.headerTexts.containsKey(it.scope) }) {
                formatChanges {
                    appendChangeLongEntry()
                }
            }
            formatChanges {
                appendChangeLongEntry()
            }
            appendLine()
        }

        // other unknown changes
        otherwise {
            appendLine(constants.otherChange)
            formatChanges {
                appendChangeLongEntry()
            }
            appendLine()
        }

        appendLine(constants.footer)
    }
}

// look up git tag
var gitTagVersion = "0.0.1"
try {
    gitTagVersion = ByteArrayOutputStream().use { outputStream ->
        project.exec {
            commandLine("git", "tag", "--points-at", "HEAD")
            standardOutput = outputStream
        }
        outputStream.toString().trim()
    }
} catch (t: Throwable) {
    gitTagVersion = "0.0.1"
    logger.warn("Warning: could not figure out version using git")
}

val semanticVersionRegex = "\\d+\\.\\d+\\.\\d+".toRegex()

// if we are on a tag-commit, we use the tag version
version = if (gitTagVersion.matches(semanticVersionRegex)) {
    gitTagVersion // version provided by git tag
} else {
    semver.version // version provided by the semantic versioning plugin
}
