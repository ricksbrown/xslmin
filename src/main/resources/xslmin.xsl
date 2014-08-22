<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="no"/>
	
	<xsl:strip-space  elements="*"/>
	
	<xsl:template match="*">
		<xsl:copy-of select="."/>
	</xsl:template>
	
</xsl:stylesheet>