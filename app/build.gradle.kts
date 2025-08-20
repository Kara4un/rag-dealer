plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
    id("org.openapi.generator") version "7.5.0"
}

import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.ai:spring-ai-core:1.0.0-M6")
    implementation("org.opensearch.client:opensearch-java:3.1.0")
    // OpenAPI 'java' client (okhttp-gson library)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.gsonfire:gson-fire:1.8.5")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    runtimeOnly("ch.qos.logback:logback-classic")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}


// OpenAPI client generation (place spec at: rootDir/openapi/gigachat.yaml)
val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

// Register a dedicated task for generation
val generateGigachatClient = tasks.register("generateGigachatClient", GenerateTask::class) {
    generatorName.set("java")
    library.set("okhttp-gson")
    inputSpec.set("${project.rootDir}/openapi/gigachat.yml")
    outputDir.set(openApiOutputDir.get().asFile.path)
    apiPackage.set("ru.kara4un.ragdealer.gigachat.api")
    modelPackage.set("ru.kara4un.ragdealer.gigachat.model")
    invokerPackage.set("ru.kara4un.ragdealer.gigachat.invoker")
    // Temporary workaround: skip strict spec validation if spec uses non-ASCII keys
    validateSpec.set(false)
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useJakartaEe" to "true"
    ))
    doFirst {
        // ensure no stale files from previous generator/libraries
        project.delete(openApiOutputDir.get().asFile)
    }
}


sourceSets {
    named("main") {
        java.srcDir(openApiOutputDir.map { it.dir("src/main/java").asFile })
        resources.srcDir(openApiOutputDir.map { it.dir("src/main/resources").asFile })
    }
}

tasks.named("compileJava").configure {
    dependsOn(generateGigachatClient)
}

// Ensure generated resources are available before processing
tasks.named("processResources").configure {
    dependsOn(generateGigachatClient)
}

