plugins {
    id("io.micronaut.library")
}

micronaut {
    processing {
        incremental(true)
        annotations("ru.kara4un.ragdealer.*")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
