plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.isel.ps"
version = "0.0.1-SNAPSHOT"
description = "secdash"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// oauth
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

	// jdbi / postgres
	implementation("org.jdbi:jdbi3-core:3.47.0")
	implementation("io.github.cdimascio:dotenv-kotlin:6.4.1") // read .env
	implementation("org.postgresql:postgresql:42.7.2")
	implementation("org.jdbi:jdbi3-kotlin:3.47.0")
	implementation("org.jdbi:jdbi3-postgres:3.47.0")
//	implementation("org.jdbi:jdbi3-spring5:3.47.0")
//	implementation("org.jdbi:jdbi3-kotlin-sqlobject:3.47.0")
//	implementation("org.postgresql:postgresql:42.7.2")

	// To use Kotlin specific date and time functions
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

	// To get password encode
	//api("org.springframework.security:spring-security-core:6.5.5")

	implementation("org.springframework.boot:spring-boot-starter-restclient")
	//implementation("org.springframework.boot:spring-boot-starter-security")
//	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
//	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	// OpenAPI / Swagger UI (v3.x line targets Spring Boot 4); enabled only in the dev profile
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	//runtimeOnly("org.postgresql:postgresql")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// WebTestClient on tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")

	// Mockito Kotlin DSL (whenever, any, etc.)
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Local runs via `./gradlew bootRun` activate the dev profile (Swagger UI on).
// The packaged jar (docker compose up -> java -jar app.jar) runs with no profile -> Swagger off.
tasks.bootRun {
	systemProperty("spring.profiles.active", "dev")
}
