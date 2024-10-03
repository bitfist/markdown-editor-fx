plugins {
	id("buildlogic.java-library-conventions")
	id("org.openjfx.javafxplugin")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

dependencies {
	api(libs.controlsFx)
	api(libs.fontawesomeFx)
	api(libs.miglayoutFx)
	api(libs.richTextFx)
}

javafx {
	version = "22.0.2"
	modules("javafx.controls")
}
