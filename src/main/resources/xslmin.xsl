<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="no"/>
	
	<xsl:strip-space elements="*"/>
	
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<!-- This could probably be expanded to cover a larger number of attributes but for now it's moderately cautious. -->
	<xsl:template match="@elements | @extension-element-prefixes | @exclude-result-prefixes">
		<xsl:variable name="attrname">
			<xsl:value-of select="name(.)"/>
		</xsl:variable>
		<xsl:attribute name="{$attrname}">
			<xsl:value-of select="normalize-space(.)"/>
		</xsl:attribute>
	</xsl:template>
	
</xsl:stylesheet>