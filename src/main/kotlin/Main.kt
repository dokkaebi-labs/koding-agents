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

        if (userPrompt.isEmpty()) continue
        if (userPrompt == "exit") {
            println("종료합니다.")
            break
        }

        // /clear 명령 처 리
        if (userPrompt == "/clear") {
            codingAgent = CodingAgent(apiKey) // 새 Agent 생성!
            println("새로운 대화가 시작되었습니다.")
            println()
            continue
        }

        val response = codingAgent.chat(userPrompt)
        println("Assistant: $response")
        println()
    }
}
