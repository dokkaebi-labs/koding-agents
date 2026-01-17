package theunderdog.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import theunderdog.ai.utills.resolveFilePath

@Tool("readFileTool")
@LLMDescription("주어진 경로의 파일 내용을 읽어서 반환합니다")
fun readFile(
    @LLMDescription("읽을 파일의 경로") path: String
): String {
    require(path.isNotBlank()) { "파일 경로가 비어있습니다" }

    val file = resolveFilePath(path)

    if (!file.exists()) return "오류: 파일을 찾을 수 없습니다: $path"
    if (!file.isFile) return "오류: 디렉토리 입니다: $path"
    if (!file.canRead()) return "오류: 읽기 권한이 없습니다: $path"

    return try { file.readText() }
    catch (e: Exception) { "오류: ${e.message}" }
}