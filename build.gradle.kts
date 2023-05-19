plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.intellij") version "1.13.2"
}

group = "me.rerere"
version = "1.0.8"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.JnCrMx:discord-game-sdk4j:java-impl-SNAPSHOT")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(file("token/chain.crt").readText())
        privateKey.set(file("token/private.pem").readText())
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runIde {
//        ideDir.set(file(
//            "/Users/re/Library/Application Support/JetBrains/Toolbox/apps/Gateway/ch-0/231.9011.34/JetBrains Gateway.app/Contents"
//        ))
//        ideDir.set(
//            file(
//                "/Users/re/Library/Application Support/JetBrains/Toolbox/apps/WebStorm/ch-0/223.8836.50/WebStorm.app/Contents"
//            )
//        )
    }
}
