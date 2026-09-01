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
                    "br.com.autorepairshop.accessidentity",
                    "br.com.autorepairshop.customer",
                    "br.com.autorepairshop.catalog",
                    "br.com.autorepairshop.inputmanagment",
                    "br.com.autorepairshop.serviceandexecution",
                    "br.com.autorepairshop.budget",
                    "br.com.autorepairshop.shared",
                    "br.com.autorepairshop.api",
                )
            }
            excludes {
                classes(
                    "*HashedPassword",
                    "*UserId",
                    "*CustomerInviteId",
                    "*RefreshSessionId",
                    "*CustomerId",
                    "*VehicleId",
                    "*ServiceOrderId",
                    "*ServiceId",
                    "*ExtraServiceId",
                    "*InventoryId",
                    "*PartId",
                    "*InventoryKind",
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
                    "*Column",
                    "*OpenApiConfig",
                    "*AutoRepairShopApplication",
                    "*JwtConfig",
                    "*MailSettings",
                    "*SecurityConfig",
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
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/dto/**",
                "**/*Entity.kt",
                "**/*JpaRepository.kt",
                "**/*Column.kt",
                "**/*Id.kt",
                "**/Role.kt",
                "**/*Status.kt",
                "**/*Type.kt",
                "**/ContactInfo.kt",
                "**/HashedPassword.kt",
                "**/InventoryKind.kt",
                "**/OpenApiConfig.kt",
                "**/AutoRepairShopApplication.kt",
                "**/JwtConfig.kt",
                "**/MailSettings.kt",
                "**/SecurityConfig.kt",
            ).joinToString(separator = ","),
        )
    }
}

tasks.named("sonar") {
    dependsOn("koverXmlReport", "detektMain")
}
