<?xml version="1.0" encoding="UTF-8"?>

<!--
  - $Id$
  -
  - userguide-fo.xsl transforms Tail-f user guides from XML into XSL-FO (which
  - can be rendered on various print targets, e.g. PDF, by an XSL-FO processor,
  - e.g. http://xmlgraphics.apache.org/fop).
  -
  -->


<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output indent="yes"/>

  <!-- When using <code>, whitespaces should be preserved -->
  <xsl:preserve-space elements="code"/>


  <!-- Fixed strings -->
  <xsl:variable name="companyname">Tail-f Systems</xsl:variable>
  <xsl:variable name="copyright">Copyright &#169; 2005-2009</xsl:variable>
  <!-- FIXME: remove when appendix creation has been fixed -->
  <xsl:variable name="appendix_title">
      <xsl:text>Appendix: Unix man pages for ConfD</xsl:text>
  </xsl:variable>

  <!-- Font size (all other font sizes should be proportional to this -->
  <xsl:param name="base-font-size">10pt</xsl:param>

  <!-- Paper size: A4 (297x210 mm) -->
  <xsl:param name="page-height">297mm</xsl:param>
  <xsl:param name="page-width">210mm</xsl:param>

  <!-- Paper size: US Letter (279x216 mm) -->
  <!-- 
  <xsl:param name="page-height">11in</xsl:param>
  <xsl:param name="page-width">8.5in</xsl:param>
  -->


  <!-- XSL-FO properties -->
  <xsl:attribute-set name="caption">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">0.8em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="keep-with-previous.within-page">always</xsl:attribute>
    <xsl:attribute name="space-after">2.5em</xsl:attribute>
    <xsl:attribute name="space-before">0em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="pre">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
    <xsl:attribute name="font-size">0.8em</xsl:attribute>
    <xsl:attribute name="keep-together.within-page">auto</xsl:attribute>
    <xsl:attribute name="linefeed-treatment">preserve</xsl:attribute>
    <xsl:attribute name="padding-after">0em</xsl:attribute>
    <!-- Compensate for empty line that always seems to appear here now... -->
    <xsl:attribute name="padding-before">-1em</xsl:attribute>
    <xsl:attribute name="white-space-collapse">false</xsl:attribute>
    <xsl:attribute name="white-space-treatment">preserve</xsl:attribute>
    <xsl:attribute name="wrap-option">no-wrap</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.copyright">
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="text-align">end</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.logo">
    <xsl:attribute name="space-before">130mm</xsl:attribute>
    <xsl:attribute name="space-before.conditionality">retain</xsl:attribute>
    <xsl:attribute name="text-align">end</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.title">
    <xsl:attribute name="border-before-style">solid</xsl:attribute>
    <xsl:attribute name="border-before-width">10pt</xsl:attribute>
    <xsl:attribute name="border-color">#003570</xsl:attribute>
    <xsl:attribute name="font-size">2.3em</xsl:attribute>
    <xsl:attribute name="padding-before">0.5em</xsl:attribute>
    <xsl:attribute name="text-align">end</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.version">
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="text-align">end</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.inner.copyright">
    <xsl:attribute name="border-before-style">solid</xsl:attribute>
    <xsl:attribute name="border-before-width">1pt</xsl:attribute>
    <xsl:attribute name="border-color">#003570</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="padding-before">0.5em</xsl:attribute>
    <xsl:attribute name="space-before">200mm</xsl:attribute>
    <xsl:attribute name="space-before.conditionality">retain</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.inner.copyrightnotice">
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cover.inner.date">
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">2em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="dd">
    <xsl:attribute name="start-indent">2em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="dt">
    <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="image">
    <xsl:attribute name="space-after">0.5em</xsl:attribute>
    <xsl:attribute name="space-before">0.5em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="listblock">
    <xsl:attribute name="provisional-distance-between-starts">1.8em</xsl:attribute>
    <xsl:attribute name="provisional-label-separation">1em</xsl:attribute>
    <xsl:attribute name="space-after">0.25em</xsl:attribute>
    <xsl:attribute name="space-before">0.25em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="listblock.dl">
    <xsl:attribute name="space-after">0.25em</xsl:attribute>
    <xsl:attribute name="space-before">0.25em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="listitem">
    <xsl:attribute name="space-after">0.1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h1">
    <xsl:attribute name="border-after-style">solid</xsl:attribute>
    <xsl:attribute name="border-after-width">1pt</xsl:attribute>
    <xsl:attribute name="border-color">#003570</xsl:attribute>
    <xsl:attribute name="break-before">page</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">1.83em</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
    <xsl:attribute name="space-before">2em</xsl:attribute>
    <xsl:attribute name="space-before.conditionality">retain</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h2">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">1.5em</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
    <xsl:attribute name="space-after">0.3em</xsl:attribute>
    <xsl:attribute name="space-before">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h3">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">1.33em</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
    <xsl:attribute name="space-after">0.3em</xsl:attribute>
    <xsl:attribute name="space-before">0.8em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h4">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">1.17em</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
    <xsl:attribute name="space-after">0.3em</xsl:attribute>
    <xsl:attribute name="space-before">0.6em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h5">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">1em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
    <xsl:attribute name="space-after">0.2em</xsl:attribute>
    <xsl:attribute name="space-before">0.4em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h6">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">0.83em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
    <xsl:attribute name="space-after">0em</xsl:attribute>
    <xsl:attribute name="space-before">0.4em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="p">
    <xsl:attribute name="font-size">1em</xsl:attribute>
    <!-- <xsl:attribute name="keep-together.within-page">always</xsl:attribute>-->
    <xsl:attribute name="space-after">0.5em</xsl:attribute>
    <xsl:attribute name="space-before">0.5em</xsl:attribute>
    <xsl:attribute name="text-align">justify</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="page-header">
    <xsl:attribute name="border-after-style">solid</xsl:attribute>
    <xsl:attribute name="border-after-width">2pt</xsl:attribute>
    <xsl:attribute name="border-color">#003570</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="page-footer">
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="code">
    <xsl:attribute name="border-after-style">solid</xsl:attribute>
    <xsl:attribute name="border-after-width">1pt</xsl:attribute>
    <xsl:attribute name="border-before-style">solid</xsl:attribute>
    <xsl:attribute name="border-before-width">1pt</xsl:attribute>
    <xsl:attribute name="border-color">#003570</xsl:attribute>
    <xsl:attribute name="font-family">monospace</xsl:attribute>
    <xsl:attribute name="font-size">0.8em</xsl:attribute>
    <xsl:attribute name="keep-together.within-page">auto</xsl:attribute>
    <xsl:attribute name="linefeed-treatment">preserve</xsl:attribute>
    <xsl:attribute name="padding-before">0em</xsl:attribute>
    <xsl:attribute name="padding-after">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
    <xsl:attribute name="space-before">2em</xsl:attribute>
    <xsl:attribute name="white-space-collapse">false</xsl:attribute>
    <xsl:attribute name="white-space-treatment">preserve</xsl:attribute>
    <xsl:attribute name="wrap-option">no-wrap</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="table">
    <!-- Only border-collapse="separate" is supported in current FOP
         (if this is not set, border on spanned cells will not work) -->
    <xsl:attribute name="border-collapse">separate</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="td">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">1pt</xsl:attribute>
    <xsl:attribute name="display-align">before</xsl:attribute>
    <xsl:attribute name="padding-after">0.5em</xsl:attribute>
    <xsl:attribute name="padding-before">0.5em</xsl:attribute>
    <xsl:attribute name="padding-end">0.3em</xsl:attribute>
    <xsl:attribute name="padding-start">0.3em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="th">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">1pt</xsl:attribute>
    <xsl:attribute name="display-align">before</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="padding-after">0.5em</xsl:attribute>
    <xsl:attribute name="padding-before">0.5em</xsl:attribute>
    <xsl:attribute name="padding-end">0.3em</xsl:attribute>
    <xsl:attribute name="padding-start">0.3em</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="toc.level1">
    <xsl:attribute name="space-before">1em</xsl:attribute>
  </xsl:attribute-set>


  <!-- Templates -->
  <xsl:template match="/">
    <xsl:apply-templates select="guide"/>
  </xsl:template>

  <xsl:template match="guide">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

      <!-- Page masters -->          
      <fo:layout-master-set>
        <fo:simple-page-master
            master-name="cover"
            margin="25mm">
          <xsl:attribute name="page-height">
            <xsl:value-of select="$page-height"/>
          </xsl:attribute>
          <xsl:attribute name="page-width">
            <xsl:value-of select="$page-width"/>
          </xsl:attribute>
          <fo:region-body 
              margin="0mm"/>
        </fo:simple-page-master>

        <fo:simple-page-master
            master-name="left-page"
            margin-top="25mm"
            margin-bottom="15mm"
            margin-left="20mm"
            margin-right="25mm">
          <xsl:attribute name="page-height">
            <xsl:value-of select="$page-height"/>
          </xsl:attribute>
          <xsl:attribute name="page-width">
            <xsl:value-of select="$page-width"/>
          </xsl:attribute>
          <fo:region-body 
              margin-top="15mm"
              margin-bottom="20mm"/>
          <fo:region-before
              region-name="left-header"
              extent="10mm"/>
          <fo:region-after
              region-name="left-footer"
              extent="10mm"/>
        </fo:simple-page-master>

        <fo:simple-page-master
            master-name="right-page"
            margin-top="25mm"
            margin-bottom="15mm"
            margin-left="25mm"
            margin-right="20mm">
          <xsl:attribute name="page-height">
            <xsl:value-of select="$page-height"/>
          </xsl:attribute>
          <xsl:attribute name="page-width">
            <xsl:value-of select="$page-width"/>
          </xsl:attribute>
          <fo:region-body
              margin-top="15mm"
              margin-bottom="20mm"/>
          <fo:region-before
              region-name="right-header"
              extent="10mm"/>
          <fo:region-after
              region-name="right-footer"
              extent="10mm"/>
        </fo:simple-page-master>

        <fo:page-sequence-master master-name="document">
          <fo:repeatable-page-master-alternatives>
            <fo:conditional-page-master-reference 
                master-reference="left-page"
                odd-or-even="even"/>
            <fo:conditional-page-master-reference 
                master-reference="right-page"
                odd-or-even="odd"/>
          </fo:repeatable-page-master-alternatives>
        </fo:page-sequence-master>
      </fo:layout-master-set>

      <!-- Process bookmarks -->
      <xsl:call-template name="bookmarks.tree"/>

      <!-- Process cover page and table of contents -->
      <xsl:apply-templates select="title"/>
      <xsl:call-template name="toc"/>


      <!-- Process chapters -->
      <fo:page-sequence
          font-family="serif"
          master-reference="document"
          initial-page-number="1">
        <xsl:attribute name="font-size">
          <xsl:value-of select="$base-font-size"/>
        </xsl:attribute>
        <xsl:attribute name="language">
          <xsl:value-of select="/guide/@lang"/>
        </xsl:attribute>

        <fo:static-content flow-name="left-header">
          <fo:block xsl:use-attribute-sets="page-header" text-align="start">
            <fo:retrieve-marker
                retrieve-boundary="page-sequence"
                retrieve-class-name="chapter-title"
                retrieve-position="first-including-carryover"/>
          </fo:block>
        </fo:static-content>

        <fo:static-content flow-name="right-header">
          <fo:block xsl:use-attribute-sets="page-header" text-align="end">
            <fo:retrieve-marker
                retrieve-boundary="page-sequence"
                retrieve-class-name="chapter-title"
                retrieve-position="first-including-carryover"/>
          </fo:block>
        </fo:static-content>

        <fo:static-content flow-name="left-footer">
          <fo:block xsl:use-attribute-sets="page-footer" text-align="start">
            <fo:page-number/>
            <xsl:text> | </xsl:text>
            <xsl:value-of select="$companyname"/>:
            <xsl:value-of select="/guide/title"/>
          </fo:block>
        </fo:static-content>

        <fo:static-content flow-name="right-footer">
          <fo:block xsl:use-attribute-sets="page-footer" text-align="end">
            <xsl:value-of select="$companyname"/>:
            <xsl:value-of select="/guide/title"/>
            <xsl:text> | </xsl:text>
            <fo:page-number/>
          </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates select="chapter"/>
          <!-- Do not include appendix when building single chapters -->
	  <!--
          <xsl:if test="count(/guide/chapter) != 1 ">
            <xsl:call-template name="appendix"/>
          </xsl:if>
	  -->
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>


  <!-- Cover page -->
  <xsl:template match="guide/title">
    <fo:page-sequence 
        font-family="sans-serif"
        force-page-count="even"
        master-reference="cover">
      <xsl:attribute name="font-size">
        <xsl:value-of select="$base-font-size"/>
      </xsl:attribute>
      <xsl:attribute name="language">
        <xsl:value-of select="/guide/@lang"/>
      </xsl:attribute>

      <fo:flow flow-name="xsl-region-body">
        <fo:block xsl:use-attribute-sets="cover.logo">
          <fo:external-graphic src="../pics/logo.png"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="cover.title" id="cover-page">
          <xsl:apply-templates/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="cover.copyright">
          <xsl:value-of select="$copyright"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="$companyname"/>
        </fo:block>
        <xsl:if test="/guide/productname">
          <fo:block xsl:use-attribute-sets="cover.version">
            <xsl:value-of select="/guide/productname"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="/guide/swversion"/>
          </fo:block>
        </xsl:if>
        <fo:block xsl:use-attribute-sets="cover.version">
          <xsl:value-of select="/guide/date"/>
        </fo:block>

        <!-- Inner cover (copyright notice) -->
        <fo:block break-before="page"
                  xsl:use-attribute-sets="cover.inner.copyright">
          <xsl:value-of select="$copyright"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="$companyname"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="cover.inner.copyrightnotice">
          All contents in this document are confidential and proprietary to
          <xsl:value-of select="$companyname"/>.
        </fo:block>
        <fo:block xsl:use-attribute-sets="cover.inner.date">
          <xsl:value-of select="/guide/date"/>
        </fo:block>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>


  <!-- Bookmarks -->
  <xsl:template name="bookmarks.tree">
    <fo:bookmark-tree>
      <fo:bookmark internal-destination="cover-page"
                   starting-state="show">
        <fo:bookmark-title>
          <xsl:value-of select="/guide/title"/>
        </fo:bookmark-title>
        <fo:bookmark internal-destination="table-of-contents"
                     starting-state="hide">
          <fo:bookmark-title>
            <xsl:text>Table of contents</xsl:text>
          </fo:bookmark-title>
        </fo:bookmark>

        <xsl:call-template name="bookmarks">
          <xsl:with-param name="entries" select="chapter[title]"/>
        </xsl:call-template>

        <!-- FIXME: remove when appendix creation has been fixed -->
	<!--
        <xsl:if test="count(/guide/chapter) != 1 ">
          <fo:bookmark internal-destination="appendix-start-page"
                       starting-state="hide">
            <fo:bookmark-title>
              <xsl:value-of select="$appendix_title"/>
            </fo:bookmark-title>
          </fo:bookmark>
        </xsl:if>
	-->
      </fo:bookmark>
    </fo:bookmark-tree>
  </xsl:template>

  <xsl:template name="bookmarks">
    <xsl:param name="entries"/>
    <xsl:for-each select="$entries">
      <fo:bookmark internal-destination="{generate-id(title)}"
                   starting-state="hide">
        <fo:bookmark-title>
          <xsl:value-of select="title"/>
        </fo:bookmark-title>
        <xsl:call-template name="bookmarks">
          <xsl:with-param name="entries"
                          select="section[title] | subsection[title]"/>
        </xsl:call-template>
      </fo:bookmark>
    </xsl:for-each>
  </xsl:template>


  <!-- Table of contents -->
  <xsl:template name="toc">
    <fo:page-sequence 
        font-family="sans-serif"
        master-reference="document"
        format="i"
        force-page-count="even"
        initial-page-number="1">
      <xsl:attribute name="font-size">
        <xsl:value-of select="$base-font-size"/>
      </xsl:attribute>
      <xsl:attribute name="language">
        <xsl:value-of select="/guide/@lang"/>
      </xsl:attribute>

      <fo:static-content flow-name="left-header">
        <fo:block xsl:use-attribute-sets="page-header" text-align="start">
            Contents
        </fo:block>
      </fo:static-content>

      <fo:static-content flow-name="right-header">
        <fo:block xsl:use-attribute-sets="page-header" text-align="end">
            Contents
        </fo:block>
      </fo:static-content>

      <fo:static-content flow-name="left-footer">
        <fo:block xsl:use-attribute-sets="page-footer" text-align="start">
          <fo:page-number/>
          <xsl:text> | </xsl:text>
          <xsl:value-of select="$companyname"/>:
          <xsl:value-of select="/guide/title"/>
        </fo:block>
      </fo:static-content>

      <fo:static-content flow-name="right-footer">
        <fo:block xsl:use-attribute-sets="page-footer" text-align="end">
          <xsl:value-of select="$companyname"/>:
          <xsl:value-of select="/guide/title"/>
          <xsl:text> | </xsl:text>
          <fo:page-number/>
        </fo:block>
      </fo:static-content>

      <fo:flow flow-name="xsl-region-body">
        <fo:block xsl:use-attribute-sets="h1" id="table-of-contents">
          Table of contents
        </fo:block>
        <xsl:call-template name="toc.lines">
          <xsl:with-param name="entries" select="chapter[title]"/>
          <xsl:with-param name="level" select="1"/>
        </xsl:call-template>
      
        <!-- FIXME: remove when appendix creation has been fixed -->
	<!--
        <xsl:if test="count(/guide/chapter) != 1 ">
          <fo:block xsl:use-attribute-sets="toc.level1"
                    text-align-last="justify">
            <fo:basic-link internal-destination="appendix-start-page">
              <fo:inline font-weight="bold">
                <xsl:value-of select="$appendix_title"/>
              </fo:inline>
              <fo:leader leader-pattern="dots"/>
              <fo:page-number-citation ref-id="appendix-start-page"/>
            </fo:basic-link>
          </fo:block>
        </xsl:if>
	-->
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template name="toc.lines">
    <xsl:param name="entries"/>
    <xsl:param name="level"/>
    <xsl:for-each select="$entries">
      <xsl:if test="$level = 1">
        <fo:block xsl:use-attribute-sets="toc.level1"/>
      </xsl:if>
      <fo:block text-align-last="justify">
        <fo:basic-link internal-destination="{generate-id(title)}">
          <xsl:choose>
            <xsl:when test="$level = 1">
              <xsl:attribute name="keep-with-next.within-page">always</xsl:attribute>
              <fo:inline font-weight="bold">
                <xsl:number level="single"
                            count="chapter "
                            format="1 "/>
                <xsl:value-of select="title"/>
              </fo:inline>
            </xsl:when>
            <xsl:otherwise>
              <xsl:number level="multiple"
                          count="chapter | section | subsection"
                          format="1.1 "/>
              <xsl:value-of select="title"/>
            </xsl:otherwise>
          </xsl:choose>
          <fo:leader leader-pattern="dots"/>
          <fo:page-number-citation ref-id="{generate-id(title)}"/>
        </fo:basic-link>
      </fo:block>
      <xsl:call-template name="toc.lines">
        <xsl:with-param name="entries" select="section[title]"/>
        <xsl:with-param name="level" select="$level + 1"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>


  <!-- Chapter -->
  <xsl:template match="chapter">
    <xsl:variable name="chapnum"><xsl:number/></xsl:variable>

    <fo:block xsl:use-attribute-sets="h1" id="{generate-id(title)}">
      <!-- Mark title for header printout -->
      <fo:marker marker-class-name="chapter-title">
        <xsl:number/>&#160;&#160;<xsl:value-of select="title"/>
      </fo:marker>
      <xsl:number/>&#160;&#160;<xsl:value-of select="title"/>
    </fo:block>

    <xsl:apply-templates select="body">
      <xsl:with-param name="chapnum" select="$chapnum"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="section">
      <xsl:with-param name="chapnum" select="$chapnum"/>
    </xsl:apply-templates>
  </xsl:template>

  
  <!-- Section -->
  <xsl:template match="section">
    <xsl:param name="chapnum"/>
    <fo:block xsl:use-attribute-sets="h2" id="{generate-id(title)}">
      <xsl:value-of select="$chapnum"/>.<xsl:number/>&#160;
      <xsl:value-of select="title"/>
    </fo:block>
    <xsl:apply-templates select="body">
      <xsl:with-param name="chapnum" select="$chapnum"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="subsection">
      <xsl:with-param name="chapnum" select="$chapnum"/>
      <xsl:with-param name="sectnum"><xsl:number/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

 
  <!-- Subsection -->
  <xsl:template match="subsection">
    <xsl:param name="chapnum"/>
    <xsl:param name="sectnum"/>
    <fo:block xsl:use-attribute-sets="h3" id="{generate-id(title)}">
      <xsl:value-of select="$chapnum"/>.<xsl:value-of select="$sectnum"/>.<xsl:number/>
      <xsl:text>  </xsl:text>
      <xsl:value-of select="title"/>
    </fo:block>
    <xsl:apply-templates select="body">
      <xsl:with-param name="chapnum" select="$chapnum"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- Appendix -->
  <!-- FIXME: The current appendix arrangement is just a temporary solution
    -  until the  book building procedure has been improved...
    -->
  <xsl:template name="appendix">
    <fo:block xsl:use-attribute-sets="h1" id="appendix-start-page">
      <!-- Mark title for header printout -->
      <fo:marker marker-class-name="chapter-title">
        <xsl:value-of select="$appendix_title"/>
      </fo:marker>
        <xsl:value-of select="$appendix_title"/>
    </fo:block>
    <fo:block>
      This appendix contains copies of the Unix man pages that are installed 
      with the ConfD software package.
    </fo:block>

    <fo:table space-before="1em" table-layout="fixed" width="100%">
      <fo:table-column column-width="25%"/>
      <fo:table-column column-width="75%"/>

      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
                confd(1)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - command to start and control the ConfD daemon
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_aaa_bridge(1)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
               - populating ConfD aaa_bridge.fxs with external data
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confdc(1)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - command to compile and link confspec files
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
                confd_jsshell(1)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - command line shell to the ConfD Javascript engine
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              smidump(1)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - translation tool from SNMP MIB files to confspecs
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_js(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>        
              - Javascript library for ConfD
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_js_file(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>        
              - Javascript File object
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_js_maapi(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
	    <fo:block>        
	      - AJAX MAAPI (Management Agent API). A library for connecting 
	      to ConfD with a read/write interface inside transactions
	      from a web interface.
	    </fo:block>
	  </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_lib(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>        
              - callback library for connecting to ConfD
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_lib_cdb(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>        
              - library for connecting to ConfD built in XML database (CDB)
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_lib_maapi(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>        
              - MAAPI (Management Agent API). A library for connecting to 
                ConfD with a read/write interface inside transactions.
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
               confd_types(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - ConfD XML value representation in C
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
               smi_config(3)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - how to configure the SNMP translation tool
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              clispec(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>       
              - CLI specification file format
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd.conf(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>       
              - ConfD daemon configuration file format
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confd_ns(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              - built-in utility namespace
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confspec(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
               - configuration specification file format
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              confspec_annotations(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
               - confspec annotations
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              ncspec(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>       
              - NETCONF specification file format
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              webspec(5)
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>       
              - Web UI specification file format
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <!-- Body -->
  <xsl:template match="body">
    <xsl:param name="chapnum"/>
    <xsl:apply-templates>
      <xsl:with-param name="chapnum" select="$chapnum"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- Image -->
  <xsl:template match="img">
    <xsl:param name="chapnum"/>
    <xsl:variable name="fignum">
      <xsl:number level="any" from="chapter" count="img"/>
    </xsl:variable>

    <fo:block xsl:use-attribute-sets="image">
      <fo:external-graphic src="{@src}">
        <xsl:if test="@width">
          <xsl:attribute name="content-width">
            <xsl:value-of select="@width" />
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@height">
          <xsl:attribute name="content-height">
            <xsl:value-of select="@height" />
          </xsl:attribute>
        </xsl:if>
      </fo:external-graphic>
    </fo:block>
    <xsl:if test="@caption">
      <fo:block xsl:use-attribute-sets="caption">
        Figure
        <xsl:value-of select="$chapnum"/>.<xsl:value-of select="$fignum"/>:
        &#160;
        <xsl:value-of select="@caption"/>
      </fo:block>
    </xsl:if>
  </xsl:template>


  <!-- Code listing -->
  <xsl:template match="code">
    <xsl:param name="chapnum"/>
    <xsl:variable name="codenum">
      <xsl:number level="any" from="chapter" count="code"/>
    </xsl:variable>

    <fo:block xsl:use-attribute-sets="code" margin-left="1.5em">
      <xsl:apply-templates select="text()"/> 
    </fo:block>

    <xsl:if test="@caption">
      <fo:block xsl:use-attribute-sets="caption" margin-left="1.5em">
          Code listing <xsl:value-of select="$chapnum"/>.<xsl:value-of select="$codenum"/>:&#160;
          <xsl:value-of select="@caption"/>
      </fo:block>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pre">
    <fo:block xsl:use-attribute-sets="pre">
      <xsl:apply-templates select="text()"/>
    </fo:block>
  </xsl:template>


  <!-- Lists -->
  <xsl:template match="ol">
    <xsl:param name="chapnum"/>
    <fo:list-block xsl:use-attribute-sets="listblock">
      <xsl:apply-templates>
        <xsl:with-param name="chapnum" select="$chapnum"/>
      </xsl:apply-templates>
    </fo:list-block>
  </xsl:template>

  <xsl:template match="ul">
    <xsl:param name="chapnum"/>
    <fo:list-block xsl:use-attribute-sets="listblock">
      <xsl:apply-templates>
        <xsl:with-param name="chapnum" select="$chapnum"/>
      </xsl:apply-templates>
    </fo:list-block>
  </xsl:template>

  <!-- See XSL specification 6.8.1.1 for a more complicated dl solution... -->
  <xsl:template match="dl">
    <xsl:param name="chapnum"/>
    <fo:block xsl:use-attribute-sets="listblock.dl">
      <xsl:apply-templates>
        <xsl:with-param name="chapnum" select="$chapnum"/>
      </xsl:apply-templates>
    </fo:block>
  </xsl:template>

  <xsl:template match="ol/li">
    <xsl:param name="chapnum"/>
    <fo:list-item xsl:use-attribute-sets="listitem">
      <fo:list-item-label end-indent="label-end()">
        <fo:block>
          <xsl:number/>.
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()" format="justify">
        <fo:block>
          <xsl:apply-templates>
            <xsl:with-param name="chapnum" select="$chapnum"/>
          </xsl:apply-templates>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="ul/li">
    <xsl:param name="chapnum"/>
    <fo:list-item xsl:use-attribute-sets="listitem">
      <fo:list-item-label end-indent="label-end()">
        <fo:block>
          &#x2022;
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()" format="justify">
        <fo:block>
          <xsl:apply-templates>
            <xsl:with-param name="chapnum" select="$chapnum"/>
          </xsl:apply-templates>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="dl[@termtype='code']/dt">
    <fo:block xsl:use-attribute-sets="dt">
      <fo:inline font-family="monospace">
        <xsl:apply-templates/>
      </fo:inline>
    </fo:block>
  </xsl:template>

  <xsl:template match="dl[@termtype='strong']/dt">
    <fo:block xsl:use-attribute-sets="dt">
      <fo:inline font-weight="bold">
        <xsl:apply-templates/>
      </fo:inline>
    </fo:block>
  </xsl:template>

  <xsl:template match="dl[@termtype='em']/dt">
    <fo:block xsl:use-attribute-sets="dt">
      <fo:inline font-style="italic">
        <xsl:apply-templates/>
      </fo:inline>
    </fo:block>
  </xsl:template>

  <xsl:template match="dl[@termtype='plain']/dt">
    <fo:block xsl:use-attribute-sets="dt">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="dd">
    <xsl:param name="chapnum"/>
    <fo:block xsl:use-attribute-sets="dd">
      <xsl:apply-templates>
        <xsl:with-param name="chapnum" select="$chapnum"/>
      </xsl:apply-templates>
    </fo:block>
  </xsl:template>


  <!-- Table -->
  <!-- Tables are created with the same tags as for HTML tables. However, note:
       * column width must be specified using <col> tags (fop doesn't calculate
         column width automatically)
       * tables rows (<tr>) must be enclosed in any of <thead>, <tbody>, or
         <tfoot>. Table rows enclosed in <thead> and <tfoot> appears on every
         page if the table spans multiple pages.
       * <thead> and <tfoot> must be specified before <tbody> (if used)
       * for <table> tag, the attribute 'width' is supported
       * for <tr> tag, the attribute 'height' is supported
       * for <td> and <th> tags, the attributes 'colspan', 'rowspan', 'align'
         and 'valign' are supported (works as in HTML)


       Example:
         
       <table width="100%">
         <col width="40%"/>
         <col width="30%"/>
         <col width="30%"/>
         <thead>
           <tr>
             <th>Head 1</th>
             <th>Head 2</th>
             <th>Head 3</th>
           </tr>
         </thead>
         <tfoot>
           <tr>
             <td colspan="3" align="center">Table foot</th>
           </tr>
         </tfoot>
         <tbody>
           <tr>
             <td>Table data 1</th>
             <td>Table data 2</th>
             <td>Table data 3</th>
           </tr>
         </tbody>
       </table>
  -->

  <xsl:template match="table">
    <fo:table xsl:use-attribute-sets="table">
      <xsl:if test="@width">
        <xsl:attribute name="inline-progression-dimension">
          <xsl:value-of select="@width"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </fo:table>
  </xsl:template>

  <xsl:template match="col">
    <fo:table-column column-width="{@width}"/>
  </xsl:template>

  <xsl:template match="tbody">
    <fo:table-body>
      <xsl:apply-templates/>
    </fo:table-body>
  </xsl:template>

  <xsl:template match="thead">
    <fo:table-header>
      <xsl:apply-templates/>
    </fo:table-header>
  </xsl:template>

  <xsl:template match="tfoot">
    <fo:table-footer>
      <xsl:apply-templates/>
    </fo:table-footer>
  </xsl:template>

  <xsl:template match="tr">
    <fo:table-row>
      <xsl:if test="@height">
        <xsl:attribute name="block-progression-dimension">
          <xsl:value-of select="@height"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </fo:table-row>
  </xsl:template>

  <xsl:template match="th">
    <fo:table-cell xsl:use-attribute-sets="th">
      <xsl:call-template name="cell-span"/>
      <xsl:call-template name="cell-valign"/>
      <fo:block>
        <xsl:call-template name="cell-align"/>
        <xsl:apply-templates/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="td">
    <fo:table-cell xsl:use-attribute-sets="td">
      <xsl:call-template name="cell-span"/>
      <xsl:call-template name="cell-valign"/>
      <fo:block>
        <xsl:call-template name="cell-align"/>
        <xsl:apply-templates/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>

  <xsl:template name="cell-span">
    <xsl:if test="@colspan">
      <xsl:attribute name="number-columns-spanned">
        <xsl:value-of select="@colspan"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@rowspan">
      <xsl:attribute name="number-rows-spanned">
        <xsl:value-of select="@rowspan"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="cell-align">
    <xsl:choose>
      <xsl:when test="@align='left'">
        <xsl:attribute name="text-align">start</xsl:attribute>
      </xsl:when>
      <xsl:when test="@align='center'">
        <xsl:attribute name="text-align">center</xsl:attribute>
      </xsl:when>
      <xsl:when test="@align='right'">
        <xsl:attribute name="text-align">end</xsl:attribute>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="cell-valign">
    <xsl:choose>
      <xsl:when test="@valign='top'">
        <xsl:attribute name="display-align">before</xsl:attribute>
      </xsl:when>
      <xsl:when test="@valign='middle'">
        <xsl:attribute name="display-align">center</xsl:attribute>
      </xsl:when>
      <xsl:when test="@valign='bottom'">
        <xsl:attribute name="display-align">after</xsl:attribute>
      </xsl:when>
    </xsl:choose>
  </xsl:template>


  <!-- Paragraph -->
  <xsl:template match="p">
    <fo:block xsl:use-attribute-sets="p">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>


  <!-- Inline elements -->
  <xsl:template match="b | strong">
    <fo:inline font-weight="bold">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="br">
    <fo:block/>
  </xsl:template>

  <xsl:template match="c | tt | path">
    <fo:inline font-family="monospace">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="em | i">
    <fo:inline font-style="italic">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
</xsl:stylesheet>
