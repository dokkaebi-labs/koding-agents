package theunderdog.ai

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor

suspend fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")

    val client = AnthropicLLMClient(apiKey)
    val executor = SingleLLMPromptExecutor(client)

    // 사용자 입력 받기
    print("User: ")
    val userPrompt = readln()

    // 프롬프트 구성
    val prompt = prompt(id = "hello-koog") {
        system("당신은 코딩 에이전트 입니다.")
        user(userPrompt)
    }

    //LLM에 요청 보내고 응답 받기
    val response = executor.execute(
        prompt = prompt,
        model = AnthropicModels.Opus_4_5
    )

    println("Assistant: ${response.first().content}")
}