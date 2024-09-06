plugins {
	id("buildlogic.java-library-conventions")
}

dependencies {
	val flexmarkVersion = "0.64.8"
	api( "com.vladsch.flexmark:flexmark:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-abbreviation:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-anchorlink:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-aside:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-autolink:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-definition:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-footnotes:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-gfm-tasklist:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-tables:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-toc:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-wikilink:${flexmarkVersion}" )
	api( "com.vladsch.flexmark:flexmark-ext-yaml-front-matter:${flexmarkVersion}" )
}
