plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.sonar)

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
    jvmTarget.set("17")
    reports {
        checkstyle.required.set(true)
        checkstyle.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.xml"))
        html.required.set(true)
        markdown.required.set(true)
        sarif.required.set(true)
    }
}

tasks.named("check") {
    dependsOn("detektMain")
}

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
                    "br.com.autorepairshop.budget.domain",
                    "br.com.autorepairshop.budget.application",
                    "br.com.autorepairshop.budget.infrastructure.persistence",
                    "br.com.autorepairshop.shared.domain",
                    "br.com.autorepairshop.api.controller",
                )
            }
            excludes {
                classes(
                    "*HashedPassword",
                    "*UserId",
                    "*CustomerInviteId",
                    "*CustomerId",
                    "*VehicleId",
                    "*ServiceOrderId",
                    "*ServiceId",
                    "*BudgetId",
                    "*BudgetStatus",
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
                minBound(80)
            }
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "fiap-group123_auto-repair-shop")
        property("sonar.organization", "fiap-group123")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property("sonar.kotlin.file.suffixes", ".kt")

        property("sonar.java.binaries", "build/classes/kotlin/main")
        property("sonar.java.test.binaries", "build/classes/kotlin/test")
        property("sonar.java.libraries", "build/libs")

        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.sarif")
        property(
            "sonar.exclusions",
            "**/build/**,**/generated/**,**/*.sql",
        )
        property("sonar.coverage.exclusions", "**/dto/**,**/*Entity.kt,**/*JpaRepository.kt")
    }
}

tasks.named("sonar") {
    dependsOn("koverXmlReport", "detektMain")
}
