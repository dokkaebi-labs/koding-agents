package theunderdog.ai

import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient

fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")

    val client = AnthropicLLMClient(apiKey)
    println("LLM 클라이언트 생성")
}