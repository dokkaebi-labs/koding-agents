package theunderdog.ai.storage

import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

interface ConversationHistoryStorage {
    suspend fun addConversation(userMessage: String, assistantMessage: String)
    suspend fun getHistory(): List<Message>
    suspend fun getSummary(): String?
    suspend fun compressHistory(executor: PromptExecutor, model: LLModel)
}

@Serializable
data class JsonlEntry(
    val timeStamp: Instant,
    val message: Message
)