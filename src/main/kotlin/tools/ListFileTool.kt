package theunderdog.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import theunderdog.ai.utills.resolveFilePath
import java.io.File

@Tool("listFileTool")
@LLMDescription("주어진 경로에서 모든 파일과 디렉토리를 재귀적으로 탐색합니다.")
fun listFiles(
    @LLMDescription("탐색할 디렉토리 경로")
    path: String,
): String {
    val basePath = resolveFilePath(path)

    if (!basePath.exists()) "오류: 경로를 찾을 수 없습니다: $path"
    if (!basePath.isDirectory) "오류: 경로가 디렉토리가 아닙니다.: $path"
    if (!basePath.canRead()) return "오류: 디렉토리 읽기 권한이 없습니다: $path"

    val files = mutableListOf<String>()

    fun walkDirectory(dir: File, base: File) {
        val entries = dir.listFiles() ?: return

        for (entry in entries) {
            val relativePath = entry.relativeTo(base).path

            if (entry.isDirectory && shouldExclude(relativePath)) {
                continue
            }

            if (entry.isDirectory) {
                files.add("$relativePath/")
                walkDirectory(entry, base)
            } else {
                files.add(relativePath)
            }
        }
    }

    walkDirectory(basePath, basePath)

    return if (files.isEmpty()) {
        "디렉토리에서 파일을 찾을수 없습니다: $path"
    } else {
        "Found ${files.size} items:\n" + files.sorted().joinToString("\n")
    }
}

private fun shouldExclude(path: String): Boolean {
    val excludedDirs = setOf(".git", ".devenv", "build", "node_modules", ".idea")
    return excludedDirs.any { path.startsWith(it) || path.contains("/$it/") }
}