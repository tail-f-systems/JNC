<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output encoding="UTF-8" method="html" indent="yes" 
	      doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/>

  <!-- When using <code>, whitespaces should be preserved -->
  <xsl:preserve-space elements="code"/>
  
  <!-- img tag -->
  <xsl:template match="imgold">
    <div class="image">
      <img src="pics/{@src}" height="{@height}"/>
    </div>
  </xsl:template>

  
  <!-- img tag -->
  <xsl:template match="img">
    <xsl:param name="chid"/>
    <xsl:variable name="imgnum"><xsl:number level="any" from="chapter" count="img"/></xsl:variable>
    <xsl:variable name="imgid">doc_chap<xsl:value-of select="$chid"/>_img<xsl:value-of select="$imgnum"/></xsl:variable>
    <a name="{$imgid}"/>

    <div class="img">
      <table>
	<tr>
	  <td>
	    <img src="pics/{@src}"/>
	  </td>
	</tr>

	<tr>
	  <td class="infohead">
	    <p class="caption">
	      <xsl:choose>
		<xsl:when test="@caption">
		  Picture: <xsl:if test="$chid"><xsl:value-of select="$chid"/>.</xsl:if>
		  <xsl:value-of select="$imgnum"/>: <xsl:value-of select="@caption"/>
		</xsl:when>
		<xsl:otherwise>
		  Picture: <xsl:value-of select="$chid"/>.<xsl:value-of select="$imgnum"/>
		</xsl:otherwise>
	      </xsl:choose>
	    </p>
	  </td>
	</tr>

      </table>
    </div>
  </xsl:template>

  
  <xsl:template match="guide">
    <html>
      <head>
	<link title="new" rel="stylesheet" href="css/stil.css" 
	      type="text/css"/>
	<link REL="shortcut icon" HREF="favicon.ico" TYPE="image/x-icon"/>
      </head>
      <body>
	<xsl:apply-templates/>
	<div class="footer">
	  <hr/>
	  <p>
	    Copyright 2005-2009 Tail-f Systems AB
	  </p>
	</div>
      </body>
    </html>
  </xsl:template>


  <xsl:template match="guide/title">
    <table>
      <tr>
	<td>
	  <h1>
	    <xsl:apply-templates/>
	  </h1>
	</td>
	<img src="pics/logo.png" align="right"/>
      </tr>
    </table>
  </xsl:template>


  <xsl:template match="guide/swversion">
    <div class="abstr"><p>ConfD version - <xsl:apply-templates/></p></div>
  </xsl:template>


  <xsl:template match="guide/productname">
  </xsl:template>

  <xsl:template match="guide/date">
    <div class="abstr"><p><xsl:apply-templates/></p></div>
  </xsl:template>

  <xsl:template match="guide/author">
  </xsl:template>


  <!-- simple code fragment not to use inside <code> -->
  <xsl:template match="pre">
    <pre><span class="code"><xsl:apply-templates/></span></pre>
  </xsl:template>

  <!-- used for e.g. filenames and xml elements and attributes in
  running text -->
  <xsl:template match="c">
    <code><xsl:apply-templates/></code>
  </xsl:template>
 
  <xsl:template match="p">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="br">
    <br />
  </xsl:template>

  <xsl:template match="em">
    <span class="emphasis"><xsl:apply-templates/></span>
  </xsl:template>


  <!-- E-mail address -->
  <xsl:template match="mail">
    <a href="mailto:{@link}"><xsl:value-of select="."/></a>
  </xsl:template>
  

  <!-- Unnumbered List -->
  <xsl:template match="ul">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>
  
  <!-- Ordered List -->
  <xsl:template match="ol">
    <ol>
      <xsl:apply-templates/>
    </ol>
  </xsl:template>
  
  <!-- List Item -->
  <xsl:template match="li">
    <li>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  
  <!-- List Item -->
  <xsl:template match="dl">
    <div class="deflist">
      <dl>
	<xsl:apply-templates/>
      </dl>
    </div>
  </xsl:template>
  

  
  <!-- List Item -->
  <xsl:template match="dt">
    <dt>
      <xsl:apply-templates/>
    </dt>
  </xsl:template>


  
  <!-- List Item -->
  <xsl:template match="dd">
    <dd>
      <xsl:apply-templates/>
    </dd>
  </xsl:template>

  
  
  <!-- Tables -->
  <!-- See userguide-fo.xsl for explanation of the table tags -->
  <xsl:attribute-set name="th">
    <xsl:attribute name="align">center</xsl:attribute>
    <xsl:attribute name="valign">top</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="td">
    <xsl:attribute name="align">left</xsl:attribute>
    <xsl:attribute name="valign">top</xsl:attribute>
  </xsl:attribute-set>

  <xsl:template match="table">
    <table border="1">
      <xsl:if test="@width">
        <xsl:attribute name="width">
          <xsl:value-of select="@width"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </table>
  </xsl:template>
 
  <xsl:template match="col">
    <col width="{@width}"/>
  </xsl:template>

  <xsl:template match="tbody">
    <tbody>
      <xsl:apply-templates/>
    </tbody>
  </xsl:template>

  <xsl:template match="thead">
    <thead>
      <xsl:apply-templates/>
    </thead>
  </xsl:template>

  <xsl:template match="tfoot">
    <tfoot>
      <xsl:apply-templates/>
    </tfoot>
  </xsl:template>

  <xsl:template match="tr">
    <tr>
      <xsl:if test="@height">
        <xsl:attribute name="height">
          <xsl:value-of select="@height"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="th">
    <th xsl:use-attribute-sets="th">
      <xsl:call-template name="cell-span"/>
      <xsl:call-template name="cell-align"/>
      <xsl:apply-templates/>
    </th>
  </xsl:template>

  <xsl:template match="td">
    <td xsl:use-attribute-sets="td">
      <xsl:call-template name="cell-span"/>
      <xsl:call-template name="cell-align"/>
      <xsl:apply-templates/>
    </td>
  </xsl:template>

  <xsl:template name="cell-span">
    <xsl:if test="@colspan">
      <xsl:attribute name="colspan">
        <xsl:value-of select="@colspan"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@rowspan">
      <xsl:attribute name="rowspan">
        <xsl:value-of select="@rowspan"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="cell-align">
    <xsl:if test="@align">
      <xsl:attribute name="align">
        <xsl:value-of select="@align"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@valign">
      <xsl:attribute name="valign">
        <xsl:value-of select="@valign"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>



  <xsl:template match="chapter/title">
    <h2>
      <xsl:apply-templates/>
    </h2>
  </xsl:template>
  


  <xsl:template match="section/title">
    <h3>
      <xsl:apply-templates/>
    </h3>
  </xsl:template>


  <xsl:template match="subsection/title">
    <h4>
      <xsl:apply-templates/>
    </h4>
  </xsl:template>


  <xsl:template match="b | strong">
    <b>
      <xsl:apply-templates/>
    </b>
  </xsl:template>




<xsl:template match="code">
<xsl:param name="chid"/>
<xsl:variable name="codenum"><xsl:number level="any" from="chapter" count="code"/></xsl:variable>
<xsl:variable name="codeid">doc_chap<xsl:value-of select="$chid"/>_code<xsl:value-of select="$codenum"/></xsl:variable>
<a name="{$codeid}"/>
<table class="ntable" width="100%" cellspacing="0" cellpadding="0" border="0">

  <tr>
    <td>
      <div class="prebox">
	<pre>
	  <xsl:apply-templates/>
	</pre>
      </div>
    </td>
  </tr>


  <tr>
    <td class="infohead">
      <p class="caption">
        <xsl:choose>
          <xsl:when test="@caption">
	    Code listing <xsl:if test="$chid"><xsl:value-of select="$chid"/>.</xsl:if>
	    <xsl:value-of select="$codenum"/>: <xsl:value-of select="@caption"/>
          </xsl:when>
          <xsl:otherwise>
            Code listing <xsl:value-of select="$chid"/>.<xsl:value-of select="$codenum"/>
          </xsl:otherwise>
        </xsl:choose>
      </p>
    </td>
  </tr>


</table>
</xsl:template>

<!-- Emphasize -->
<xsl:template match="e | i">
  <span class="emphasis"><xsl:apply-templates/></span>
</xsl:template>


  <xsl:template match="pre">
    <div class="box">
      <pre>
	<xsl:apply-templates/>
      </pre>
    </div>
  </xsl:template>


  <xsl:template match="uri">
    <a href="{@link}"><xsl:apply-templates /></a>
  </xsl:template>



</xsl:stylesheet>
