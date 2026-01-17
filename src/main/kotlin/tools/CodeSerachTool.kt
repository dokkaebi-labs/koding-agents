package theunderdog.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import java.util.concurrent.TimeUnit
@Tool(customName = "code_search")
@LLMDescription(description = """
    ripgrep을 사용해 코드를 검색합니다. 
    정규식을 지원합니다.
    파일 경로, 라인 번호, 매칭된 내용을 반환합니다.
    가독성을 위해 결과를 50개로 제한합니다.

    예시:
    - pattern="TODO" → TODO 주석 찾기
    - pattern="fun main" fileType="kt" → Kotlin에서 main 함수
    - pattern="class.*implements" → 클래스 구현 정규식 검색
""")
fun codeSearch(
    @LLMDescription("검색 패턴 (정규식 지원)")
    pattern: String,
    @LLMDescription("검색할 경로 (기본값: 현재 디렉토리)")
    path: String = ".",
    @LLMDescription("파일 타입 필터 (예: 'kt', 'java', 'py')")
    fileType: String? = null,
    @LLMDescription("대소문자 구분 여부 (기본값: false)")
caseSensitive: Boolean = false,
): String {
    // ripgrep 명령 빌드
    val cmdArgs = mutableListOf("rg")
    cmdArgs.add("--line-number")
    cmdArgs.add("--with-filename")
    cmdArgs.add("--color=never")
    cmdArgs.add("--no-heading")

    if (!caseSensitive) {
        cmdArgs.add("--ignore-case")
    }
    if (fileType != null) {
        cmdArgs.add("--type")
        cmdArgs.add(fileType)
    }
    cmdArgs.add(pattern)
    cmdArgs.add(path)

    val process = ProcessBuilder(cmdArgs)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    val finished = process.waitFor(30, TimeUnit.SECONDS)
    if (!finished) {
        process.destroyForcibly()
        return "타임아웃: 30초 초과"
    }

    return when(val exitCode = process.exitValue()) {
        0 -> {
            val lines = output.trim().lines()
            if (lines.size > 50) {
                buildString {
                    appendLine("${lines.size}개 결과 발견 (처음 50개만 표시):")
                    appendLine()
                    append(lines.take(50).joinToString("\n"))
                    appendLine()
                    appendLine()
                    appendLine("... ${lines.size - 50}개 결과 생략")
                }
            } else {
                buildString {
                    appendLine("${lines.size}개 결과 발견:")
                    appendLine()
                    append(output.trim())
                }
            }
        }
        1 -> "검색 결과 없음: '${pattern}'"
        else -> "ripgrep 오류 (exit code $exitCode): $output"
    }
}