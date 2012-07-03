<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output encoding="UTF-8" method="xml" 
	      indent="yes" />



  <xsl:template match="title">
  </xsl:template>

  <xsl:template match="productname">
  </xsl:template>

  <xsl:template match="swversion">
  </xsl:template>

  <xsl:template match="date">
  </xsl:template>

  <xsl:template match="author">
  </xsl:template>
  <xsl:template match="abstract">
  </xsl:template>



  <xsl:template match="chapter">
    <chapter><xsl:apply-templates/></chapter>
  </xsl:template>

  <xsl:template match="chapter/title">
    <title><xsl:apply-templates/></title>
  </xsl:template>

  <xsl:template match="section/title">
    <title><xsl:apply-templates/></title>
  </xsl:template>

  
  <xsl:template match="section">
    <section><xsl:apply-templates/></section>
  </xsl:template>

  <xsl:template match="body">
    <body><xsl:apply-templates/></body>
  </xsl:template>


  <xsl:template match="p">
    <p><xsl:apply-templates/></p>
  </xsl:template>


  <xsl:template match="pre">
    <pre><xsl:apply-templates/></pre>
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates/></code>
  </xsl:template>

  <xsl:template match="ul">
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <xsl:template match="li">
    <li><xsl:apply-templates/></li>
  </xsl:template>

  <xsl:template match="ol">
    <ol><xsl:apply-templates/></ol>
  </xsl:template>


  <xsl:template match="img">
    <img src="{@src}" height="{@height}"></img>
  </xsl:template>




</xsl:stylesheet>
