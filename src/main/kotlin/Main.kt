package theunderdog.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tool
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import theunderdog.ai.tools.editFile
import theunderdog.ai.tools.listFiles
import theunderdog.ai.tools.readFile

suspend fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
        ?: error("API KEY가 설정되지 않았습니다.")

    val executor = simpleAnthropicExecutor(apiKey)
    val model = AnthropicModels.Opus_4_5
    val toolRegistry = ToolRegistry {
        tool(::readFile)
        tool(::listFiles)
        tool(::editFile)
    }

    val systemPrompt = "당신은 코딩 에이전트입니다."

    // 사용자 입력 받기
    print("User: ")
    val userPrompt = readln()

    val agent = AIAgent(
        promptExecutor = executor,
        systemPrompt = systemPrompt,
        llmModel = model,
        toolRegistry = toolRegistry,
        maxIterations = 10,
    )

    val response = agent.run(userPrompt)
    println("Assistant: $response")
}
