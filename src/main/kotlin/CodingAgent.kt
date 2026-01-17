package theunderdog.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tool
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.message.Message
import ai.koog.rag.base.files.JVMFileSystemProvider
import theunderdog.ai.storage.ConversationHistoryStorage
import theunderdog.ai.storage.JsonlConversationHistoryStorage
import theunderdog.ai.tools.*
import java.nio.file.Path
import java.util.*

class CodingAgent(
    apiKey: String,
    projectDir: String = "dir",
    sessionId: String = UUID.randomUUID().toString(),
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
    val conversationHistoryStorage: ConversationHistoryStorage =
        JsonlConversationHistoryStorage(
            fs = JVMFileSystemProvider.ReadWrite,
            sessionDir = Path.of(".koding_agent/projects/$projectDir/$sessionId"),
        )

    suspend fun chat(userMessage: String): String {
        conversationHistoryStorage.compressHistory(executor, model)

        val history = conversationHistoryStorage.getHistory()
        val summary = conversationHistoryStorage.getSummary()
        val system = buildSystemPromptWithHistory(history, summary)

        val agent = AIAgent(
            promptExecutor = executor,
            systemPrompt = system,
            llmModel = model,
            toolRegistry = toolRegistry,
            maxIterations = 10,
        )

        val assistantMessage = agent.run(userMessage)
        conversationHistoryStorage.addConversation(
            userMessage = userMessage,
            assistantMessage = assistantMessage,
        )

        return assistantMessage
    }

    private fun buildSystemPromptWithHistory(
        history: List<Message>,
        summary: String?,
    ): String {
        if (history.isEmpty()) {
            return systemPrompt
        }
        return buildString {
            appendLine("# System Prompt")
            appendLine(systemPrompt)
            summary?.let { appendLine("\n# Previous Conversation Summary\n$it") }
            if (history.isNotEmpty()) {
                appendLine("# Conversation History")
                history.forEach { message ->
                    when (message) {
                        is Message.User -> appendLine("User: ${message.content}")
                        is Message.Assistant -> appendLine("Assistant: ${message.content}")
                        else -> {}
                    }
                }
                appendLine()
                appendLine("위의 맥락을 바탕으로 대화를 이어가주세요.")
            }
        }
    }
}