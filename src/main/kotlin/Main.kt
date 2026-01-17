package theunderdog.ai

fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")

    println("API Key 로드 완료: ${apiKey.take(10)}...")
}