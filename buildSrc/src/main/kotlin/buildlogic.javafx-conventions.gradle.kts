plugins {
	id("buildlogic.java-library-conventions")
	id("org.openjfx.javafxplugin")
}

dependencies {
	api("org.controlsfx:controlsfx:11.2.1")
	api("com.miglayout:miglayout-javafx:11.4")
	api( "org.fxmisc.richtext:richtextfx:0.11.3" )
}

javafx {
	version = "22.0.2"
	modules("javafx.controls")
}
