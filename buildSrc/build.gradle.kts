plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
	gradlePluginPortal()
}

dependencies {
    implementation("com.github.jmongard:git-semver-plugin:0.12.10")
	implementation("org.openjfx:javafx-plugin:0.1.0")
}
