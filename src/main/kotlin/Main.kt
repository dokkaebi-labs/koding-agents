package theunderdog.ai

suspend fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")
    val codingAgent = CodingAgent(apiKey)
    println("Coding Agent가 시작되었습니다. 종료하면 'exit'을 입력하세요.")
    println()

    while (true) {
        print("User: ")
        val input = readln().trim()
        if (input.isEmpty()) continue
        if (input == "exit") {
            println("종료합니다.")
            break
        }

        val response = codingAgent.chat(input)
        println("Assistant: $response")
        println()
    }
}
