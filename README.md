# xslmin

## An XSLT minifier

The primary goal of this tool is to reduce the size of XSL files used in client side XSLT in web browsers.

xslmin performs the following minification tasks:


* Deletes unused templates
* Deletes unused variables
* Deletes unused parameters
* Strips comments
* Strips ignorable whitespace
* Renames local variables
* Renames local parameters
* Renames global variables
* Does NOT rename global parameters (so you can still inject them)
* Renames named templates
* Normalizes space in attribute token lists, e.g. `xsl:preserve-space`
* Collapses xsl:elements to short form e.g. `<element name="foo">` becomes `<foo>`
* Inlines xsl:attributes e.g.

	``` xml
	<element name="foo">
		<attribute name="bar">
			<value-of select="$foobar"></value-of>
		</attribute>
	</element>
	```

	becomes

	``` xml
	<foo bar="{$foobar}">
	```

## Usage
You can download the latest JAR from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ccom.github.ricksbrown.xslmin)

### Executable Jar
`java -jar xslmin.jar inputXslPath outputXslPath [-p[reserve]]`

### Ant Task
The easiest way is to use maven and the `maven-antrun-plugin` to fetch xslmin and then use it in your ant build like so:

``` xml
<path id="project.class.path">
	<path path="${maven.plugin.classpath}"/>
</path>

<taskdef name="xslmin"
	classname="com.github.ricksbrown.xslmin.ant.MinifyTask"
	classpathref="project.class.path"/>

<xslmin in="${srcdir}/myxsl.xsl" out="${outdir}/myxsl.xsl" preserve="true"/>
```

## Releases

2.3.0

* Existing users note, the java classes have been repacked to the github namespace `com.github.ricksbrown.xslmin`.
* Now available on Maven Central

	``` xml
	<dependency>
		<groupId>com.github.ricksbrown</groupId>
		<artifactId>xslmin</artifactId>
		<version>2.3.0</version>
	</dependency>
	```

#### TODO

* Merge identical templates
* Detect recurring long text and move to global variable