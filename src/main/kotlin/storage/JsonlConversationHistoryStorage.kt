package theunderdog.ai.storage

import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.rag.base.files.JVMFileSystemProvider
import ai.koog.rag.base.files.createDirectory
import ai.koog.rag.base.files.readText
import ai.koog.rag.base.files.writeText
import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System.now
import kotlinx.serialization.json.Json

class JsonlConversationHistoryStorage(
    private val fs: JVMFileSystemProvider.ReadWrite,
    private val sessionDir: java.nio.file.Path,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    },
): ConversationHistoryStorage {
    private val historyFile: java.nio.file.Path
        get() = fs.joinPath(sessionDir, "session.jsonl")

    init {
        kotlinx.coroutines.runBlocking {
            if (!fs.exists(sessionDir)) {
                fs.createDirectory(sessionDir)
            }
        }
    }

    override suspend fun addConversation(
        userMessage: String,
        assistantMessage: String
    ) {
        val existingContent = if (fs.exists(historyFile)) {
            fs.readText(historyFile)
        } else ""

        val userEntry = JsonlEntry(
            timeStamp = now(),
            message = Message.User(userMessage, RequestMetaInfo.create(Clock.System))
        )
        val assistantEntry = JsonlEntry(
            timeStamp = now(),
            message = Message.Assistant(assistantMessage, ResponseMetaInfo.create(Clock.System))
        )

        val newLines = listOf(userEntry, assistantEntry)
            .joinToString("\n") { entry ->
                json.encodeToString(JsonlEntry.serializer(), entry)
            }
        val updatedContent = if (existingContent.isEmpty()) {
            newLines
        } else {
            existingContent + "\n" + newLines
        }

        fs.writeText(historyFile, updatedContent)
    }


    override suspend fun getHistory(): List<Message> {
        if (!fs.exists(historyFile)) {
            return emptyList()
        }

        val content = fs.readText(historyFile)
        return content.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val entry = json.decodeFromString<JsonlEntry>(line)
                    entry.message
                } catch (e: Exception) {
                    System.err.println("해당 줄을 구문 분석하는 데 실패했습니다. $line")
                    null
                }
            }
    }
}