plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
	gradlePluginPortal()
}

dependencies {
	implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location)) // load TOML
	implementation(libs.semanticVersioning)
	implementation(libs.javaFxPlugin)
}
