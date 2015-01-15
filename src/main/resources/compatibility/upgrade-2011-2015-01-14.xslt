<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xslt"
	exclude-result-prefixes="xalan"
>
	<xsl:output method="xml" indent="yes" xalan:indent-amount="2" standalone="yes" />
	<xsl:strip-space elements="*" />

	<xsl:param name="cobweb-version" />

	<!-- Copy document as is -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<!-- Fix up header -->
	<xsl:template match="inputData">
		<COBWEB2Config config-version="2015-01-14" cobweb-version="{$cobweb-version}">
			<xsl:apply-templates />
		</COBWEB2Config>
	</xsl:template>


	<!-- Fix up foodweb -->
	<xsl:template match="foodweb/*[substring(name(),1,5) = 'agent']">
		<agent id="{substring(name(),6)}">
			<xsl:apply-templates />
		</agent>
	</xsl:template>
	<xsl:template match="foodweb/*[substring(name(),1,4) = 'food']">
		<food id="{substring(name(),5)}">
			<xsl:apply-templates />
		</food>
	</xsl:template>


	<!-- Disease -->
	<xsl:template match="disease/agent/transmitTo/*">
		<agent id="{substring(name(),6)}">
			<xsl:apply-templates />
		</agent>
	</xsl:template>


	<!-- Abiotic -->
	<xsl:template match="Temperature/AgentParams/*">
		<Agent id="{substring(name(),6)}">
			<xsl:apply-templates />
		</Agent>
	</xsl:template>


	<!-- Fix up GeneticController StateSize map -->
	<xsl:template match="ControllerConfig//AgentParams/*">
		<Agent id="{position()}">
			<xsl:apply-templates />
		</Agent>
	</xsl:template>
	<xsl:template match="StateSize/*">
		<State Name="{Name/text()}">
			<xsl:apply-templates select="Size/node()" />
		</State>
	</xsl:template>


	<!-- Fix up GA -->
	<xsl:template match="ga/*[substring(name(),1,15) = 'linkedphenotype']">
		<linkedphenotype id="{substring(name(),16)}">
			<xsl:apply-templates />
		</linkedphenotype>
	</xsl:template>

	<xsl:key name="gene-table-agentId"
		match="*[substring(name(),1,5) = 'agent']"
		use="substring-before(substring-after(name(),'agent'), 'gene')" />

	<xsl:key name="gene-table-geneId"
		match="*[substring(name(),1,5) = 'agent']"
		use="substring-after(name(),'gene')" />

	<xsl:template match="ga">
		<xsl:copy>
			<!-- passthrough -->
			<xsl:apply-templates select="*[substring(name(),1,5) != 'agent']" />

			<!-- default gene values -->
			<xsl:for-each select="key('gene-table-geneId', 1)">
				<agent id="{substring-before(substring-after(name(), 'agent'), 'gene')}">
					<xsl:for-each select="key('gene-table-agentId', substring-before(substring-after(name(), 'agent'), 'gene'))">
					<gene id="{substring-after(name(), 'gene')}">
						<xsl:apply-templates />
					</gene>
					</xsl:for-each>
				</agent>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
