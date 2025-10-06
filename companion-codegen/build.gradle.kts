plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.maven.publish)
}

group = property.project.groupName
version = property.project.version

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}

dependencies {
    compileOnly(libs.ksp.api)

    ksp(libs.auto.service.ksp)

    implementation(libs.moshi.kotlin)
    implementation(libs.auto.service.annotations)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}