<?xml version="1.0"?>
<!--
	This is used in the xslmin junit tests.
	Basically don't mess around with existing parts of it.
	If you need a particular case tested that is not part of the existing XSLT
	then add new templates / global vars etc after existing ones.

	Note that there are some non-standard attributes added to the XSL. These are
	to help getting handles on specific elements for the tests.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:variable name="globalShadow" handle="def1">
		<xsl:value-of select="barfoo"/><!-- Must remain first global var -->
	</xsl:variable>
	<xsl:param name="globalParamShadow"><!-- Must remain first global param -->
		<xsl:value-of select="foobart"/>
	</xsl:param>

	<xsl:variable name="stripped" handle="gvStrippedDef">
		<!-- this call-template must remain before the actual template is found in the tree -->
		<xsl:call-template name="getStripped"/>
	</xsl:variable>

	<xsl:template name="sed"><!-- Must remain the FIRST template and must NOT have a match attribute -->
		<xsl:param name="localA" select="'localA'"/><!-- This must remain the first local param in the whole stylesheet-->
		<xsl:variable name="localB"><!-- This must remain the first local variable in the whole stylesheet-->
			<xsl:text>localB</xsl:text>
		</xsl:variable>
		<xsl:variable name="localAandB" select="concat($localA,$localB)"/>
		<xsl:value-of select="$localAandB"/>
	</xsl:template>

	<xsl:template match="/">
	  <xsl:element name="div">
	  	<xsl:variable name="local1">
	  		<xsl:text>Mr Local Variable</xsl:text>
	  	</xsl:variable>
		<xsl:variable name="globalShadow">
			<xsl:value-of select="foobar"/>
		</xsl:variable>
		<xsl:variable name="kungfu">
	  		<xsl:text>I know kungfu...</xsl:text>
	  	</xsl:variable>
		<xsl:value-of select="concat($local1, $globalShadow)" handle="noref1"/>
		<xsl:value-of select="$kungfu"/>
		<xsl:value-of select="$stripped"/>
	  </xsl:element>
	</xsl:template>

	<xsl:template name="fred" match="span">
		<xsl:param name="local1"/>
		<xsl:element name="bar">
			<xsl:attribute name="kung">fu</xsl:attribute>
			<xsl:attribute name="abc">
				<xsl:text>def</xsl:text>
			</xsl:attribute>
		</xsl:element>
		<div>
			<xsl:value-of select="concat($local1, $globalShadow)" handle="ref1"/>
		</div>
	</xsl:template>
	<!-- Uncomment this when it can handle narrower variable scope.
	<xsl:template name="tooManyVariables" match="stupidhead">
			<xsl:variable name="notDuplicate">
					<xsl:text>nodup</xsl:text>
			</xsl:variable>
			<xsl:if test="foo=bar">
				<xsl:variable name="duplicate">
						<xsl:text>first def</xsl:text>
				</xsl:variable>
				<xsl:value-of select="$duplicate"/>
			</xsl:if>
			<xsl:if test="bar=foo">
				<xsl:variable name="duplicate">
						<xsl:text>second def</xsl:text>
				</xsl:variable>
				<xsl:value-of select="$duplicate"/>
			</xsl:if>
		<div>
					<xsl:value-of select="$notDuplicate"/>
		</div>
	</xsl:template>
	-->
	<xsl:template name="ned" match="elephant" handle="tmpl1">
		<xsl:variable name="local2" handle="partialMatchA">
			<xsl:text>Mr Local Variable the Second</xsl:text>
		</xsl:variable>
		<xsl:variable name="local3">
			<xsl:text>Mr Local Variable the Third</xsl:text>
		</xsl:variable>
		<xsl:variable name="local2and3" select="concat($local2,$local3)"/>
		<xsl:call-template name="sed">
			<xsl:with-param name="localA">
				<xsl:value-of select="$local2and3" handle="partialMatchA"/><!-- The value in the select must not be changed, it must start with the name of a local variable above -->
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="foot">
		<xsl:variable name="tex">
			<xsl:text>ABCD</xsl:text>
		</xsl:variable>
	  	<xsl:variable name="mex">
			<xsl:text>EFGH</xsl:text>
		</xsl:variable>
		<xsl:variable name="tex-mex">
			<xsl:text>IJKL</xsl:text>
		</xsl:variable>
		<xsl:variable name="tex_mex">
			<xsl:text>IJKL</xsl:text>
		</xsl:variable>
		<xsl:variable name="mex_tex">
			<xsl:text>MNOP</xsl:text>
		</xsl:variable>
		<xsl:value-of select="concat($tex,$mex,$tex-mex,$tex_mex,$mex_tex)" handle="varMatchA"/>
		<xsl:value-of select="concat('$tex','$mex','$tex-mex','$tex','-mex','tex_mex','mex_tex','tex')" handle="varMatchB"/>
		<xsl:call-template name="fred">
			<xsl:with-param name="local1">
				<xsl:value-of select="$tex"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="div">
	  <xsl:element name="{$globalShadow}">
	  	<xsl:attribute name="fu">
	  		<xsl:text>kung</xsl:text>
	  	</xsl:attribute>
	  </xsl:element>
	</xsl:template>

	<xsl:template match="nose" handle="collapse1">
		<!--
			The attributes on this element are hardcoded as expected results in JUnit tests
			so be careful when changing anything.
		 -->
		<xsl:param name="aParam"/>
		<xsl:element name="mouth"><!-- This must remain the first xsl:element in this template -->
			<xsl:attribute name="alpha">
				<xsl:text>text</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="numeric">
				<xsl:text>0101</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="var">
				<xsl:value-of select="$aParam"/>
			</xsl:attribute>
			<xsl:attribute name="func">
				<xsl:value-of select="concat($aParam, '-suffix')"/>
			</xsl:attribute>
			<xsl:attribute name="xpath">
				<xsl:value-of select="@shape"/>
			</xsl:attribute>
			<xsl:attribute name="textNode">text</xsl:attribute>
			<xsl:attribute name="mix">
				txtNode-
				<xsl:text>text-</xsl:text>
				<xsl:value-of select="$aParam"/>
			</xsl:attribute>
			<xsl:attribute name="noCollapse1">
				<xsl:if test="@shape='pointy'">
					<xsl:text>haha</xsl:text>
				</xsl:if>
				<xsl:if test="@shape='flat'">
					<xsl:text>hoho</xsl:text>
				</xsl:if>
			</xsl:attribute>
			<xsl:if test="@shape='round'">
				<xsl:attribute name="button">
					<xsl:text>true</xsl:text>
				</xsl:attribute>
			</xsl:if>
		</xsl:element>
	</xsl:template>

	<xsl:template match="bottom">
		<xsl:param name="myTable"/>
		<xsl:call-template name="getStripped">
			<!--
				This call must occur before the apply-tremplates below.
				Must be to a template that contains a parameter with the same
				name as the with-param in apply-templates.
			-->
			<xsl:with-param name="myTable" select="$myTable"/>
		</xsl:call-template>
		<xsl:choose>
			<xsl:when test="*">
				<xsl:apply-templates>
					<xsl:with-param name="myTable" select="$myTable" handle="norename"/><!-- the name should never be renamed -->
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="makeHead">
			<xsl:element name="script">
				<xsl:value-of handle="gvStrippedRef" select="concat('defaultXslUrl:&#34;', normalize-space($stripped), '&#34;,&#xA;')"/>
		</xsl:element>
	</xsl:template>

	<xsl:template name="getStripped">
		<xsl:param name="myTable"/><!-- Don't rename this -->
		<xsl:text>Stripped</xsl:text>
	</xsl:template>

	<xsl:template name="replaceString">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text, $replace)">
                <xsl:value-of select="substring-before($text, $replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replaceString">
                    <xsl:with-param name="text" select="substring-after($text,$replace)"/>
                    <xsl:with-param name="replace" select="$replace"/>
                    <xsl:with-param name="with" select="$with"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

	<xsl:template name="notUsedButHasMatch" match="/fu/kung/test/icicles">
		<!--
			This template should not be stripped because it has a match attribute.
			DO NOT add any calls to this template or you invalidate the test.
		-->
		<xsl:variable name="varUnusedFuKungTestIcicles" handle="notused_plsremove">
			<xsl:text>This will be stripped because it is unused</xsl:text>
		</xsl:variable>
		<xsl:element name="car">
			<xsl:attribute name="fu">kung</xsl:attribute>
			<xsl:attribute name="def">
				<xsl:value-of select="$globalShadow"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!--
		It's lame I know, but to make the tests easier to write please ensure the templates
		that will be stripped (i.e. the unused templates) occur after any other templates.
	-->

	<xsl:template name="notused_plsremove" handle="notused_plsremove">
		<xsl:call-template name="makeHead"/>
	</xsl:template>

	<xsl:template name="notused_plsremove2" handle="notused_plsremove">
		<xsl:variable name="varRemoved2a" handle="notused_plsremove">
			<xsl:text>This will be removed when the parent template is removed</xsl:text>
		</xsl:variable>
		<xsl:element name="car">
			<xsl:attribute name="fu">kung</xsl:attribute>
			<xsl:attribute name="def">
				<xsl:value-of select="$varRemoved2a"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
