package theunderdog.ai.storage

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
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
import java.nio.file.Path

class JsonlConversationHistoryStorage(
    private val fs: JVMFileSystemProvider.ReadWrite,
    private val sessionDir: Path,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    },
): ConversationHistoryStorage {

    companion object {
        private const val COMPRESS_THRESHOLD = 4
        private const val KEEP_RECENT = 2
    }

    private val summaryFile: Path
        get() = fs.joinPath(sessionDir, "summary.md")

    private val historyFile: Path
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
        return loadAllMessages().takeLast(KEEP_RECENT)
    }

    override suspend fun getSummary(): String? {
        if (!fs.exists(summaryFile)) return null
        return fs.readText(summaryFile).ifBlank { null }
    }

    override suspend fun compressHistory(
        executor: PromptExecutor,
        model: LLModel
    ) {
        val allMessages = loadAllMessages()

        // 1단계: 압축이 필요한지 확인
        if (allMessages.size <= COMPRESS_THRESHOLD) return

        // 2단계: 요약할 대상 분리 (전체에서 최근 N개를 뺀 나머지)
        val toSummarize = allMessages.dropLast(KEEP_RECENT)

        // 3단계: 요약 프롬프트 구성
        val conversationText = buildString {
            // 기존 요약이 있으면 포함
            getSummary()?.let {
                appendLine("이전 요약: $it")
                appendLine()
            }
            // 요약할 대화 추가
            toSummarize.forEach { msg ->
                when (msg) {
                    is Message.User -> appendLine("User: ${msg.content}")
                    is Message.Assistant -> appendLine("Assistant: ${msg.content}")
                    else -> {}
                }
            }
        }

        // 4단계: LLM에게 요약 요청
        val summarizePrompt = prompt("summarize") {
            system(conversationText)
            user("이 대화를 간결하게 요약하세요:")
        }
        val response = executor.execute(model = model, prompt = summarizePrompt)

        // 5단계: 요약 저장
        fs.writeText(summaryFile, response.first().content)
    }

    private suspend fun loadAllMessages(): List<Message> {
        if (!fs.exists(historyFile)) {
            return emptyList()
        }
        return fs.readText(historyFile).lines()
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