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
	<xsl:template match="inputData|inputeData">
		<COBWEB2Config config-version="2015-01-14" cobweb-version="{$cobweb-version}">
			<xsl:apply-templates select="*[name() != 'production' and name() != 'food' and name() != 'agent']" />
			<Agents>
				<AgentParams>
					<xsl:for-each select="/*/agent">
						<Agent id="{Index + 1}">
							<xsl:apply-templates />
						</Agent>
					</xsl:for-each>
				</AgentParams>
			</Agents>
			<FoodGrowth>
				<FoodParams>
					<xsl:for-each select="/*/food">
						<Food id="{Index + 1}">
							<xsl:apply-templates />
						</Food>
					</xsl:for-each>
				</FoodParams>
			</FoodGrowth>
			<!-- Move waste properties under own section -->
			<Waste>
				<AgentParams>
					<xsl:for-each select="/*/agent">
						<Agent id="{Index + 1}">
							<xsl:apply-templates select="*[starts-with(name(), 'waste')]" />
						</Agent>
					</xsl:for-each>
				</AgentParams>
			</Waste>
			<!-- Move PD params -->
			<PrisonersDilemma>
				<enable><xsl:value-of select="PrisDilemma/text()" /></enable>
				<xsl:apply-templates select="pd/*" />
				<AgentParams>
					<xsl:for-each select="/*/agent">
						<Agent id="{Index + 1}">
							<xsl:apply-templates select="*[starts-with(name(), 'pd')]" />
						</Agent>
					</xsl:for-each>
				</AgentParams>
			</PrisonersDilemma>
			<!-- Move production agent params under Production section -->
			<Production>
				<AgentParams>
					<xsl:for-each select="/*/production">
						<Agent id="{Index + 1}">
							<xsl:apply-templates />
						</Agent>
					</xsl:for-each>
				</AgentParams>
			</Production>
		</COBWEB2Config>
	</xsl:template>

	<!-- Move variables from agent config -->
	<xsl:template match="/*/agent">
		<xsl:copy>
			<xsl:apply-templates select="*[name() != 'MemoryBits' and name() != 'communicationBits' and not(starts-with(name(), 'waste')) and not(starts-with(name(), 'pd'))]" />
		</xsl:copy>
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
	<xsl:template match="disease">
		<Disease>
			<AgentParams>
				<xsl:for-each select="agent">
					<Agent id="{Index + 1}">
						<xsl:apply-templates select="node()|@*"/>
					</Agent>
				</xsl:for-each>
			</AgentParams>
		</Disease>
	</xsl:template>

	<xsl:template match="transmitTo/*">
		<Agent id="{substring(name(),6)}">
			<xsl:apply-templates />
		</Agent>
	</xsl:template>


	<!-- Abiotic -->
	<xsl:template match="Temperature">
		<Abiotic>
			<Factors>
				<Factor class="org.cobweb.cobweb2.plugins.abiotic.HorizontalBands" id="1">
					<Bands>
						<xsl:for-each select="TempBands/*">
							<Band id="{substring-after(name(),'Band')}">
								<xsl:apply-templates />
							</Band>
						</xsl:for-each>
					</Bands>
				</Factor>
			</Factors>
			<AgentParams>
				<xsl:for-each select="AgentParams/*">
					<Agent id="{substring(name(),6)}">
						<FactorParams>
							<Factor id="1">
								<xsl:apply-templates select="node()|@*" />
							</Factor>
						</FactorParams>
					</Agent>
				</xsl:for-each>
			</AgentParams>
		</Abiotic>
	</xsl:template>
	<xsl:template match="PreferedTemp">
		<PreferedValue>
			<xsl:apply-templates select="node()|@*" />
		</PreferedValue>
	</xsl:template>
	<xsl:template match="PreferedTempRange">
		<PreferedRange>
			<xsl:apply-templates select="node()|@*" />
		</PreferedRange>
	</xsl:template>

	<!-- Fix up GeneticController StateSize map -->
	<xsl:template match="ControllerConfig/PluginParams">
		<xsl:apply-templates select="node()|@*"/>
	</xsl:template>
	<xsl:template match="ControllerConfig/RandomSeed" /><!-- moved under agent -->
	<xsl:template match="ControllerConfig//AgentParams/*[substring(name(),1,5) = 'Agent']">
		<xsl:variable name="agentIndex" select="substring-after(name(),'Agent') - 1" />
		<xsl:variable name="agentParams" select="/inputData/agent[./Index/text() = $agentIndex]" />
		<Agent id="{$agentIndex + 1}">
			<RandomSeed>
				<xsl:value-of select="./../../../RandomSeed" />
			</RandomSeed>
			<MemoryBits>
				<xsl:value-of select="$agentParams/MemoryBits" />
			</MemoryBits>
			<CommunicationBits>
				<xsl:value-of select="$agentParams/communicationBits" />
			</CommunicationBits>
			<xsl:apply-templates />
		</Agent>
	</xsl:template>
	<xsl:template match="StateSize/*">
		<State>
			<xsl:attribute name="Name">
				<xsl:apply-templates select="Name/text()" />
			</xsl:attribute>
			<xsl:apply-templates select="Size/node()" />
		</State>
	</xsl:template>
	<xsl:template match="*[text() = 'ProdHunt']/text()">
		<xsl:text>Production Value</xsl:text>
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
			<AgentParams>
				<xsl:for-each select="key('gene-table-geneId', 1)">
					<Agent id="{substring-before(substring-after(name(), 'agent'), 'gene')}">
						<xsl:for-each select="key('gene-table-agentId', substring-before(substring-after(name(), 'agent'), 'gene'))">
						<Gene id="{substring-after(name(), 'gene')}">
							<xsl:call-template name="bin2int">
								<xsl:with-param name="binaryString">
									<xsl:apply-templates />
								</xsl:with-param>
							</xsl:call-template>
						</Gene>
						</xsl:for-each>
					</Agent>
				</xsl:for-each>
			</AgentParams>
		</xsl:copy>
	</xsl:template>


	<!-- LinearWeightsController -->
	<xsl:template match="ControllerConfig[./inp]">
		<xsl:copy>
			<WeightMatrix>
				<xsl:apply-templates select="inp" />
			</WeightMatrix>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="bin2int">
		<xsl:param name="binaryString"/>
		<xsl:param name="integer" select="0"/>

		<xsl:choose>
			<xsl:when test="$binaryString=''">
				<xsl:value-of select="$integer"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="bin2int">
				<xsl:with-param name="binaryString" select="substring($binaryString,2)"/>
				<xsl:with-param name="integer" select="2*$integer + number(substring($binaryString,1,1))"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
