package me.rerere.discordij.service

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
    C("C", "c"),
    CPP("C++", "cpp"),
    CSHARP("C#", "csharp"),
    VUE("Vue", "vue"),
    PHP("PHP", "php"),
    GOLANG("Go", "golang"),
    FILE("*", "file"), // FALLBACK
}

fun getFileTypeByName(name: String, extension: String?) = when (name) {
    "JAVA" -> FileType.JAVA
    "Kotlin" -> FileType.KOTLIN
    "Rust" -> FileType.RUST
    "Python" -> FileType.PYTHON
    "JavaScript" -> FileType.JAVASCRIPT
    "TypeScript" -> FileType.TYPESCRIPT
    else -> when (extension) {
        "c", "h" -> FileType.C
        "cpp", "hpp" -> FileType.CPP
        "vue" -> FileType.VUE
        "cs" -> FileType.CSHARP
        "php" -> FileType.PHP
        "go" -> FileType.GOLANG
        else -> FileType.FILE.also {
            DiscordIJ.logger.warn("Unknown file type: $name ($extension)")
        }
    }
}