package me.rerere.discordij.service

import me.rerere.discordij.DiscordIJ

enum class FileType(
        val typeName: String, // can be got from VirtualFile.getFileType().getName()
        val icon: String
) {
    JAVA("JAVA", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/java.png"),
    KOTLIN("Kotlin", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/kotlin.png"),
    RUST("Rust", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/rust.png"),
    PYTHON("Python", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/python.png"),
    JAVASCRIPT("JavaScript", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/javascript.png"),
    TYPESCRIPT("TypeScript", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/typescript.png"),
    C("C", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/c.png"),
    CPP("C++", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/cpp.png"),
    CSHARP("C#", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/csharp.png"),
    VUE("Vue", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/vue.png"),
    PHP("PHP", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/php.png"),
    GOLANG("Go", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/go.png"),
    RUBY("Ruby", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/ruby.png"),
    SWIFT("Swift", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/swift.png"),
    HTML("HTML", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/html.png"),
    CSS("CSS", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/css.png"),
    SCSS("SCSS", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/sass.png"),
    LESS("LESS", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/less.png"),
    JSON("JSON", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/json.png"),
    XML("XML", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/xml.png"),
    YAML("YAML", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/yaml.png"),
    MARKDOWN("Markdown", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/markdown.png"),
    TEX("TeX", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/tex.png"),
    GIT("Git", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/git.png"),
    SVELTE("Svelte", "https://raw.githubusercontent.com/Arch-Storm/icons/master/png/svelte.png"),
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
        "cpp", "hpp", "cxx", "hxx", "cc", "hh" -> FileType.CPP
        "cs" -> FileType.CSHARP
        "php", "phtml", "php3", "php4", "php5", "phps" -> FileType.PHP
        "rb", "erb", "rake", "rbw" -> FileType.RUBY
        "go" -> FileType.GOLANG
        "swift" -> FileType.SWIFT
        "html", "htm" -> FileType.HTML
        "css" -> FileType.CSS
        "sass", "scss" -> FileType.SCSS
        "less" -> FileType.LESS
        "json" -> FileType.JSON
        "xml", "xsd", "xsl", "xslt" -> FileType.XML
        "yaml", "yml" -> FileType.YAML
        "md", "markdown" -> FileType.MARKDOWN
        "tex", "latex", "sty", "cls" -> FileType.TEX
        "gitignore", "gitconfig" -> FileType.GIT
        "vue" -> FileType.VUE
        "svelte" -> FileType.SVELTE
        else -> FileType.FILE.also {
            DiscordIJ.logger.warn("Unknown file type: $name ($extension)")
        }
    }
}