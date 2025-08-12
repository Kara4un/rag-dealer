import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("io.micronaut.application")
    id("org.openapi.generator") version "7.5.0"
}

dependencies {
    implementation(project(":core"))
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.projectreactor:reactor-core")
    // OpenAPI 'java' client (okhttp-gson library)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.gsonfire:gson-fire:1.8.5")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

application {
    mainClass.set("ru.kara4un.ragdealer.RagdealerApplication")
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("ru.kara4un.ragdealer.*")
    }
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

// Micronaut plugin task inspects runtime classpath and touches generated resources
tasks.named("inspectRuntimeClasspath").configure {
    dependsOn(generateGigachatClient)
}
