<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="serviceName" />
  <xsl:param name="fileName" />

  <xsl:variable name="serviceNameShort">
  	<xsl:value-of select="substring-before($serviceName, 'ManagerService')"/>
  </xsl:variable>
  <xsl:variable name="fileNameNoExt">
  	<xsl:value-of select="substring-before($fileName, '.xml')"/>
  </xsl:variable>
 
  <xsl:template match="/serviceType">
<batchesEdition>
  <batch>
    <xsl:attribute name="name"><xsl:value-of select="$fileNameNoExt" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Batchs_', $fileName)" /></xsl:attribute>
    <description><xsl:value-of select="$fileNameNoExt"/></description>
    <sequence>
      <batch>
        <xsl:attribute name="ref"><xsl:value-of select="concat($fileNameNoExt, 'Init')" /></xsl:attribute>
      </batch>
      <batch>
        <xsl:attribute name="ref"><xsl:value-of select="concat($fileNameNoExt, 'Treatment')" /></xsl:attribute>
      </batch>
      <batch>
        <xsl:attribute name="ref"><xsl:value-of select="concat($fileNameNoExt, 'Check')" /></xsl:attribute>
      </batch>
      <batch>
        <xsl:attribute name="ref"><xsl:value-of select="concat($fileNameNoExt, 'Clean')" /></xsl:attribute>
      </batch>
    </sequence>
  </batch>

  <batch>
    <xsl:attribute name="name"><xsl:value-of select="concat($fileNameNoExt, 'Init')" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Batchs_', $fileName)" /></xsl:attribute>
    <description>Initialisation du GlobalTest</description>
    <sequence>
      <xsl:apply-templates select="operation[starts-with(name, 'post') and ends-with(name, 'List')]" />
    </sequence>
  </batch>
  <batch>
    <xsl:attribute name="name"><xsl:value-of select="concat($fileNameNoExt, 'Treatment')" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Batchs_', $fileName)" /></xsl:attribute>
    <description>Traitement du GlobalTest</description>
    <sequence>
      <xsl:apply-templates select="operation[not(starts-with(name, 'get')) and not(starts-with(name, 'find'))]" />
    </sequence>
  </batch>
  <batch>
    <xsl:attribute name="name"><xsl:value-of select="concat($fileNameNoExt, 'Check')" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Batchs_', $fileName)" /></xsl:attribute>
    <description>Controle du GlobalTest</description>
    <sequence>
      <xsl:apply-templates select="operation[starts-with(name, 'get') or starts-with(name, 'find')]" />
    </sequence>
  </batch>
  <batch>
    <xsl:attribute name="name"><xsl:value-of select="concat($fileNameNoExt, 'Clean')" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Batchs_', $fileName)" /></xsl:attribute>
    <description>Nettoyage du GlobalTest</description>
    <sequence>
      <xsl:apply-templates select="operation[starts-with(name, 'delete') and ends-with(name, 'List')]" />
      <task description=" " activeDomain="Test1" defaultDomain="default">
        <xsl:attribute name="ref"><xsl:value-of select="concat('GTCheckDeleteList', $serviceNameShort)" /></xsl:attribute>
      </task>
    </sequence>
  </batch>
</batchesEdition>
  </xsl:template>

  <xsl:template match="operation">
    <xsl:variable name="operation">
      <xsl:value-of select="concat(upper-case(substring(name, 1, 1)), substring(name, 2, string-length(name)))"/>
    </xsl:variable>
      <task description=" " activeDomain="Test1" defaultDomain="default">
        <xsl:if test="ends-with($operation, 'List')">
          <xsl:attribute name="ref"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort)" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="not(ends-with($operation, 'List'))">
          <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
        </xsl:if>
      </task>
  </xsl:template>
</xsl:stylesheet>