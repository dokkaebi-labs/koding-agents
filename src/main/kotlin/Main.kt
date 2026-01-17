package theunderdog.ai

import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor

fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")

    val client = AnthropicLLMClient(apiKey)
    val executor = SingleLLMPromptExecutor(client)
    println("Prompt Executor 생성 완료")
}