package theunderdog.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool

@Tool(customName = "bash")
@LLMDescription(
    description = """
    bash 명령어를 실행하고 결과를 반환합니다.
    stdout과 stderr를 합쳐서 반환합니다.
    타임아웃: 120초.
    성공/실패 모두 exit code와 출력을 반환합니다.
""")
fun bash(
    @LLMDescription("실행할 bash 명령어 (예: 'ls -la', 'git status')")
    command: String
): String {
    val dangerous = listOf("rm -rf", "sudo", "chmod 777", "> /dev/")
    if (dangerous.any { command.contains(it) }) {
        return "오류: 위험한 명령어는 실행할 수 없습니다"
    }

    val process = ProcessBuilder("bash", "-c", command)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().use { it.readText() }
    val finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS)

    if (!finished) {
        process.destroyForcibly()
        return "타임아웃: 120초 초과"
    }
    val exitCode = process.exitValue()

    return buildString {
        if (exitCode != 0) appendLine("명령 실패 (exit code: $exitCode)")
        appendLine("출력:")
        append(output.trim().ifEmpty { "(출력 없음)" })
    }
}