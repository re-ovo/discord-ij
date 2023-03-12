package me.rerere.discordij.service

import com.intellij.openapi.vfs.VirtualFile
import me.rerere.discordij.DiscordIJ

enum class FileType(
    val typeName: String, // can be got from VirtualFile.getFileType().getName()
    val icon: String
) {
    JAVA("JAVA", "java"),
    KOTLIN("Kotlin", "kotlin"),
    RUST("Rust", "rust"),
    PYTHON("Python", "python"),
    JAVASCRIPT("JavaScript", "javascript"),
    TYPESCRIPT("TypeScript", "typescript"),
    FILE("*", "file"), // FALLBACK
}

fun getFileTypeByName(name: String) = when (name) {
    "JAVA" -> FileType.JAVA
    "Kotlin" -> FileType.KOTLIN
    "Rust" -> FileType.RUST
    "Python" -> FileType.PYTHON
    "JavaScript" -> FileType.JAVASCRIPT
    "TypeScript" -> FileType.TYPESCRIPT
    else -> FileType.FILE.also {
        DiscordIJ.logger.warn("Unknown file type: $name")
    }
}