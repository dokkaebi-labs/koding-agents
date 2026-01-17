package theunderdog.ai.utills

import java.io.File

fun resolveFilePath(path: String): File {
    val workingDir = File(System.getProperty("user.dir"))

    val directFile = File(path)
    if (directFile.exists()) return directFile.canonicalFile

    val trimmedPath = path.trimStart('/')
    if (trimmedPath != path) {
        val relativeFile = File(workingDir, trimmedPath)
        if (relativeFile.exists()) return relativeFile.canonicalFile
    }

    return File(workingDir, path).canonicalFile
}