plugins {
    id("io.micronaut.application")
}

dependencies {
    implementation(project(":core"))
    runtimeOnly("ch.qos.logback:logback-classic")
    implementation("io.micronaut:micronaut-jackson-databind")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.micronaut:micronaut-http-client")
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
