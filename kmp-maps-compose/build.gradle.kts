import io.github.frankois944.spmForKmp.swiftPackageConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.spmForKmp)
    alias(libs.plugins.vanniktechMavenPublish)
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_compiler_stability_config.conf"))
}

group = "eu.buney.maps"
version = libs.versions.kmp.maps.compose.get()

kotlin {
    // Suppress warnings about expect/actual classes being in Beta
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "eu.buney.maps"
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
            baseName = "MapsComposeMp"
            isStatic = true
        }

        iosTarget.swiftPackageConfig(cinteropName = "GoogleMapsBridge") {
            minIos = "17.0"
            dependency {
                remotePackageVersion(
                    url = uri("https://github.com/googlemaps/ios-maps-sdk"),
                    products = {
                        add("GoogleMaps", exportToKotlin = true)
                    },
                    version = libs.versions.google.maps.ios.get()
                )
            }
            // Export GoogleMaps package so iOS app can use it directly
            exportedPackageSettings {
                includeProduct = listOf("GoogleMaps")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.compose.components.resources)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.google.maps.compose)
            implementation(libs.play.services.maps)
        }

        iosMain.dependencies {
            // Google Maps iOS SDK via SPM
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "kmp-maps-compose", version.toString())

    pom {
        name = "Maps Compose Multiplatform"
        description = "Kotlin Compose Multiplatform library wrapping Google Maps for Android and iOS"
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
