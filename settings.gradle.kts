enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("com.highcapable.sweetproperty") version "1.0.8"
}

sweetProperty {
    global {
        sourcesCode {
            includeKeys(
                "^project\\..*\$".toRegex(),
                "^gradle\\..*\$".toRegex()
            )
            isEnableRestrictedAccess = true
        }
    }

    rootProject {
        all {
            isEnable = false
        }
    }
}

rootProject.name = "moshi-companion"

include(":samples:app")
include(":companion-api", ":companion-codegen")