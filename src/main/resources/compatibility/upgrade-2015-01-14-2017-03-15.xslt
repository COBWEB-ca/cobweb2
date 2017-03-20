<xsl:stylesheet version="1.0"
	xmlns:cw="http://cobweb.ca/schema/cobweb2/config" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xslt" exclude-result-prefixes="xalan">

	<xsl:output method="xml" indent="yes" xalan:indent-amount="2"
		standalone="yes" />
	<xsl:strip-space elements="*" />

	<xsl:param name="cobweb-version" />

	<!-- Copy document as is -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@config-version[parent::cw:COBWEB2Config]">
		<xsl:attribute name="config-version">
			<xsl:value-of select="2017-03-15" />
		</xsl:attribute>
	</xsl:template>

	<!-- Delete shared weight matrix -->
	<xsl:template
		match="cw:ControllerConfig[@class='org.cobweb.cobweb2.impl.ai.LinearWeightsControllerParams']/cw:WeightMatrix">
	</xsl:template>

	<!-- Copy it to every every agent type -->
	<xsl:template
		match="cw:ControllerConfig[@class='org.cobweb.cobweb2.impl.ai.LinearWeightsControllerParams']/cw:AgentParams/cw:Agent">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
			<xsl:copy-of select="../../cw:WeightMatrix" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
