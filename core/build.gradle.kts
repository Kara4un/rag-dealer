import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation("org.springframework:spring-context")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
