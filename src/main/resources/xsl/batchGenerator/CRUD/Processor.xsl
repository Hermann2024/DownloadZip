<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="serviceName" />
  <xsl:param name="fileName" />
  <xsl:param name="operationName" />

  <xsl:variable name="serviceNameShort">
  	<xsl:value-of select="substring-before($serviceName, 'ManagerService')"/>
  </xsl:variable>

  <xsl:template match="/serviceType">
<processorsEdition>
    <xsl:apply-templates select="operation" />
</processorsEdition>
  </xsl:template>
 
  <xsl:template match="operation">
    <xsl:variable name="operation">
      <xsl:value-of select="concat(upper-case(substring(name, 1, 1)), substring(name, 2, string-length(name)))"/>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:if test="ends-with($operation, 'List')">
        <xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort)" />
      </xsl:if>
      <xsl:if test="not(ends-with($operation, 'List'))">
        <xsl:value-of select="concat('GT', $operation)" />
      </xsl:if>
    </xsl:variable>

  <processor>
    <xsl:attribute name="name"><xsl:value-of select="$name" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Processors_', $fileName)" /></xsl:attribute>
    <variables>
      <variable name="response"/>
      <variable name="context">
        <value>
          <contextType>
            <businessUnit>${BUSINESS_UNIT}</businessUnit>
            <language>${LANGUAGE}</language>
          </contextType>
        </value>
      </variable>
    </variables>
    <block>
      <service>
        <xsl:attribute name="name"><xsl:value-of select="$name" /></xsl:attribute>
        <xsl:attribute name="operation"><xsl:value-of select="name" /></xsl:attribute>
        <xsl:attribute name="refservice"><xsl:value-of select="$serviceName" /></xsl:attribute>
        <params>
          <input/>
          <scenario/>
          <context variable="context"/>
        </params>
        <return variable="response"/>
      </service>
    </block>
    <output return="response"/>
  </processor>
  </xsl:template>

  <xsl:template match="text()"/>
</xsl:stylesheet>