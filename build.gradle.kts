plugins {
	id("buildlogic.javafx-conventions")
	id("buildlogic.flexmark-conventions")
	id("buildlogic.version-conventions")
}

group = "io.github.bitfist"
description = "Markdown Editor component for JavaFX"

dependencies {
	implementation("org.apache.commons:commons-lang3:3.16.0")
	implementation("de.jensd:fontawesomefx-fontawesome:4.7.0-9.1.2")
}
