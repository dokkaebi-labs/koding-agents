plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
    idea
}

group = "com.agent"
version = "1.0.0"

dependencies {
    // Koog 프레임워크 (AI 에이전트 구축)
    implementation(libs.koog.agents)
    implementation(libs.koog.tools)
    implementation(libs.koog.anthropic)

    // kotlinx-serialization (JSON 처 리)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.cli)

    // JLine3 (Interactive terminal UI)
    implementation(libs.jline)

    // 로깅
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // 테스트
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}