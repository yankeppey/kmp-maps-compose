import io.github.frankois944.spmForKmp.swiftPackageConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

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
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_stability_config.conf"))
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
            api(libs.google.maps.compose)
            implementation(libs.play.services.maps)
        }

        iosMain.dependencies {
            // Google Maps iOS SDK via SPM
        }
    }
}

// spm4Kmp writes the absolute Swift package build directory of the machine running cinterop straight
// into the generated .def file, and cinterop copies those options verbatim into the klib manifest. On
// a consumer's machine the paths do not exist, so every release so far shipped manifests carrying
// `-I/-L/-F "/Users/runner/work/..."` and `"/Applications/Xcode_<version>.app/..."`, which surface as
// confusing `ld: warning: search path ... not found` lines. Strip them so the published klibs are
// relocatable: the Swift bridge archive is embedded inside the klib itself (default/targets/<target>/
// included/libGoogleMapsBridge.a), and none of the removed options contribute an actual link input.
// See https://github.com/yankeppey/kmp-maps-compose/issues/10
val sanitizeKlibManifests =
    providers.gradleProperty("kmpMaps.sanitizeKlibManifests").map(String::toBoolean).getOrElse(true)

tasks.withType<CInteropProcess>().configureEach {
    val klib = klibOutput
    doLast {
        if (sanitizeKlibManifests) stripBuildMachinePaths(klib.get())
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

val klibManifestEntry = "default/manifest"

/** Matches a `-I`, `-L` or `-F` option pointing at an absolute path, quoted or bare. */
val absolutePathOption = Regex("""\s*-[ILF]\s*(?:"/[^"]*"|/\S+)""")

/** Rewrites the manifest of [klib] in place, dropping every build-machine-specific path. */
fun stripBuildMachinePaths(klib: File) {
    if (klib.isDirectory) {
        val manifest = klib.resolve(klibManifestEntry)
        manifest.writeText(sanitizeKlibManifest(manifest.readText()))
        return
    }

    val rewritten = File.createTempFile(klib.nameWithoutExtension, ".klib", klib.parentFile)
    ZipFile(klib).use { zip ->
        ZipOutputStream(rewritten.outputStream().buffered()).use { out ->
            for (entry in zip.entries()) {
                out.putNextEntry(ZipEntry(entry.name))
                if (entry.name == klibManifestEntry) {
                    val manifest = zip.getInputStream(entry).use { it.readBytes() }.decodeToString()
                    out.write(sanitizeKlibManifest(manifest).toByteArray())
                } else {
                    zip.getInputStream(entry).use { it.copyTo(out) }
                }
                out.closeEntry()
            }
        }
    }
    Files.move(rewritten.toPath(), klib.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

fun sanitizeKlibManifest(manifest: String): String =
    manifest
        .lineSequence()
        .filterNot { it.startsWith("libraryPaths=") }
        .map { line ->
            if (line.startsWith("compilerOpts=") || line.startsWith("linkerOpts=")) {
                line.replace(absolutePathOption, "").trimEnd()
            } else {
                line
            }
        }
        .joinToString("\n")
