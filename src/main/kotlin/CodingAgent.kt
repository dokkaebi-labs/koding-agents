package theunderdog.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgent.Companion.invoke
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.ToolRegistry.Companion.invoke
import ai.koog.agents.core.tools.reflect.tool
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import theunderdog.ai.tools.bash
import theunderdog.ai.tools.codeSearch
import theunderdog.ai.tools.editFile
import theunderdog.ai.tools.listFiles
import theunderdog.ai.tools.readFile

class CodingAgent(
    apiKey: String,
) {
    private val executor = simpleAnthropicExecutor(apiKey)
    private val model = AnthropicModels.Opus_4_5
    private val toolRegistry = ToolRegistry {
        tool(::readFile)
        tool(::listFiles)
        tool(::editFile)
        tool(::bash)
        tool(::codeSearch)
    }
    private val systemPrompt = "당신은 코딩 에이전트입니다."

    suspend fun chat(userMessage: String): String {
        val agent = AIAgent(
            promptExecutor = executor,
            systemPrompt = systemPrompt,
            llmModel = model,
            toolRegistry = toolRegistry,
            maxIterations = 10,
        )

        return agent.run(userMessage)
    }
}