<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="utf-8"
		indent="yes" omit-xml-declaration="yes" />

	<!-- règle de suppression de la balise root -->
	<xsl:template match="/*">
		<xsl:apply-templates select="*" />
	</xsl:template>

	<!-- permet de supprimer les déclarations de namespace sur les balises (xmlns:ns2=...) -->
	<xsl:template match="*">
		<xsl:element name="{name()}" namespace="{namespace-uri()}">
			<xsl:apply-templates select="@* | node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:copy>
			<xsl:apply-templates select="*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/tasksEdition/task | /providersEdition/provider">
		<xsl:element name="{name()}" namespace="{namespace-uri()}">
			<xsl:attribute name="xsi:type" namespace="{namespace-uri()}"><xsl:value-of select='@type'/></xsl:attribute>
			<xsl:apply-templates select="@*[name() != 'type'] | node()" />
		</xsl:element>
	</xsl:template>

    <!-- permet de supprimer les attributs fileName -->
	<xsl:template match="*[name() != 'file']/@fileName" />

    <!-- permet de supprimer les processors template -->
    <xsl:template match="/processorsEdition/processor[@pattern = 'true']" />

	<xsl:template match="mandatory[text() = 'false']"/>

	<xsl:template match="mandatory[text() = 'true']">
	<xsl:comment> mandatory </xsl:comment>
	</xsl:template>

	<xsl:template match="comment">
	<xsl:text disable-output-escaping="yes">&lt;!--</xsl:text><xsl:value-of select="text()"/><xsl:text disable-output-escaping="yes">--&gt;</xsl:text>
	</xsl:template>

</xsl:stylesheet>
