plugins {
	id("buildlogic.java-library-conventions")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

dependencies {
	implementation(platform(libs.flexmarkPlatform))
	api("com.vladsch.flexmark:flexmark")
	api("com.vladsch.flexmark:flexmark-ext-abbreviation")
	api("com.vladsch.flexmark:flexmark-ext-anchorlink")
	api("com.vladsch.flexmark:flexmark-ext-aside")
	api("com.vladsch.flexmark:flexmark-ext-autolink")
	api("com.vladsch.flexmark:flexmark-ext-definition")
	api("com.vladsch.flexmark:flexmark-ext-footnotes")
	api("com.vladsch.flexmark:flexmark-ext-gfm-strikethrough")
	api("com.vladsch.flexmark:flexmark-ext-gfm-tasklist")
	api("com.vladsch.flexmark:flexmark-ext-tables")
	api("com.vladsch.flexmark:flexmark-ext-toc")
	api("com.vladsch.flexmark:flexmark-ext-wikilink")
	api("com.vladsch.flexmark:flexmark-ext-yaml-front-matter")
}
