package theunderdog.ai

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import theunderdog.ai.tools.readFile

@Serializable
data class ToolCall(
    val tool: String,
    val args: ToolArgs,
)


@Serializable
data class ToolArgs(
    val path: String,
)

suspend fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")

    val executor = simpleAnthropicExecutor(apiKey)

    // 사용자 입력 받기
    print("User: ")
    val userPrompt = readln()
    val systemPrompt = """
            당신은 코딩 에이전트입니다.
            
            사용 가능한 도구:
            - readFile(path: String): String - 주어진 경로의 파일 내용을 읽어서 반환합니다.
            
            도구 사용 규칙
            - [중요] 도구를 사용하려면 반드시 다음 JSON 형식으로 응답하세요:
            {"tool": "readFile", "args": {"path": "파일경로"}}
            - 도구가 필요하지 않은 일반 대화는 그냥 텍스트로 응답하세요.
        """.trimIndent()


    // === 1차 LLM 호출 ===
    val prompt = prompt(id = "first") {
        system(systemPrompt)
        user(userPrompt)
    }

    println("\n=== 1차 LLM 호출 ===")
    println("프롬프트: $userPrompt")

    val firstResponse = executor.execute(
        prompt = prompt,
        model = AnthropicModels.Opus_4_5,
    )
    val llmResponse = firstResponse.first().content
    println("LLM 응답: $llmResponse")

    val json = Json { ignoreUnknownKeys = true }
    val toolCall = json.decodeFromString<ToolCall>(llmResponse)
    val toolResult = when (toolCall.tool) {
        "readFile" -> readFile(toolCall.args.path)
        else -> "알 수 없는 Tool입니다: ${toolCall.tool}"
    }

    println("\n=== Tool 실행 ===")
    println("Tool: ${toolCall.tool}")
    println("결과: ${toolResult.take(100)}...")

    println("\n=== Tool 실행 ===")
    println("Tool: ${toolCall.tool}")
    println("결과: ${toolResult.take(100)}...")

    // === 2차 LLM 호출 - Tool 결과 포 함 ===
    val secondPrompt = prompt("second") {
        system(systemPrompt)
        user(userPrompt)
        assistant(llmResponse)
        user(toolResult) // ← Tool 결과 가 여기 들어감!
    }

    println("\n=== 2차 LLM 호출 ===")
    println("프롬프트에 추가 된 Tool 결과: ${toolResult.take(50)}...")

    val finalResponse = executor.execute(
        prompt = secondPrompt,
        model = AnthropicModels.Sonnet_4,
    )
    println("\n=== 최종 응답 ===")
    println(finalResponse.first().content)
}