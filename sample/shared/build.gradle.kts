import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildkonfig)
}

// Load secrets from properties file
val secretsPropertiesFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()
if (secretsPropertiesFile.exists()) {
    secretsProperties.load(secretsPropertiesFile.inputStream())
}

kotlin {
    androidLibrary {
        namespace = "eu.buney.sample.shared"
        compileSdk = 36
        minSdk = 24

        // Enable Android resources for Compose resources support
        androidResources.enable = true

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
            baseName = "SampleApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Use local project dependency for development
            // implementation(libs.kmp.maps.compose)
            implementation(project(":kmp-maps-compose"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        androidMain.dependencies {
            // No Android-specific dependencies in shared module
        }
    }
}

buildkonfig {
    packageName = "eu.buney.sample"

    defaultConfigs {
        buildConfigField(STRING, "MAPS_API_KEY", secretsProperties.getProperty("MAPS_API_KEY", ""))
    }
}
