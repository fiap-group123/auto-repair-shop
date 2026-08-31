plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

group = "br.com"
version = "0.0.1-SNAPSHOT"
description = "auto-repair-shop"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins(libs.detekt.rules.ktlint)

    implementation(libs.bundles.spring.runtime)
    implementation(libs.flyway.postgresql)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.springdoc.openapi.webmvc.ui)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register<Test>("unitTest") {
    group = "verification"
    description = "Unit tests without Testcontainers"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("unit")
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    ignoreFailures = false
    config.setFrom(files("config/detekt/detekt.yml"))
}

configurations.matching { it.name.contains("detekt", ignoreCase = true) }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(libs.versions.kotlin.get())
        }
    }
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
//    dependsOn("installGitHooks")
    jvmTarget.set("17")
    reports {
        html.required.set(true)
        markdown.required.set(true)
        sarif.required.set(true)
        checkstyle.required.set(false)
    }
}

tasks.named("check") {
    dependsOn("detektMain")
}

//val installGitHooks by tasks.registering(type = Exec::class) {
//    group = "verification"
//    description = "Installs the Git pre-commit hook that runs Detekt."
//    workingDir(rootDir)
//    commandLine("sh", "hooks/install.sh")
//    onlyIf { layout.projectDirectory.dir(".git").asFile.exists() }
//    inputs.files(
//        layout.projectDirectory.file("hooks/install.sh"),
//        layout.projectDirectory.file("hooks/pre-commit"),
//    )
//    outputs.file(layout.projectDirectory.file(".git/hooks/pre-commit"))
//}

//tasks.named("compileKotlin") {
//    dependsOn(installGitHooks)
//}

kover {
    reports {
        filters {
            includes {
                packages(
                    "br.com.autorepairshop.customer.domain",
                    "br.com.autorepairshop.customer.application",
                    "br.com.autorepairshop.customer.infrastructure.persistence",
                    "br.com.autorepairshop.authentication.domain",
                    "br.com.autorepairshop.authentication.application",
                    "br.com.autorepairshop.authentication.infrastructure.persistence",
                    "br.com.autorepairshop.serviceorder.domain",
                    "br.com.autorepairshop.serviceorder.application",
                    "br.com.autorepairshop.serviceorder.infrastructure",
                    "br.com.autorepairshop.catalog.domain",
                    "br.com.autorepairshop.catalog.application",
                    "br.com.autorepairshop.catalog.infrastructure.persistence",
                    "br.com.autorepairshop.shared.domain",
                    "br.com.autorepairshop.api.controller",
                )
            }
            excludes {
                classes(
                    "*HashedPassword",
                    "*UserId",
                    "*CustomerId",
                    "*VehicleId",
                    "*ServiceOrderId",
                    "*ServiceId",
                    "*DocumentType",
                    "*LicensePlateType",
                    "*Role",
                    "*ServiceOrderStatus",
                    "*ServiceStatus",
                    "*ContactInfo",
                    "*Entity",
                    "*JpaRepository",
                )
            }
        }
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
        }
        verify {
            rule {
                minBound(98)
            }
        }
    }
}
