<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="serviceName" />
  <xsl:param name="fileName" />
  <xsl:param name="operationName" />

  <xsl:variable name="serviceNameShort">
  	<xsl:value-of select="substring-before($serviceName, 'ManagerService')"/>
  </xsl:variable>

  <xsl:template match="/serviceType">
<tasksEdition>
    <xsl:apply-templates select="operation" />

  <task type="EXTRACTION">
    <xsl:attribute name="name"><xsl:value-of select="concat('GTCheckDeleteList', $serviceNameShort)" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Tasks_', $fileName)" /></xsl:attribute>
    <processor>
      <xsl:attribute name="ref"><xsl:value-of select="concat('GTCheckDeleteList', $serviceNameShort)" /></xsl:attribute>
    </processor>
    <xsl:if test="operation[starts-with(name, 'find')]">
    <input>
      <provider>
        <xsl:attribute name="ref"><xsl:value-of select="concat('GTFind', $serviceNameShort)" /></xsl:attribute>
      </provider>
    </input>
    </xsl:if>
    <response>
      <provider>
        <xsl:attribute name="ref"><xsl:value-of select="concat('GTCheckDeleteList', $serviceNameShort, 'Extract')" /></xsl:attribute>
      </provider>
    </response>
    <maxFetchSize>0</maxFetchSize>
  </task>

</tasksEdition>
  </xsl:template>

  <xsl:template match="operation">
    <xsl:variable name="operation">
      <xsl:value-of select="concat(upper-case(substring(name, 1, 1)), substring(name, 2, string-length(name)))"/>
    </xsl:variable>

  	<xsl:if test="starts-with($operation, 'Find') or starts-with($operation, 'Get')">
  <task type="EXTRACTION">
    <xsl:attribute name="name"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Tasks_', $fileName)" /></xsl:attribute>
    <xsl:attribute name="operation"><xsl:value-of select="name" /></xsl:attribute>
    <processor>
      <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
    </processor>
    <input>
      <provider>
        <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
      </provider>
    </input>
    <response>
      <provider>
        <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation, 'Extract')" /></xsl:attribute>
      </provider>
    </response>
    <maxFetchSize>0</maxFetchSize>
  </task>
  	</xsl:if>

  	<xsl:if test="not(starts-with($operation, 'Find')) and not(starts-with($operation, 'Get'))">
  <task type="INTEGRATION">
    <xsl:if test="ends-with($operation, 'List')">
      <xsl:attribute name="name"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort)" /></xsl:attribute>
    </xsl:if>
    <xsl:if test="not(ends-with($operation, 'List'))">
      <xsl:attribute name="name"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
    </xsl:if>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Tasks_', $fileName)" /></xsl:attribute>
    <xsl:attribute name="operation"><xsl:value-of select="name" /></xsl:attribute>
    <processor>
      <xsl:if test="ends-with($operation, 'List')">
        <xsl:attribute name="ref"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort)" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="not(ends-with($operation, 'List'))">
        <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
      </xsl:if>
    </processor>
    <input>
      <provider>
        <xsl:if test="ends-with($operation, 'List')">
          <xsl:attribute name="ref"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort)" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="not(ends-with($operation, 'List'))">
          <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
        </xsl:if>
      </provider>
    </input>
  	<xsl:if test="starts-with($operation, 'Update') or starts-with($operation, 'Create') or starts-with($operation, 'Post')">
    <reject>
      <provider>
        <xsl:if test="ends-with($operation, 'List')">
          <xsl:attribute name="ref"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort, 'Reject')" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="not(ends-with($operation, 'List'))">
          <xsl:attribute name="ref"><xsl:value-of select="concat('GT', $operation, 'Reject')" /></xsl:attribute>
        </xsl:if>
      </provider>
    </reject>
    </xsl:if>
  </task>
  	</xsl:if>
  </xsl:template>

</xsl:stylesheet>