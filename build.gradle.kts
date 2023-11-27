plugins {
    java
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.liquibase.gradle") version "2.2.0"
}

group = "ru.nechaev"
version = "0.0.1-SNAPSHOT"

val hibernateEnversVersion = "6.3.1.Final"
val amazonAwsBomVersion = "2.21.1"
val openAiWebMvcVersion = "2.2.0"
val commonsCodecVersion = "1.15"
val springBootStarterParentVersion = "3.1.5"
val nimbusJoseJwtVersion = "9.31"
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusJoseJwtVersion")
    implementation("org.liquibase:liquibase-core")
    implementation("org.hibernate.orm:hibernate-envers:$hibernateEnversVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openAiWebMvcVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(platform("software.amazon.awssdk:bom:$amazonAwsBomVersion"))
    implementation("software.amazon.awssdk:s3")
    implementation("commons-codec:commons-codec:$commonsCodecVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-parent:$springBootStarterParentVersion")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
}
