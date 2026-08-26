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
    dependsOn("installGitHooks")
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

val installGitHooks by tasks.registering(type = Exec::class) {
    group = "verification"
    description = "Installs the Git pre-commit hook that runs Detekt."
    workingDir(rootDir)
    commandLine("sh", "hooks/install.sh")
    onlyIf { layout.projectDirectory.dir(".git").asFile.exists() }
    inputs.files(
        layout.projectDirectory.file("hooks/install.sh"),
        layout.projectDirectory.file("hooks/pre-commit"),
    )
    outputs.file(layout.projectDirectory.file(".git/hooks/pre-commit"))
}

tasks.named("compileKotlin") {
    dependsOn(installGitHooks)
}

kover {
    reports {
        filters {
            includes {
                packages(
                    "br.com.autorepairshop.customer.domain.aggregate",
                    "br.com.autorepairshop.customer.domain.valueobject",
                    "br.com.autorepairshop.customer.application.usecase",
                    "br.com.autorepairshop.authentication.domain.aggregate",
                    "br.com.autorepairshop.authentication.domain.valueobject",
                    "br.com.autorepairshop.authentication.application.usecase",
                    "br.com.autorepairshop.serviceorder.domain.aggregate",
                    "br.com.autorepairshop.serviceorder.domain.valueobject",
                    "br.com.autorepairshop.serviceorder.application.usecase",
                    "br.com.autorepairshop.catalog.domain.aggregate",
                    "br.com.autorepairshop.catalog.domain.valueobject",
                    "br.com.autorepairshop.catalog.application.usecase",
                )
            }
            excludes {
                classes(
                    "*HashedPassword",
                    "*UserId",
                    "*CustomerId",
                    "*VehicleId",
                    "*ServiceOrderId",
                    "*OfferedServiceId",
                    "*DocumentType",
                    "*LicensePlateType",
                    "*Role",
                    "*ServiceOrderStatus",
                    "*ContactInfo",
                    "*ServiceOrderItem",
                    "*ServiceOrderTimeline",
                )
            }
        }
        verify {
            rule {
                minBound(98)
            }
        }
    }
}
