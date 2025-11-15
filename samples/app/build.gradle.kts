plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = gropify.project.samples.app.packageName
    compileSdk = gropify.project.android.compileSdk

    defaultConfig {
        applicationId = gropify.project.samples.app.packageName
        minSdk = gropify.project.android.minSdk
        targetSdk = gropify.project.android.targetSdk
        versionCode = gropify.project.samples.app.versionCode
        versionName = gropify.project.samples.app.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint { checkReleaseBuilds = false }
}

ksp {
    // Proguard rules that need to be generated with codegen
    // are disabled and handed over to companion-codegen for processing.
    arg("moshi.generateProguardRules", "false")

    arg("moshi-companion.generateAdapterRegistryPackageName", android.namespace!!)
    arg("moshi-companion.generateAdapterRegistryClassName", "SampleAdapterRegistry")
}

dependencies {
    implementation(projects.companionApi)

    implementation(libs.moshi.kotlin)

    // Moshi's codegen must be ensured to be executed before.
    ksp(libs.moshi.kotlin.codegen)
    ksp(projects.companionCodegen)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}