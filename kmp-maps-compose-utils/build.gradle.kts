import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechMavenPublish)
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

group = "eu.buney.maps"
version = libs.versions.kmp.maps.compose.get()

kotlin {
    androidLibrary {
        namespace = "eu.buney.maps.utils"
        compileSdk = 36
        minSdk = 24

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MapsComposeUtilsMp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp-maps-compose"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "kmp-maps-compose-utils", version.toString())

    pom {
        name = "Maps Compose Multiplatform - Utils"
        description = "Utilities (clustering, animation) for kmp-maps-compose"
        inceptionYear = "2025"
        url = "https://github.com/yankeppey/kmp-maps-compose"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "yankeppey"
                name = "Andrei Buneyeu"
                email = "yankeppey@gmail.com"
                url = "https://buney.eu"
            }
        }
        scm {
            url = "https://github.com/yankeppey/kmp-maps-compose"
            connection = "scm:git:git://github.com/yankeppey/kmp-maps-compose.git"
            developerConnection = "scm:git:ssh://git@github.com/yankeppey/kmp-maps-compose.git"
        }
    }
}
