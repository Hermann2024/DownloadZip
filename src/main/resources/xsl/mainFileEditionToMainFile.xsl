<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="utf-8"
		indent="yes" omit-xml-declaration="no"/>

	<xsl:template name="getDocType">
		<xsl:param name="rootTagname"/>
		<xsl:text disable-output-escaping='yes'>&lt;</xsl:text>!DOCTYPE <xsl:value-of select="$rootTagname" />[<xsl:for-each select="propertiesEdition/includeXML">
			<xsl:text disable-output-escaping='yes'>
	&lt;!ENTITY </xsl:text><xsl:value-of select="substring-before(@name, '.xml')" /> SYSTEM "<xsl:value-of select="@name" /><xsl:text disable-output-escaping='yes'>"&gt;</xsl:text>
		</xsl:for-each>
		<xsl:for-each select="includeXML">
			<xsl:text disable-output-escaping='yes'>
	&lt;!ENTITY </xsl:text><xsl:value-of select="substring-before(@name, '.xml')" /> SYSTEM "<xsl:value-of select="@name" /><xsl:text disable-output-escaping='yes'>"&gt;</xsl:text>
		</xsl:for-each>
		<xsl:text disable-output-escaping='yes'>
]&gt;
</xsl:text>
	</xsl:template>

	<xsl:template match="/batchesEdition">
		<xsl:text disable-output-escaping="yes"><![CDATA[<?xml-stylesheet type="text/xsl" href="Batchs.xsl"?>]]>
</xsl:text>
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">batchs</xsl:with-param>
		</xsl:call-template>
		<batchs>
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</batchs>
	</xsl:template>

	<xsl:template match="/tasksEdition">
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">tasks</xsl:with-param>
		</xsl:call-template>
		<tasks xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</tasks>
	</xsl:template>

	<xsl:template match="/providersEdition">
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">providers</xsl:with-param>
		</xsl:call-template>
		<providers xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</providers>
	</xsl:template>

	<xsl:template match="/triggersEdition">
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">triggers</xsl:with-param>
		</xsl:call-template>
		<triggers xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</triggers>
	</xsl:template>

	<xsl:template match="/mappersEdition">
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">mappers</xsl:with-param>
		</xsl:call-template>
		<mappers>
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</mappers>
	</xsl:template>

	<xsl:template match="/stylesheetsEdition">
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">stylesheets</xsl:with-param>
		</xsl:call-template>
		<stylesheets>
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</stylesheets>
	</xsl:template>

	<xsl:template match="/processorsEdition">
		<xsl:call-template name="getDocType">
			<xsl:with-param name="rootTagname">processors</xsl:with-param>
		</xsl:call-template>
		<processors>
		<xsl:text disable-output-escaping='yes'>
</xsl:text>
			<xsl:apply-templates select="@* | node()" />
		</processors>
	</xsl:template>

	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="includeXML">
		<xsl:text disable-output-escaping='yes'>	&amp;</xsl:text><xsl:value-of select="substring-before(@name, '.xml')" />;
</xsl:template>

	<xsl:template match="propertiesEdition/includeXML">
		<xsl:text disable-output-escaping='yes'>	&amp;</xsl:text><xsl:value-of select="substring-before(@name, '.xml')" />;</xsl:template>

	<xsl:template match="propertiesEdition">
		<xsl:text disable-output-escaping="yes">
	<![CDATA[<properties>]]></xsl:text>
			<xsl:apply-templates select="@* | node()" />
		<xsl:text disable-output-escaping="yes">
	<![CDATA[</properties>]]>
</xsl:text>
	</xsl:template>
	
	<xsl:template match="projectDescription" />
</xsl:stylesheet>
