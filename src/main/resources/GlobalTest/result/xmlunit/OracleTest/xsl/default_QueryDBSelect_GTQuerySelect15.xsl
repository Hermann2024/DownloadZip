<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="xmlunit.xsl"/>
	<xsl:template match="/queryResultType/values[valueField6 and valueField22]">
		<xsl:apply-templates select="*[./text()!=''][1]" mode="custom"/>
		<xsl:apply-templates select="*//*[./text()!=''][1]" mode="custom"/>
	</xsl:template>

	<xsl:template name="getXPathCustom">
		<xsl:for-each select="ancestor::*">
			<xsl:variable name="tagNameCust" select="name()"/>
			<xsl:variable name="xpathCustom0">
				<xsl:call-template name="getXPathCustom0"/>
			</xsl:variable>
			<xsl:if test="$xpathCustom0!='NULL'">
				<xsl:value-of select="$xpathCustom0"/>
			</xsl:if>
			<xsl:if test="$xpathCustom0='NULL'">
				<xsl:value-of select="concat('/', name(), '[', count(preceding-sibling::node()[name()=$tagNameCust])+1, ']')" />
			</xsl:if>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="getXPathCustom0">
		<xsl:choose>
			<xsl:when test="name()='values' and name(..)='queryResultType' and valueField6 and valueField22">
				<xsl:value-of select="concat('/', name(), '[valueField6=',$QUOTES,valueField6,$QUOTES,'and valueField22=',$QUOTES,valueField22,$QUOTES,']')" />
			</xsl:when>
			<xsl:otherwise>NULL</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match=" valueField2 | valueField3 | valueField13 | valueField15 | valueField25" mode="none">
		<xsl:value-of select="$EXIST"/>
	</xsl:template>
</xsl:stylesheet>
