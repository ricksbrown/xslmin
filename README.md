xslmin
======

An XSLT minifier

A tool to minify an XSL file so that it is as compact as possible (requires Java 1.6).

The primary goal of this tool is to compress XSL files used in client side XSLT in web browsers.

VERSION 2.0 RELEASED! The new version is major rewrite - the primary focus: SPEED!!! Some new features too, see below...

xslmin performs the following minification tasks:

New features in version 2:

Deletes unused templates
Deletes unused variables
Deletes unused parameters
And as always:

Strips comments
Strips ignorable whitespace
Renames local variables
Renames local parameters
Renames global variables
Does NOT rename global parameters (so you can still inject them)
Renames named templates
Collapses xsl:elements to short form e.g.
<element name="foo">
becomes
<foo>
Inlines xsl:attributes e.g.
<element name="foo">
<attribute name="bar">
<value-of select="$foobar">
</value-of>
</attribute>
becomes
<foo bar="{$foobar}">
It will, soon:

Merge identical templates
Detect recurring long text and move to global variable