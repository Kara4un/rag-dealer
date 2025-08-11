plugins {
    id("io.micronaut.library")
}

micronaut {
    processing {
        incremental(true)
        annotations("ru.kara4un.ragdealer.*")
    }
}
