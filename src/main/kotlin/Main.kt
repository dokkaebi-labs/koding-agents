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

    // 프롬프트 구성
    val prompt = prompt(id = "tool-routing") {
        system("""
            당신은 코딩 에이전트입니다.
            
            사용 가능한 도구:
            - readFile(path: String): String - 주어진 경로의 파일 내용을 읽어서 반환합니다.
            
            도구 사용 규칙
            - [중요] 도구를 사용하려면 반드시 다음 JSON 형식으로 응답하세요:
            {"tool": "readFile", "args": {"path": "파일경로"}}
            - 도구가 필요하지 않은 일반 대화는 그냥 텍스트로 응답하세요.
        """.trimIndent())
        user(userPrompt)
    }

    //LLM에 요청 보내고 응답 받기
    val response = executor.execute(
        prompt = prompt,
        model = AnthropicModels.Opus_4_5,
    )
    val llmResponse = response.first().content

    val json = Json {
        ignoreUnknownKeys = true
    }
    val toolCall = json.decodeFromString<ToolCall>(llmResponse)

    val result = when (toolCall.tool) {
        "readFile" -> readFile(toolCall.args.path)
        else -> "알 수 없는 Tool입니다: ${toolCall.tool}"
    }

    println("툴 호출 결과: $result")
}