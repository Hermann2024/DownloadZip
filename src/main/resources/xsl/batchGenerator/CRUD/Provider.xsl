<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="serviceName" />
  <xsl:param name="fileName" />

  <xsl:variable name="serviceNameShort">
  	<xsl:value-of select="substring-before($serviceName, 'ManagerService')"/>
  </xsl:variable>
 
   <xsl:template match="/serviceType">
<providersEdition>
     <xsl:apply-templates select="operation" />

  <provider type="FILEPROVIDER">
    <xsl:attribute name="name"><xsl:value-of select="concat('GTCheckDeleteList', $serviceNameShort, 'Extract')" /></xsl:attribute>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Providers_', $fileName)" /></xsl:attribute>
    <file>
      <xsl:attribute name="dir">${REPOSITORY}/Out</xsl:attribute>
      <xsl:attribute name="fileName"><xsl:value-of select="concat('${DATE}_GT', $serviceNameShort, '_CheckDeleteList', '${PROVIDER_IDX}.xml')" /></xsl:attribute>
    </file>
  </provider>

</providersEdition>
  </xsl:template>
 
  <xsl:template match="operation">
    <xsl:variable name="operation">
      <xsl:value-of select="concat(upper-case(substring(name, 1, 1)), substring(name, 2, string-length(name)))"/>
    </xsl:variable>
    <xsl:variable name="fileNameAttribute">
      <xsl:choose>
        <xsl:when test="ends-with($operation, 'List')">
          <xsl:value-of select="concat('GT', $serviceNameShort, '_', substring-before($operation, $serviceNameShort), 'List${PROVIDER_IDX}.xml')"/>
        </xsl:when>
        <xsl:when test="contains($operation, $serviceNameShort)">
          <xsl:value-of select="concat('GT', $serviceNameShort, '_', substring-before($operation, $serviceNameShort), '${PROVIDER_IDX}.xml')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat('GT', $serviceNameShort, '_', $operation, '${PROVIDER_IDX}.xml')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

  <provider type="FILEPROVIDER">
    <xsl:if test="ends-with($operation, 'List')">
      <xsl:attribute name="name"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort)" /></xsl:attribute>
    </xsl:if>
    <xsl:if test="not(ends-with($operation, 'List'))">
      <xsl:attribute name="name"><xsl:value-of select="concat('GT', $operation)" /></xsl:attribute>
    </xsl:if>
    <xsl:attribute name="fileName"><xsl:value-of select="concat('Providers_', $fileName)" /></xsl:attribute>
    <file>
      <xsl:attribute name="dir">${REPOSITORY}/In</xsl:attribute>
      <xsl:attribute name="fileName"><xsl:value-of select="$fileNameAttribute" /></xsl:attribute>
    </file>
  </provider>

  	<xsl:if test="starts-with($operation, 'Find') or starts-with($operation, 'Get')">
  <provider type="FILEPROVIDER">
      <xsl:attribute name="name"><xsl:value-of select="concat('GT', $operation, 'Extract')" /></xsl:attribute>
      <xsl:attribute name="fileName"><xsl:value-of select="concat('Providers_', $fileName)" /></xsl:attribute>
    <file>
      <xsl:attribute name="dir">${REPOSITORY}/Out</xsl:attribute>
      <xsl:attribute name="fileName"><xsl:value-of select="concat('${DATE}_', $fileNameAttribute)" /></xsl:attribute>
    </file>
  </provider>
    </xsl:if>
 
  	<xsl:if test="starts-with($operation, 'Update') or starts-with($operation, 'Create') or starts-with($operation, 'Post')">
  <provider type="FILEPROVIDER">
      <xsl:if test="ends-with($operation, 'List')">
        <xsl:attribute name="name"><xsl:value-of select="concat('GT', substring-before($operation, $serviceNameShort), 'List', $serviceNameShort, 'Reject')" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="not(ends-with($operation, 'List'))">
        <xsl:attribute name="name"><xsl:value-of select="concat('GT', $operation, 'Reject')" /></xsl:attribute>
      </xsl:if>
      <xsl:attribute name="fileName"><xsl:value-of select="concat('Providers_', $fileName)" /></xsl:attribute>
    <file>
      <xsl:attribute name="dir">${REPOSITORY}/KO</xsl:attribute>
      <xsl:attribute name="fileName"><xsl:value-of select="concat('${DATE}_', $fileNameAttribute)" /></xsl:attribute>
    </file>
  </provider>
  	</xsl:if>
  </xsl:template>

  <xsl:template match="text()"/>

  <xsl:template name="getOperationForFileName">
  </xsl:template>
</xsl:stylesheet>