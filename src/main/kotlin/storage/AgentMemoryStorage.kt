package theunderdog.ai.storage

import ai.koog.rag.base.files.JVMFileSystemProvider
import ai.koog.rag.base.files.readText
import ai.koog.rag.base.files.writeText
import java.nio.file.Path

interface AgentMemoryStorage {
    suspend fun addMemory(content: String)
    suspend fun getMemory(): String?
}

class KodingMemoryStorage(
    private val fs: JVMFileSystemProvider.ReadWrite,
) : AgentMemoryStorage {
    private val memoryFile: Path = Path.of("./KODING.md")

    override suspend fun addMemory(content: String) {
        val existing = getMemory() ?: ""
        val updated = if (existing.isEmpty()) {
            "- $content"
        } else {
            "$existing\n- $content"
        }
        fs.writeText(memoryFile, updated)
    }

    override suspend fun getMemory(): String? {
        if(!fs.exists(memoryFile)) return null
        return fs.readText(memoryFile).ifBlank { null }
    }
}

