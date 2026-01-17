package theunderdog.ai

suspend fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")
    var codingAgent = CodingAgent(apiKey)
    println("Coding Agent가 시작되었습니다. 종료하면 'exit'을 입력하세요.")
    println()

    while (true) {
        print("User: ")
        val userPrompt = readln().trim()

        when {
            userPrompt == "/clear" -> {
                codingAgent = CodingAgent(apiKey) // 새 Agent 생성!
                println("새로운 대화가 시작되었습니다.")
                continue
            }

            userPrompt == "/exit" -> {
                println("종료합니다.")
                break
            }

            userPrompt.startsWith("/memory add ") -> {
                val content = userPrompt.removePrefix("/memory add ").trim()
                codingAgent.agentMemoryStorage.addMemory(content)
                println("메모리에 저장했습니다: $content")
            }

            else -> {
                val response = codingAgent.chat(userPrompt)
                println("Agent: $response")
            }
        }
    }
}
