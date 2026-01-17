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
    val json = Json { ignoreUnknownKeys = true }
    val model = AnthropicModels.Opus_4_5
    var iterations = 0
    val maxIterations = 10

    val conversationHistory = mutableListOf<Pair<String, String>>()
    val systemPrompt = """
            당신은 코딩 에이전트입니다.
            
            사용 가능한 도구:
            - readFile(path: String): String - 주어진 경로의 파일 내용을 읽어서 반환합니다.
            
            도구 사용 규칙
            - [중요] 도구를 사용하려면 반드시 다음 JSON 형식으로 응답하세요:
            {"tool": "readFile", "args": {"path": "파일경로"}}
            - 도구가 필요하지 않은 일반 대화는 그냥 텍스트로 응답하세요.
        """.trimIndent()

    fun parseToolCall(response: String): ToolCall? {
        return try {
            json.decodeFromString<ToolCall>(response)
        } catch (e: Exception) {
            null
        }
    }

    // 사용자 입력 받기
    print("User: ")
    val userPrompt = readln()


    while (maxIterations > iterations) {
        val currentPrompt = prompt("agent-loop") {
            system(systemPrompt)
            user(userPrompt)
            conversationHistory.forEach { (assistantMsg, userMsg) ->
                assistant(assistantMsg)
                user(userMsg)
            }
        }

        val response = executor.execute(currentPrompt, model)
        val llmResponse = response.first().content

        val toolCall = parseToolCall(llmResponse)
        if (toolCall != null) {
            val toolResult = when (toolCall.tool) {
                "readFile" -> readFile(toolCall.args.path)
                else -> "알 수 없는 Tool입니다: ${toolCall.tool}"
            }
            println("Tool 결과: ${toolResult.take(100)}...")
            conversationHistory.add(llmResponse to toolResult)
            iterations++
        } else {
            println("Assistant: $llmResponse")
            break
        }
    }
}