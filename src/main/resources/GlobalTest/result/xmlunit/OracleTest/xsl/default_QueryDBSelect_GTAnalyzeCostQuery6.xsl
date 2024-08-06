<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="xmlunit.xsl"/>
	<xsl:template match=" cost" mode="none">
		<xsl:value-of select="$EXIST"/>
	</xsl:template>
</xsl:stylesheet>
