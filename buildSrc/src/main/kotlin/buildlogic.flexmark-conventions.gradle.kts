plugins {
	id("buildlogic.java-library-conventions")
}

dependencies {
	val flexmarkVersion = "0.64.8"
	implementation( "com.vladsch.flexmark:flexmark:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-abbreviation:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-anchorlink:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-aside:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-autolink:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-definition:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-footnotes:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-gfm-tasklist:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-tables:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-toc:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-wikilink:${flexmarkVersion}" )
	implementation( "com.vladsch.flexmark:flexmark-ext-yaml-front-matter:${flexmarkVersion}" )
}
