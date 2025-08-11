import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.testing.Test

plugins {
    id("io.micronaut.application") version "4.5.4" apply false
    id("io.micronaut.library") version "4.5.4" apply false
}

group = "ru.kara4un"
version = "0.1.0"

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "jacoco")

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
