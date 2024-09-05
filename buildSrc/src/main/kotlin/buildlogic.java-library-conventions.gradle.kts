plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
    mavenLocal()
}

java.sourceCompatibility = JavaVersion.VERSION_21

dependencies {
	testImplementation("org.mockito:mockito-core:5.12.0")
	testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}
