<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xalan="http://xml.apache.org/xslt"
		exclude-result-prefixes="xalan"
		>

	<xsl:output method="xml" indent="yes" xalan:indent-amount="2"/>
	<xsl:strip-space elements="*"/>

	<!-- Copy document as is -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<!-- Fix up foodweb -->
	<xsl:template match="*//foodweb/*[substring(name(),1,5) = 'agent']">
		<agent id="{position()}"><xsl:copy-of select="node()|@*"/></agent>
	</xsl:template>
	<xsl:template match="*//foodweb/*[substring(name(),1,4) = 'food']">
		<food id="{position()}"><xsl:copy-of select="node()|@*"/></food>
	</xsl:template>

</xsl:stylesheet>
