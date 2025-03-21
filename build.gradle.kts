plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
}

group = "com.na"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.43.0")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Spring Cloud GCP Starter (Core)
    implementation("com.google.cloud:spring-cloud-gcp-starter:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // 추가적으로 reactor-netty도 필요할 경우
    implementation("io.projectreactor.netty:reactor-netty-http")

    // GCP Storage Starter
    implementation("com.google.cloud:spring-cloud-gcp-starter-storage:3.4.4")

    // GCP Pub/Sub Starter
    implementation("com.google.cloud:spring-cloud-gcp-starter-pubsub:3.4.4")

    // GCP Logging (필요 시)
    implementation("com.google.cloud:google-cloud-logging:3.15.3")

    implementation("org.apache.pdfbox:pdfbox:2.0.29")
    implementation("org.apache.pdfbox:fontbox:2.0.29")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.29")

    // Jsoup HTML 파싱
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
//	implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-webflux:6.1.14")
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.90.Final:osx-aarch_64")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper")
    implementation("jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0") // 최신 JSTL API
    runtimeOnly("org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.0") // 구현체 추가



    implementation(files("src/main/resources/libs/OkCert3-java1.5-2.3.2.jar"))
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
