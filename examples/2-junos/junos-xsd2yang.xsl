<?xml version="1.0" encoding="ISO-8859-1"?>
<!--

 Copyright 2010-2012 Tail-f Systems. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

 THIS SOFTWARE IS PROVIDED BY TAIL-F SYSTEMS ``AS IS'' AND ANY EXPRESS
 OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL TAIL-F SYSTEMS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

-->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:str="http://exslt.org/strings"
		xmlns:dyn="http://exslt.org/dynamic">

  <xsl:output method="text"/>
  <xsl:strip-space elements="*"/>

  <xsl:param name="gProgname" select="'junos-xsd2yang'"/>

  <!-- how many whitespace characters each indentation level should be -->
  <xsl:param name="gIndent" select="'2'"/>
  <xsl:param name="gDoIndent" select="false()"/>

  <!-- include support for apply-groups? -->
  <xsl:param name="gApplyGroups" select="true()"/>

  <!-- include support for dynamic-profiles? -->
  <xsl:param name="gDynamicProfiles" select="false()"/>

  <!-- make use of tailf extensions -->
  <xsl:param name="gTailfExt" select="true()"/>

  <!-- make it possible to only include specific models -->
  <xsl:param name="product-filter" select="''"/>
  <!-- include spaces *around* all the wanted products -->
<!--  <xsl:param name="product-filter" select=" mx80 mx240 "/> -->

  <!-- Global variables -->
  <xsl:variable name="nl">
<xsl:text>
</xsl:text>
  </xsl:variable>
  <xsl:variable name="QC"><xsl:text>&quot;</xsl:text></xsl:variable>
  <xsl:variable name="sQC"><xsl:text>'</xsl:text></xsl:variable>

  <xsl:variable name="ns-xs"    select="'http://www.w3.org/2001/XMLSchema'"/>

  <!--
     - Helper functions
    -->

  <!-- Indent to the current level -->
  <xsl:template name="indent">
    <xsl:param name="indent" select="'0'"/>
    <xsl:choose>
      <xsl:when test="$gDoIndent">
	<xsl:variable name="level">
	  <xsl:call-template name="depth">
	    <xsl:with-param name="node" select="."/>
	  </xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="str:padding($gIndent * ($level + $indent))"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>  </xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Return the depth of the current *context node* -->
  <xsl:template name="depth">
    <xsl:param name="node"/>
    <xsl:param name="depth" select="'-2'"/>
    <xsl:choose>
      <xsl:when test="not($node)">
	<!-- <xsl:message><xsl:value-of select="concat('TOP: ',name($node),' ',$node/@name,' ',generate-id($node))"/></xsl:message> -->
	<xsl:value-of select="$depth"/>
      </xsl:when>
      <xsl:otherwise>
	<!-- <xsl:message><xsl:value-of select="concat(name($node),' ',$node/@name,' ',generate-id($node))"/></xsl:message> -->
	<xsl:call-template name="depth">
	  <xsl:with-param name="node" select="$node/.."/>
	  <xsl:with-param name="depth" select="$depth + '1'"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- insert quotes around a string if needed -->
  <xsl:template name="quote">
    <xsl:param name="str"/>

    <!-- FIXME inefficient... -->
    <!-- FIXME doesn't properly escape \ and double quotes -->
    <xsl:variable name="quotep">
      <xsl:choose>
	<xsl:when test="$str = ''"/>
	<xsl:when test="not($str)"/>
	<xsl:when test="contains($str, ' ')"/>
	<xsl:when test="contains($str, ';')"/>
	<xsl:when test="contains($str, '{')"/>
	<xsl:when test="contains($str, '}')"/>
	<xsl:when test="contains($str, '/')"/>
	<xsl:when test="contains($str, '\')"/>
	<xsl:when test="contains($str, $QC)"/>
	<xsl:when test="contains($str, $sQC)"/>
	<xsl:when test="starts-with($str, '[')"/>
	<xsl:otherwise>
	  <xsl:value-of select="'no'"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$quotep != 'no'">
	<xsl:choose>
	  <xsl:when test="contains($str, $QC)">
	    <xsl:choose>
	      <xsl:when test="contains($str, $sQC)">
		<!-- both single and double quotes in string -->
		<!-- solve by replacing double quotes with single -->
		<!-- FIXME should instead escape double -->
		<xsl:value-of select="concat($QC, translate($str,$QC,$sQC), $QC)"/>
	      </xsl:when>
	      <xsl:otherwise>
		<!-- double quotes in string, quote with single -->
		<xsl:value-of select="concat($sQC, $str, $sQC)"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- no quote charachters in string -->
	    <xsl:value-of select="concat($QC, $str, $QC)"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- emit a yang statement -->
  <xsl:template name="emit-statement">
    <xsl:param name="keyw"/>
    <xsl:param name="arg"/>
    <xsl:param name="indent"  select="'0'"/>    <!-- extra indentation -->
    <xsl:param name="argp"   select="true()"/>	<!-- use argument? -->
    <xsl:param name="quotep" select="true()"/>  <!-- quote arg? -->
    <xsl:param name="blockp" select="false()"/> <!-- begin block? -->
    <xsl:param name="nlp"    select="true()"/>	<!-- break line? -->

    <xsl:call-template name="indent">
      <xsl:with-param name="indent" select="$indent"/>
    </xsl:call-template>
    <xsl:value-of select="$keyw"/>

    <xsl:if test="string($argp) = 'true'">
      <!-- possibly quote the arg string -->
      <xsl:variable name="qarg">
	<xsl:choose>
	  <xsl:when test="$quotep">
	    <xsl:call-template name="quote">
	      <xsl:with-param name="str" select="$arg"/>
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="$arg"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <xsl:value-of select="concat(' ', $qarg)"/>
    </xsl:if>

    <!-- terminate with ; or start a block with { followed by WS or \n -->
    <xsl:choose>
      <xsl:when test="$blockp">
	<xsl:text> {</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$nlp">
	<xsl:value-of select="$nl"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text> </xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <!-- emit a yang single statement with an unquoted argument -->
  <xsl:template name="emit-statement-unquoted">
    <xsl:param name="keyw"/>
    <xsl:param name="arg"/>
    <xsl:param name="indent" select="'0'"/>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw"   select="$keyw"/>
      <xsl:with-param name="indent"  select="$indent"/>
      <xsl:with-param name="arg"    select="$arg"/>
      <xsl:with-param name="quotep" select="false()"/>
    </xsl:call-template>
  </xsl:template>

  <!-- As emit-statement, except arg is a nodeset and the resulting
       arg is the @name of each member of that nodeset -->
  <xsl:template name="emit-statement-nodenames">
    <xsl:param name="keyw"/>
    <xsl:param name="arg"/>
    <xsl:param name="indent" select="'0'"/>
    <xsl:variable name="argstr">
      <xsl:for-each select="$arg">
	<xsl:value-of select="concat(' ', @name)"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="$keyw"/>
      <xsl:with-param name="arg" select="normalize-space($argstr)"/>
      <xsl:with-param name="indent" select="$indent"/>
    </xsl:call-template>
  </xsl:template>

  <!-- As emit-statement-nodenames, except that the resulting arg is
       the relative (datamodel) path to each member of the arg nodeset -->
  <xsl:template name="emit-statement-nodenames-relpath">
    <xsl:param name="keyw"/>
    <xsl:param name="arg"/>
    <xsl:param name="parent"/>
    <xsl:param name="indent" select="'0'"/>
    <xsl:variable name="argstr">
      <xsl:for-each select="$arg">
	<xsl:variable name="name">
	  <xsl:call-template name="relpath">
	    <xsl:with-param name="parent" select="$parent"/>
	    <xsl:with-param name="pos" select="."/>
	  </xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat(' ', $name)"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="$keyw"/>
      <xsl:with-param name="arg" select="normalize-space($argstr)"/>
      <xsl:with-param name="indent" select="$indent"/>
    </xsl:call-template>
  </xsl:template>

  <!-- emit a yang statement and start a new block -->
  <xsl:template name="emit-statement-block">
    <xsl:param name="keyw"/>
    <xsl:param name="arg"/>
    <xsl:param name="indent" select="'0'"/>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="$keyw"/>
      <xsl:with-param name="indent" select="$indent"/>
      <xsl:with-param name="arg" select="$arg"/>
      <xsl:with-param name="blockp" select="true()"/>
    </xsl:call-template>
  </xsl:template>

  <!-- emit a yang closing bracket -->
  <xsl:template name="emit-close-block">
    <xsl:param name="indent" select="'0'"/>
    <xsl:call-template name="indent">
      <xsl:with-param name="indent" select="$indent"/>
    </xsl:call-template>
    <xsl:text>}</xsl:text>
    <xsl:value-of select="$nl"/>
  </xsl:template>

  <!-- import statement -->
  <xsl:template name="emit-import-stmt">
    <xsl:param name="name"/>
    <xsl:param name="prefix"/>
    <xsl:param name="indent" select="'0'"/>
    <xsl:call-template name="emit-statement-block">
      <xsl:with-param name="keyw" select="'import'"/>
      <xsl:with-param name="arg"  select="$name"/>
      <xsl:with-param name="indent" select="'1' + $indent"/>
    </xsl:call-template>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'prefix'"/>
      <xsl:with-param name="arg"  select="$prefix"/>
      <xsl:with-param name="indent" select="'2' + $indent"/>
    </xsl:call-template>
    <xsl:call-template name="emit-close-block">
      <xsl:with-param name="indent" select="'1' + $indent"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="emit-key-statement">
    <xsl:param name="cnode"/>
    <xsl:param name="indent" select="'0'"/>
    <!--
    <xsl:message>emit-key-statement <xsl:if test="count($cnode) = 1"><xsl:value-of select="local-name($cnode)"/></xsl:if><xsl:value-of select="concat(' ',count($cnode),' ',count($cnode/*))"/></xsl:message>
    -->
    <!-- Figure out which are the key-nodes. They are: element nodes
         with key attributes, element nodes whose types have key
         attributes, and choice nodes with key attributes -->
    <xsl:variable name="key-nodes"
		  select="
      $cnode/xs:element[xs:complexType/xs:simpleContent/xs:extension/xs:attribute/@name = 'key']|
      $cnode/xs:element[xs:complexType/xs:simpleContent/xs:restriction/@base = /xs:schema/xs:complexType[xs:simpleContent/xs:extension/xs:attribute[@name='key']]/@name]|
      $cnode/xs:choice[xs:element/xs:complexType/xs:attribute[@name='key']]"/>
    <xsl:variable name="argstr">
      <xsl:for-each select="$key-nodes">
	<xsl:choose>
	  <xsl:when test="@name">
	    <xsl:value-of select="concat(' ', @name)"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:variable name="pos" select="position()"/>
	    <xsl:value-of select="concat(' key',$pos)"/>
	    <xsl:if test="xs:element/xs:complexType/xs:simpleContent">
	      <xsl:value-of select="concat(' key',$pos,'-arg')"/>
	    </xsl:if>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'key'"/>
      <xsl:with-param name="arg" select="normalize-space($argstr)"/>
      <xsl:with-param name="indent" select="$indent"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="relpath">
    <xsl:param name="parent"/>
    <xsl:param name="pos"/>
    <xsl:param name="path"/>
    <xsl:choose>
      <xsl:when test="generate-id($pos) = generate-id($parent)">
	<xsl:value-of select="$path"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="relpath">
	  <xsl:with-param name="parent" select="$parent"/>
	  <xsl:with-param name="pos" select="$pos/.."/>
	  <xsl:with-param name="path">
	    <xsl:choose>
	      <xsl:when test="$path">
		<xsl:value-of select="concat($pos/@name, '/', $path)"/>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:value-of select="@name"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:with-param>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Note: don't call this template with an attribute node as
       context node, because in that case the namespace axis is
       empty.  If the attribute is the context node you can change
       it before calling, like this:

  	   <xsl:for-each select="..">
             <xsl:call-template name="translate-type">
             ...
           <xsl:for-each/>

       I'm sure there is a better way, but I don't know what it is:-)
  -->
  <xsl:template name="translate-namespace-prefix">
    <xsl:param name="type-str"/>

    <xsl:variable name="ns-tag" select="substring-before($type-str, ':')"/>
    <!-- Here comes the "trick" of matching the prefix with the
	 declared namespaces prefixes -->
    <xsl:variable name="ns" select="string(namespace::*[name() = $ns-tag])"/>

    <!-- If there isn't a proper xmlns, be kind and try to make it work... -->
    <xsl:choose>
      <xsl:when test="($ns = '') and ($ns-tag = 'xs')">
	<xsl:value-of select="$ns-xs"/>
      </xsl:when>
      <xsl:when test="($ns = '') and ($ns-tag = 'xsd')">
	<xsl:value-of select="$ns-xs"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$ns"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  
  <!-- Datatype mapping -->
  <xsl:template name="translate-type">
    <xsl:param name="type-str"/>

    <xsl:variable name="type" select="substring-after($type-str, ':')"/>
    <xsl:variable name="ns">
      <xsl:call-template name="translate-namespace-prefix">
	<xsl:with-param name="type-str" select="$type-str"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>

      <xsl:when test="$ns = $ns-xs">
	<xsl:choose>
	  <!-- Translate XML Schema types into Yang types whenever possible -->

	  <!-- builtin types -->
	  <xsl:when test="$type = 'byte'">
	    <xsl:text>int8</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'short'">
	    <xsl:text>int16</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'int'">
	    <xsl:text>int32</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'integer'">
	    <xsl:text>int64</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'long'">
	    <xsl:text>int64</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'unsignedByte'">
	    <xsl:text>uint8</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'unsignedShort'">
	    <xsl:text>uint16</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'unsignedInt'">
	    <xsl:text>uint32</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'unsignedLong'">
	    <xsl:text>uint64</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'string'">
	    <xsl:text>string</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'boolean'">
	    <xsl:text>boolean</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'dateTime'">
	    <xsl:text>yang:date-and-time</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'negativeInteger'">
	    <xsl:text>int64</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'nonNegativeInteger'">
	    <xsl:text>uint64</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'nonPositiveInteger'">
	    <xsl:text>int64</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type = 'positiveInteger'">
	    <xsl:text>uint64</xsl:text>
	  </xsl:when>
	  <xsl:when test="starts-with($type, 'g')">
	    <xsl:text>empty /* FIXME $gProgname: type </xsl:text>
	    <xsl:value-of select="$type"/>
	    <xsl:text> not supported */</xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- remaining xml schema types live in tailf-xsd-types -->
	    <xsl:value-of select="concat('xs:', $type)"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>

      <xsl:otherwise>
	<xsl:choose>
	  <xsl:when test="not($type-str)"> <!-- untyped optional leafs -->
	    <xsl:text>empty</xsl:text>
	  </xsl:when>
	  <xsl:when test="$type-str = ''"> <!-- untyped optional leafs -->
	    <xsl:text>empty</xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="$type-str"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- any implicit restrictions that follow from the type -->
  <xsl:template name="emit-type-implicit-restriction">
    <xsl:param name="type-str"/>
    <xsl:param name="indent" select="'1'"/>

    <xsl:variable name="type" select="substring-after($type-str, ':')"/>
    <xsl:variable name="ns">
      <xsl:call-template name="translate-namespace-prefix">
	<xsl:with-param name="type-str" select="$type-str"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="range">
      <xsl:choose>
	<xsl:when test="$ns = $ns-xs">
	  <xsl:choose>
	    <xsl:when test="$type = 'negativeInteger'">
	      <xsl:text>min .. -1</xsl:text>
	    </xsl:when>
	    <xsl:when test="$type = 'nonNegativeInteger'">
	      <xsl:text>0 .. max</xsl:text>
	    </xsl:when>
	    <xsl:when test="$type = 'nonPositiveInteger'">
	      <xsl:text>min .. 0</xsl:text>
	    </xsl:when>
	    <xsl:when test="$type = 'positiveInteger'">
	      <xsl:text>1 .. max</xsl:text>
	    </xsl:when>
	  </xsl:choose>
	</xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="$range != ''">
      <xsl:call-template name="emit-statement">
	<xsl:with-param name="keyw" select="'range'"/>
	<xsl:with-param name="arg"  select="$range"/>
	<xsl:with-param name="indent"  select="$indent"/>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>


  <!-- emit context node and everything below it in a comment -->
  <xsl:template name="unhandled">
    <xsl:text>/* unhandled: 
</xsl:text>
    <xsl:apply-templates select="." mode="in-comment"/>
    <xsl:text>**************/
</xsl:text>
  </xsl:template>


  <!-- call with an <xs:element> as context node, checks if container is np -->
  <xsl:template name="container-type">
    <xsl:param name="cnode" select="./xs:complexType"/>
    <!-- It seems that containers with appinfo/remove-if-empty (and
         appinfo/flag=remove-empty, which always comes together) are
         np containers. Other containers, i.e. without any flags, are
         presence containers. However, np containers with mandatory
         children doesn't make a very usable model, so we make them
         presence containers as well.
    -->
    <xsl:choose>
      <!-- Mandatory children (direct or in referred grouping) -->
      <xsl:when test="$cnode/xs:sequence/xs:choice/xs:element/xs:annotation/xs:appinfo/flag[text() = 'mandatory']">
	<xsl:call-template name="indent"/>
	<xsl:value-of select="concat('// ',
			      'presence because of mandatory children', $nl)"/>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'presence'"/>
	  <xsl:with-param name="arg"  select="''"/>
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
      </xsl:when>
      <!-- NP container (remove-if-empty is always on context node) -->
      <xsl:when test="xs:annotation/xs:appinfo/remove-if-empty">
	<xsl:call-template name="indent"/>
	<xsl:value-of select="concat('// np container', $nl)"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'presence'"/>
	  <xsl:with-param name="arg"  select="concat('enable ',@name)"/>
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- call with an <xs:element> as context node, it should be a leaf.
       This template will emit a 'mandatory true' if it thinks the
       node should be mandatory. -->
  <xsl:template name="maybe-mandatory-leaf">
<!--    <xsl:if test="(not(@minOccurs = '0') and not(@maxOccurs)) or
		  xs:annotation/xs:appinfo/flag[text() = 'mandatory']"> -->
    <xsl:if test="xs:annotation/xs:appinfo/flag[text() = 'mandatory']">
      <xsl:call-template name="emit-statement">
	<xsl:with-param name="keyw" select="'mandatory'"/>
	<xsl:with-param name="arg" select="'true'"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="maybe-default-value">
    <xsl:if test="@default">
      <xsl:choose>
	<xsl:when test="(xs:complexType/xs:simpleContent/
			 xs:extension/xs:attribute/@name = 'key') or
			(xs:complexType/xs:simpleContent/xs:restriction/@base = /xs:schema/xs:complexType[xs:simpleContent/xs:extension/xs:attribute[@name='key']]/@name)">
	  <!-- key nodes can't have default values -->
	  <xsl:call-template name="indent"/>
	  <xsl:value-of select="concat('// default ',@default,';',$nl)"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:variable name="default">
	    <xsl:choose>
	      <xsl:when test="@default = '0x7FFFFFFF'">
		<xsl:text>2147483647</xsl:text> <!-- sigh -->
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:value-of select="@default"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:variable>
	  <xsl:if test="@default = '0x7FFFFFFF'">
	    <xsl:call-template name="indent"/>
	    <xsl:value-of select="concat('// default ',@default,';',$nl)"/>
	  </xsl:if>
	  <xsl:call-template name="emit-statement">
	    <xsl:with-param name="keyw" select="'default'"/>
	    <xsl:with-param name="arg" select="$default"/>
	  </xsl:call-template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- call with an xs:element which is a list as context node, and
       unless the element has an autosort flag assume ordered-by user. -->
  <xsl:template name="maybe-ordered-by-user">
    <xsl:choose>
      <xsl:when test="xs:annotation/xs:appinfo/flag[text() = 'autosort']"/>
      <!-- <xsl:when test="starts-with(@name, 'interface')"/> -->
      <xsl:otherwise>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'ordered-by'"/>
	  <xsl:with-param name="arg"  select="'user'"/>
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>






  <xsl:template match="/">
    <xsl:text>// This file was generated by </xsl:text>
    <xsl:value-of select="concat($gProgname,$nl)"/>
    <xsl:if test="/xs:schema/xs:import/@schemaLocation and
		  /xs:schema/xs:import/@namespace">
      <xsl:text>// Source: </xsl:text>
      <xsl:value-of
	  select="concat(/xs:schema/xs:import/@schemaLocation, ' ',
			 /xs:schema/xs:import/@namespace,$nl)"/>
    </xsl:if>
    <xsl:if test="$product-filter != ''">
      <xsl:text>// product-filter: </xsl:text>
      <xsl:value-of select="concat($product-filter,$nl)"/>
    </xsl:if>
    <xsl:value-of select="$nl"/>
    <xsl:apply-templates select="*|comment()"/>
  </xsl:template>


  <!--
     - Top level schema tag
    -->
  <xsl:template match="/xs:schema">
    <xsl:call-template name="emit-statement-block">
      <xsl:with-param name="keyw" select="'module'"/>
      <xsl:with-param name="arg"  select="'junos'"/>
    </xsl:call-template>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'namespace'"/>
<!--
      <xsl:with-param name="arg"
		      select="string(namespace::*[name() = 'junos'])"/>
-->
      <xsl:with-param name="arg" select="'http://xml.juniper.net/xnm/1.1/xnm'"/>
      <xsl:with-param name="indent" select="1"/>
    </xsl:call-template>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'prefix'"/>
      <xsl:with-param name="arg"  select="'junos'"/>
      <xsl:with-param name="indent" select="1"/>
    </xsl:call-template>

    <!-- imports -->
    <xsl:if test="$gTailfExt">
      <xsl:call-template name="emit-import-stmt">
	<xsl:with-param name="name" select="'tailf-common'"/>
	<xsl:with-param name="prefix" select="'tailf'"/>
      </xsl:call-template>
    </xsl:if>

    <xsl:if test="xs:import/@schemaLocation and xs:import/@namespace">
      <xsl:text>  description
    "JunOS YANG schema generated by </xsl:text>
      <xsl:value-of select="concat($gProgname,$nl)"/>
      <xsl:value-of select="concat('     Source: ',
                                   xs:import/@schemaLocation, ', ',
			           xs:import/@namespace)"/>
      <xsl:if test="$product-filter != ''">
	<xsl:value-of select="concat($nl, '     Products: ',$product-filter)"/>
      </xsl:if>
      <xsl:value-of select="concat($QC, ';', $nl)"/>
    </xsl:if>
    
    <xsl:if test="$gApplyGroups">
      <xsl:text>
  grouping apply-group {
    leaf-list apply-groups {
      type string;
/*
      type leafref {
        path "/junos:configuration/junos:groups/junos:name";
      }
*/
    }
    leaf-list apply-groups-except {
      type string;
/*
      type leafref {
        path "/junos:configuration/junos:groups/junos:name";
      }
*/
    }
  }
</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*|comment()"/>
    <xsl:call-template name="emit-close-block"/>
  </xsl:template>

  <!-- ignore top-level imports -->
  <xsl:template match="/xs:schema/xs:import"/>

  <!-- preserve comments -->
  <xsl:template match="comment()">
    <!-- skip comments that just have the end-tag -->
    <xsl:if test="not(starts-with(., ' &lt;/'))">
      <xsl:call-template name="indent"/>
      <xsl:text>/* </xsl:text>
      <xsl:value-of select="."/>
      <xsl:text> */</xsl:text>
      <xsl:value-of select="$nl"/>
    </xsl:if>
  </xsl:template>




  <!--
     -  A somewhat simplistic approach to simpleType and its followers...
    -->

  <xsl:template match="xs:simpleType">
    <xsl:apply-templates select="*|comment()"/>
  </xsl:template>

  <xsl:template match="xs:union[parent::xs:simpleType]">
    <xsl:call-template name="emit-statement-block">
      <xsl:with-param name="keyw" select="'type'"/>
      <xsl:with-param name="arg"  select="'union'"/>
    </xsl:call-template>

    <xsl:for-each select="str:tokenize(@memberTypes)">
      <xsl:variable name="utype">
	<xsl:call-template name="translate-type">
	  <xsl:with-param name="type-str" select="."/>
	</xsl:call-template>
      </xsl:variable>
      <xsl:variable name="utype-implicit-restrictions">
	<xsl:call-template name="emit-type-implicit-restriction">
	  <xsl:with-param name="type-str" select="."/>
	  <xsl:with-param name="indent" select="'4'"/>
	</xsl:call-template>
      </xsl:variable>
      <xsl:choose>
	<xsl:when test="$utype-implicit-restrictions != ''">
	  <xsl:call-template name="emit-statement">
	    <xsl:with-param name="keyw" select="'type'"/>
	    <xsl:with-param name="arg"  select="$utype"/>
	    <xsl:with-param name="blockp" select="true()"/>
	    <xsl:with-param name="quotep" select="false()"/>
	    <xsl:with-param name="indent" select="'3'"/>
	  </xsl:call-template>
	  <xsl:value-of select="$utype-implicit-restrictions"/>
	  <xsl:call-template name="emit-close-block">
	    <xsl:with-param name="indent" select="'3'"/>
	  </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:call-template name="emit-statement-unquoted">
	    <xsl:with-param name="keyw" select="'type'"/>
	    <xsl:with-param name="arg" select="$utype"/>
	    <xsl:with-param name="indent" select="'3'"/>
	  </xsl:call-template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:call-template name="emit-close-block"/>
  </xsl:template>

  <xsl:template match="xs:restriction[parent::xs:simpleType or
		       parent::xs:simpleContent]">
    <xsl:variable name="type">
      <xsl:choose>
	<!-- Weird: enumerations that have nodes with the appinfo flag
	     "text-choice" seems to be unions and further more, the
	     nodes that have this flag are *not* enumerations -
	     instead they are types (that sometimes have to be
	     inferred) -->
	<!-- FIXME efficency: seems it would be faster to do a foreach
	     on all the enumeration children here instead (see the
	     test being done on every xs:enumeration below) -->
	<xsl:when test="xs:enumeration/xs:annotation/xs:appinfo/flag
			[text() = 'text-choice']">
	  <xsl:text>union</xsl:text>
	</xsl:when>
	<xsl:when test="child::xs:enumeration and
			not(@base='key-attribute-long-type' and
			    (child::xs:enumeration/@value = '*'))">
	  <xsl:text>enumeration</xsl:text>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:call-template name="translate-type">
	    <xsl:with-param name="type-str" select="@base"/>
	  </xsl:call-template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- In some versions of the JunOS schema an explicit wildcard for
         'unit' is missing, add it in that case. In newer versions it
         *is* there, but as an enumeration with value '*' in something
         that has an xs:long as base-type: a buggy bugfix :-(           -->
    <xsl:variable name="wildcard-union"
		  select="(@base = 'key-attribute-long-type') and
		     ../../../../../../../xs:element[@name='unit'] and
		     contains(../../../../../../xs:annotation/xs:documentation,
			      'wildcard')"/>

    <xsl:if test="$wildcard-union">
      <xsl:call-template name="emit-statement">
	<xsl:with-param name="keyw" select="'type'"/>
	<xsl:with-param name="arg"  select="'union'"/>
	<xsl:with-param name="blockp" select="true()"/>
	<xsl:with-param name="quotep" select="false()"/>
      </xsl:call-template>
    </xsl:if>

    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'type'"/>
      <xsl:with-param name="arg"  select="$type"/>
      <xsl:with-param name="blockp" select="true()"/>
      <xsl:with-param name="quotep" select="false()"/>
    </xsl:call-template>

    <xsl:call-template name="emit-type-implicit-restriction">
      <xsl:with-param name="type-str" select="@base"/>
    </xsl:call-template>

    <!-- Now recurse down through restrictions and enumerations -->
    <xsl:apply-templates select="*|comment()"/>

    <!-- finish of type declaration -->
    <xsl:call-template name="emit-close-block"/>

    <xsl:if test="$wildcard-union">
      <xsl:variable name="d" select="xs:enumeration[@value='*']/xs:annotation
                                     /xs:documentation/text()"/>
      <xsl:call-template name="emit-statement">
	<xsl:with-param name="keyw" select="'type'"/>
	<xsl:with-param name="arg" select="'enumeration'"/>
	<xsl:with-param name="blockp" select="true()"/>
	<xsl:with-param name="quotep" select="false()"/>
      </xsl:call-template>
      <xsl:call-template name="emit-statement">
	<xsl:with-param name="keyw" select="'enum'"/>
	<xsl:with-param name="arg" select="'*'"/>
	<xsl:with-param name="blockp" select="$d != ''"/>
      </xsl:call-template>
      <xsl:if test="$d != ''">
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'description'"/>
	  <xsl:with-param name="arg" select="$d"/>
	</xsl:call-template>
	<xsl:call-template name="emit-close-block"/>
      </xsl:if>
      <xsl:call-template name="emit-close-block"/>
      <xsl:call-template name="emit-close-block"/>
    </xsl:if>

  </xsl:template>


  <!-- Four combinations of min and maxInclusive -->
  <xsl:template match="xs:minInclusive[parent::xs:restriction and ../xs:maxInclusive]">
    <xsl:choose>
      <xsl:when test="@value = ../xs:maxInclusive/@value">
        <xsl:call-template name="emit-statement">
          <xsl:with-param name="keyw" select="'range'"/>
          <xsl:with-param name="arg"  select="@value"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<!-- Workaround for bug in schema -->
	<xsl:choose>
	  <xsl:when test="../../xs:restriction[@base='xsd:unsignedShort'] and (../xs:maxInclusive/@value > 65535)">
	    <xsl:text>/* </xsl:text>
	    <xsl:call-template name="emit-statement">
	      <xsl:with-param name="keyw" select="'range'"/>
	      <xsl:with-param name="arg"  select="concat(@value, ' .. ', ../xs:maxInclusive/@value)"/>
	      <xsl:with-param name="nlp" select="false()"/>
	    </xsl:call-template>
	    <xsl:text> */
</xsl:text>
	    <xsl:call-template name="emit-statement">
	      <xsl:with-param name="keyw" select="'range'"/>
	      <xsl:with-param name="arg"  select="concat(@value, ' .. max')"/>
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:call-template name="emit-statement">
	      <xsl:with-param name="keyw" select="'range'"/>
	      <xsl:with-param name="arg"  select="concat(@value, ' .. ', ../xs:maxInclusive/@value)"/>
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xs:minInclusive[parent::xs:restriction and not(../xs:maxInclusive)]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'range'"/>
      <xsl:with-param name="arg"  select="concat(@value, ' .. max')"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xs:maxInclusive[parent::xs:restriction and not(../xs:minInclusive)]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'range'"/>
      <xsl:with-param name="arg"  select="concat('min .. ', @value)"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xs:maxInclusive[parent::xs:restriction and ../xs:minInclusive]"/>

  <!-- Combinations of min and maxLength -->
  <xsl:template match="xs:minLength[parent::xs:restriction and ../xs:maxLength]">
    <xsl:choose>
      <xsl:when test="@value = ../xs:maxLength/@value">
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'length'"/>
	  <xsl:with-param name="arg"  select="@value"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:when test="@value > ../xs:maxLength/@value">
	<!-- Oops, assume they reversed it (seen in 11.2R1) -->
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'length'"/>
	  <xsl:with-param name="arg"  select="concat(../xs:maxLength/@value,
					             ' .. ', @value)"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'length'"/>
	  <xsl:with-param name="arg"  select="concat(@value, ' .. ',
					             ../xs:maxLength/@value)"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xs:minLength[parent::xs:restriction and not(../xs:maxLength)]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'length'"/>
      <xsl:with-param name="arg"  select="concat(@value, ' .. max')"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xs:maxLength[parent::xs:restriction and not(../xs:minLength)]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'length'"/>
      <xsl:with-param name="arg"  select="concat('min .. ', @value)"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xs:maxLength[parent::xs:restriction and ../xs:minLength]"/>

  <!-- Enumerations -->
  <xsl:template match="xs:enumeration[parent::xs:restriction]">
    <xsl:choose>
      <xsl:when test="(@value = '*') and
	              (../../xs:restriction/@base = 'key-attribute-long-type')">
	<!-- Skip here, bug in schema handled in template for xs:restriction -->
      </xsl:when>
      <xsl:when test="xs:annotation/xs:appinfo/flag[text() = 'text-choice']">
	<!-- this is not an enum, instead it seems to be a type, the
	     problem is we don't know which type so we'll have to
	     guess (defaulting to the base type of the restriction) -->
	<xsl:call-template name="indent"/>
	<xsl:value-of select="concat('/* ',@value,' */',$nl)"/>
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'type'"/>
	  <xsl:with-param name="arg">
	    <xsl:choose>
	      <xsl:when test="/xs:schema/xs:complexType
			      [@name = current()/@value]/xs:simpleContent">
		<!-- assume it is referring to a typedef -->
		<xsl:value-of select="@value"/>
	      </xsl:when>
	      <xsl:when test="@value = 'number'">
		<!-- silly -->
		<xsl:text>int32</xsl:text>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:call-template name="translate-type">
		  <xsl:with-param name="type-str"
				  select="../../xs:restriction/@base"/>
		</xsl:call-template>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:with-param>
	</xsl:call-template>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:when>
      <xsl:when test="($product-filter != '') and
		      xs:annotation/xs:appinfo/products and
		      not(xs:annotation/xs:appinfo/
		          products/product[contains($product-filter,.)])">
	<xsl:message>Filtered enum <xsl:value-of select="@value"/></xsl:message>
      </xsl:when>
      <xsl:when
	  test="../*/xs:annotation/xs:appinfo/flag[text() = 'text-choice']">
	<!-- this enum is part of the union (as described above) we
	     solve it by emitting a complete enumeration block (and
	     assigning it an explicit value to workaround a glitch in
	     the YANG spec: unions of enumerations) -->
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'type'"/>
	  <xsl:with-param name="arg"  select="'enumeration'"/>
	</xsl:call-template>
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'enum'"/>
	  <xsl:with-param name="arg"  select="@value"/>
	</xsl:call-template>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'value'"/>
	  <xsl:with-param name="arg"  select="position()"/>
	  <xsl:with-param name="indent" select="'2'"/>
	</xsl:call-template>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:when>
      <xsl:when test="@idValue != ''">
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'enum'"/>
	  <xsl:with-param name="arg"  select="@value"/>
	</xsl:call-template>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'value'"/>
	  <xsl:with-param name="arg"  select="@idValue"/>
	  <xsl:with-param name="indent" select="'1'"/>
	</xsl:call-template>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'enum'"/>
	  <xsl:with-param name="arg"  select="@value"/>
	</xsl:call-template>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- length restriction -->
  <xsl:template match="xs:length[parent::xs:restriction]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw"  select="'length'"/>
      <xsl:with-param name="arg"   select="@value"/>
      <xsl:with-param name="indent" select="'1'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- pattern restriction -->
  <xsl:template match="xs:pattern[parent::xs:restriction]">
    <xsl:variable name="ptype">
      <xsl:call-template name="translate-type">
	<xsl:with-param name="type-str" select="../@base"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="($ptype = 'string') or ($ptype = 'tailf:hex-list')">
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw"  select="'pattern'"/>
	  <xsl:with-param name="arg"   select="@value"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text> /* WARNING $gProgname: pattern </xsl:text>
	<xsl:value-of select="concat($QC,@value,$QC)"/>
	<xsl:text> is only legal for string type in Yang. */</xsl:text>
	<xsl:value-of select="$nl"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xs:fractionDigits[parent::xs:restriction]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'xs:fraction-digits'"/>
      <xsl:with-param name="arg"  select="@value"/>
    </xsl:call-template>
  </xsl:template>



  <!-- xml schema 'element' -->

  <xsl:template match="xs:element" mode="elem-as-enum">
    <xsl:call-template name="emit-statement-block">
      <xsl:with-param name="keyw" select="'enum'"/>
      <xsl:with-param name="arg" select="@name"/>
    </xsl:call-template>
    <xsl:apply-templates select="xs:annotation"/>
    <xsl:call-template name="emit-close-block"/>
  </xsl:template>

  <xsl:template match="xs:element[not(@name) and @ref = 'junos:comment']"/>

  <xsl:template match="xs:element[not(@name) and @ref = 'undocumented']">
    <xsl:choose>
      <xsl:when test="$gApplyGroups = false()"/>
      <xsl:when test="../../../../xs:annotation/xs:appinfo/flag
		      [(text() = 'no-apply') or (text() = 'oneliner')]"/>
      <!-- don't include in top-configuration, it is manually included there -->
      <xsl:when test="../../../../../xs:element[@name = 'configuration'] and
		      ../../../../../../xs:schema"/>
      <xsl:otherwise>
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'uses'"/>
	  <xsl:with-param name="arg"  select="'apply-group'"/>
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xs:element[@name]">
    <xsl:choose>
      <!-- ignore these elements -->
      <xsl:when test="@name = 'undocumented'"/>
      <xsl:when test="(@name = 'groups') and
		      xs:complexType/xs:sequence/xs:any"/>



      <xsl:when test="(@name = 'configuration') and ../../xs:schema">
        <!-- top-level configuration element, hand-written to include groups -->
	<xsl:text>
  container configuration {
</xsl:text>

        <xsl:if test="$gApplyGroups">
	  <xsl:text>
    uses apply-group;

    list groups {
      key name;
      leaf name {
        type string;
      }
      uses top-configuration;
    }
</xsl:text>
	</xsl:if>
	<xsl:text>
    uses top-configuration;     
</xsl:text>
        <xsl:call-template name="emit-close-block"/>
	<xsl:text>
  grouping top-configuration {
</xsl:text>
        <xsl:apply-templates select="*|comment()"/>
        <xsl:text>
  }

</xsl:text>
      </xsl:when>

      <xsl:when test="(@name = 'dynamic-profiles') and
		      ../../../../../xs:element[@name='configuration'] and
		      ../../../../../../xs:schema and
		      ($gDynamicProfiles = false())">
	<xsl:message>Skipping dynamic-profiles
</xsl:message>
      </xsl:when>


      <xsl:when test="($product-filter != '') and
		      xs:annotation/xs:appinfo/products and
		      not(xs:annotation/xs:appinfo/
		          products/product[contains($product-filter,.)])">
	<xsl:message>Filtered element <xsl:value-of select="@name"/></xsl:message>
      </xsl:when>


      <!-- Filter out elements which would produce empty enumerations
	   (seems like the schema is broken in a couple of places?)
      -->
      <xsl:when test="($product-filter != '') and
		      xs:simpleType/xs:restriction/xs:enumeration/xs:annotation/xs:appinfo/products and
		      (count(xs:simpleType/xs:restriction/xs:enumeration[contains($product-filter,xs:annotation/xs:appinfo/products/product)]) = 0)
">
	<xsl:message>Filtered element (empty enum) <xsl:value-of select="@name"/></xsl:message>	
      </xsl:when>


      <xsl:when test="@type">
	<xsl:choose>

	  <!-- a leaf with an xml-schema type -->
	  <xsl:when test="starts-with(@type,'xsd:')">
	    <xsl:call-template name="emit-statement-block">
	      <xsl:with-param name="keyw" select="'leaf'"/>
	      <xsl:with-param name="arg" select="@name"/>
	    </xsl:call-template>
	    <xsl:call-template name="emit-statement">
	      <xsl:with-param name="keyw" select="'type'"/>
	      <xsl:with-param name="arg">
		<xsl:call-template name="translate-type">
		  <xsl:with-param name="type-str" select="@type"/>
		</xsl:call-template>
	      </xsl:with-param>
	      <xsl:with-param name="indent" select="1"/>
	    </xsl:call-template>
	    <xsl:call-template name="maybe-mandatory-leaf"/>
	    <xsl:call-template name="maybe-default-value"/>
	    <xsl:apply-templates select="*|comment()"/>
	    <xsl:call-template name="emit-close-block"/>
	  </xsl:when>

	  <!-- an element that refers to a globally defined
	       complexType is either a leaf(-list) with a typedef, or
	       an inline sequence of definitions -->
	  <xsl:when test="/xs:schema/xs:complexType[@name = current()/@type]">
	    <xsl:variable name="type"
			  select="/xs:schema/xs:complexType[@name = current()/@type]"/>
	    <xsl:choose>
	      <!-- a leaf(-list) referring to a typedef -->
	      <xsl:when test="$type/xs:simpleContent">
		<xsl:call-template name="emit-statement-block">
		  <xsl:with-param name="keyw">
		    <xsl:choose>
		      <xsl:when test="@maxOccurs">
			<xsl:text>leaf-list</xsl:text>
		      </xsl:when>
		      <xsl:otherwise>
			<xsl:text>leaf</xsl:text>
		      </xsl:otherwise>
		    </xsl:choose>
		  </xsl:with-param>
		  <xsl:with-param name="arg" select="@name"/>
		</xsl:call-template>
		<xsl:if test="not(@maxOccurs)">
		  <xsl:call-template name="maybe-mandatory-leaf"/>
		  <xsl:call-template name="maybe-default-value"/>
		</xsl:if>
		<xsl:call-template name="emit-statement">
		  <xsl:with-param name="keyw" select="'type'"/>
		  <xsl:with-param name="arg" select="@type"/>
		  <xsl:with-param name="indent" select="1"/>
		</xsl:call-template>
		<xsl:apply-templates select="*|comment()"/>
		<xsl:call-template name="emit-close-block"/>
	      </xsl:when>

	      <!-- a container or list -->
	      <xsl:when test="$type/xs:sequence">
<!--
		<xsl:message>element <xsl:value-of select="concat(@name, ' ', @type)"/>
		</xsl:message>
-->
		<xsl:variable name="node-type">
		  <xsl:choose>
		    <xsl:when test="@type = 'dynamic-ifbw-parms-type'">
		      <!-- Bug in junos schema? Seems elements with
		           this type always has a maxOccurs attribute,
		           but not a key -->
		      <xsl:text>container</xsl:text>
		    </xsl:when>
		    <xsl:when test="@maxOccurs">
		      <xsl:text>list</xsl:text>
		    </xsl:when>
		    <xsl:otherwise>
		      <xsl:text>container</xsl:text>
		    </xsl:otherwise>
		  </xsl:choose>
		</xsl:variable>

		<xsl:call-template name="emit-statement-block">
		  <xsl:with-param name="keyw" select="$node-type"/>
		  <xsl:with-param name="arg" select="@name"/>
		</xsl:call-template>

		<xsl:choose>
		  <xsl:when test="$node-type = 'container'">
		    <xsl:call-template name="container-type">
		      <xsl:with-param name="cnode" select="$type"/>
		    </xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise>
		    <!-- it is a list -->
		    <xsl:call-template name="emit-key-statement">
		      <xsl:with-param name="cnode" select="$type/xs:sequence"/>
		      <xsl:with-param name="indent" select="1"/>
		    </xsl:call-template>
		    <xsl:call-template name="maybe-ordered-by-user"/>
		  </xsl:otherwise>
		</xsl:choose>

		<xsl:apply-templates select="*|comment()"/>
		
		<!-- expand inline or use grouping? -->
<!--
	    <xsl:apply-templates select="$type/*|$type/comment()"/>
-->
                <xsl:call-template name="emit-statement">
		  <xsl:with-param name="keyw" select="'uses'"/>
		  <xsl:with-param name="arg"  select="@type"/>
		  <xsl:with-param name="indent" select="1"/>
		</xsl:call-template>
		
		<xsl:call-template name="emit-close-block"/>
	      </xsl:when>

	      
	      <xsl:otherwise>
		<xsl:text>/* UNHANDLED XXX */
</xsl:text>
		<xsl:call-template name="unhandled"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:text>/* UNHANDLED ELEMENT WITH @TYPE */
</xsl:text>
            <xsl:call-template name="unhandled"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>


      <!-- Now we know @name exists, and @type doesn't -->

      <!-- a leaf (or leaf-list) with an inline type -->
      <xsl:when test="xs:simpleType">
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw">
	    <xsl:choose>
	      <xsl:when test="@maxOccurs">
		<xsl:text>leaf-list</xsl:text>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:text>leaf</xsl:text>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:with-param>
	  <xsl:with-param name="arg" select="@name"/>
	</xsl:call-template>
	<xsl:if test="not(@maxOccurs)">
	  <xsl:call-template name="maybe-mandatory-leaf"/>
	  <xsl:call-template name="maybe-default-value"/>
	</xsl:if>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:when>

      <xsl:when test="xs:complexType">
	<xsl:variable name="node-type">
	  <xsl:choose>
	    <xsl:when test="xs:complexType/xs:simpleContent and @maxOccurs">
	      <xsl:text>leaf-list</xsl:text>
	    </xsl:when>
	    <xsl:when test="xs:complexType/xs:simpleContent or
			    not(xs:complexType/node())">
	      <xsl:text>leaf</xsl:text>
	    </xsl:when>
	    <xsl:when test="@maxOccurs">
	      <xsl:text>list</xsl:text>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:text>container</xsl:text>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="$node-type"/>
	  <xsl:with-param name="arg" select="@name"/>
	</xsl:call-template>
	
	<xsl:choose>
	  <xsl:when test="$node-type = 'container'">
	    <xsl:call-template name="container-type"/>
	  </xsl:when>
	  <xsl:when test="$node-type = 'leaf'">
	    <xsl:call-template name="maybe-mandatory-leaf"/>
	    <xsl:if test="xs:complexType/xs:simpleContent">
	      <xsl:call-template name="maybe-default-value"/>
	    </xsl:if>
	  </xsl:when>
	</xsl:choose>

	<xsl:if test="$node-type = 'list'">
          <xsl:call-template name="emit-key-statement">
	    <xsl:with-param name="cnode" select="xs:complexType/xs:sequence"/>
	    <xsl:with-param name="indent" select="1"/>
	  </xsl:call-template>
	  <xsl:call-template name="maybe-ordered-by-user"/>
	</xsl:if>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:when>
      
      <xsl:otherwise>
	<xsl:text>/* UNHANDLED ELEMENT */
</xsl:text>
	<xsl:call-template name="unhandled"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template match="xs:sequence">
    <xsl:apply-templates select="*|comment()"/>
  </xsl:template>

  <xsl:template match="xs:choice">
    <xsl:choose>
      <!-- A choice with both minOccurs and maxOccurs seems to just be
           a way of saying "these can come in any order" -->
      <xsl:when test="(@minOccurs = 0) and (@maxOccurs = 'unbounded')">
	<xsl:apply-templates select="*|comment()"/>
      </xsl:when>

      <!-- bug in the schema? there are some 'choice' nodes with a
           single choice as a child (we skip the first choice here) -->
      <xsl:when test="xs:choice and (count(*) = 1)">
	<xsl:apply-templates select="*|comment()"/>
      </xsl:when>

      <!-- Choice with minOccurs and choice with no attributes at all
           seems to be a "real" choice -->
<!--
      <xsl:when test="@minOccurs = 0">
	<xsl:text>/* CHOICE WITH MINOCCURS=0 */
</xsl:text>
	<xsl:apply-templates select="*|comment()"/>
      </xsl:when>
-->

      <xsl:when test="../../xs:choice and not(../../xs:choice/@*)">
	<!-- Flatten choices with choices as parents for now -->
	<xsl:text>/* FIXME skipped one level of choice here */
</xsl:text>
        <xsl:apply-templates select="*|comment()"/>
      </xsl:when>

      <xsl:when test="../../xs:choice[@minOccurs] and not(../../xs:choice/@maxOccurs)">
	<!-- Flatten choices with choices as parents for now -->
	<xsl:text>/* FIXME skipped one level of choice here */
</xsl:text>
        <xsl:apply-templates select="*|comment()"/>
      </xsl:when>

      <xsl:when test="(count(@*) = 0) and
		      xs:element/xs:complexType/xs:attribute[@name='key']">
	<!-- This choice is also a key, which isn't allowed in
	     YANG. We solve that by converting the choice to an
	     enumeration. *But* in some cases (some of) the choice
	     elements have values (i.e. simpleContent). That is solved
	     by adding another key.
	-->
	<xsl:variable name="key-name">
	  <!-- make up a name for the key (must match emit-key-statement) -->
	  <xsl:variable name="ctxt-id" select="generate-id(.)"/>
	  <xsl:for-each select="../xs:element|../xs:choice">
	    <xsl:if test="generate-id(.) = $ctxt-id">
	      <xsl:value-of select="concat('key', position())"/>
	    </xsl:if>
	  </xsl:for-each>
	</xsl:variable>
	<!-- The enumeration... -->
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'leaf'"/>
	  <xsl:with-param name="arg" select="$key-name"/>
	</xsl:call-template>
	<xsl:if test="$gTailfExt">
	  <xsl:call-template name="emit-statement">
	    <xsl:with-param name="keyw" select="'tailf:junos-val-as-xml-tag'"/>
	    <xsl:with-param name="argp" select="false()"/>
	    <xsl:with-param name="indent" select="1"/>
	  </xsl:call-template>
	</xsl:if>
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'type'"/>
	  <xsl:with-param name="arg" select="'enumeration'"/>
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
	<xsl:apply-templates select="*|comment()" mode="elem-as-enum"/>
	<xsl:call-template name="emit-close-block">
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
	<xsl:call-template name="emit-close-block"/>
	<!-- ...and if we have elements with simpleContent, the extra key -->
	<xsl:if test="xs:element/xs:complexType/xs:simpleContent">
	  <xsl:call-template name="emit-statement-block">
	    <xsl:with-param name="keyw" select="'leaf'"/>
	    <xsl:with-param name="arg" select="concat($key-name,'-arg')"/>
	  </xsl:call-template>
	  <xsl:if test="$gTailfExt">
	    <xsl:call-template name="emit-statement">
	      <xsl:with-param name="keyw"
			      select="'tailf:junos-val-with-prev-xml-tag'"/>
	      <xsl:with-param name="argp" select="false()"/>
	      <xsl:with-param name="indent" select="1"/>
	    </xsl:call-template>
	    <xsl:call-template name="emit-statement">
	      <xsl:with-param name="keyw" select="'tailf:key-default'"/>
	      <xsl:with-param name="arg" select="''"/>
	      <xsl:with-param name="indent" select="1"/>
	    </xsl:call-template>
	  </xsl:if>
	  <xsl:call-template name="emit-statement-block">
	    <xsl:with-param name="keyw" select="'type'"/>
	    <xsl:with-param name="arg" select="'union'"/>
	    <xsl:with-param name="indent" select="1"/>
	  </xsl:call-template>
	  <xsl:for-each select="xs:element/xs:complexType[xs:simpleContent]">
	    <xsl:call-template name="indent"/>
	    <xsl:value-of select="concat('// type of enum: ', ../@name, $nl)"/>
	    <xsl:apply-templates select="."/>
	  </xsl:for-each>
	  <xsl:call-template name="emit-close-block">
	    <xsl:with-param name="indent" select="1"/>
	  </xsl:call-template>
	  <xsl:call-template name="emit-close-block"/>
	</xsl:if>
      </xsl:when>

      <xsl:otherwise>
	<xsl:call-template name="emit-statement-block">
	  <xsl:with-param name="keyw" select="'choice'"/>
	  <xsl:with-param name="arg" select="concat('choice-',position())"/>
	</xsl:call-template>
	<xsl:apply-templates select="*|comment()"/>
	<xsl:call-template name="emit-close-block"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template match="xs:complexType">
    <xsl:choose>
      <xsl:when test="not(node())">
	<xsl:call-template name="emit-statement">
	  <xsl:with-param name="keyw" select="'type'"/>
	  <xsl:with-param name="arg" select="'empty'"/>
	  <xsl:with-param name="indent" select="1"/>
	</xsl:call-template>
      </xsl:when>

      <xsl:when test="xs:simpleContent">
	<xsl:choose>
	  <!-- (skip an unused type with illegal name for now) -->
	  <xsl:when test="../../xs:schema and (@name = 'bits')"/>

	  <xsl:when test="../../xs:schema">
	    <!-- at top-level, it is a typedef -->
	    <xsl:call-template name="emit-statement-block">
	      <xsl:with-param name="keyw" select="'typedef'"/>
	      <xsl:with-param name="arg" select="@name"/>
	    </xsl:call-template>
	    <xsl:apply-templates select="*|comment()"/>
	    <xsl:call-template name="emit-close-block"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- otherwise it is an inline type -->
	    <xsl:apply-templates select="*|comment()"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>

      <xsl:when test="xs:sequence">
	<xsl:choose>
	  <xsl:when test="../../xs:schema">
	    <!-- complexType at top-level with a sequence is treated
	         as grouping -->
	    <!-- or we expand these inline instead? -->
	    <!-- -->
	    <xsl:call-template name="emit-statement-block">
	      <xsl:with-param name="keyw" select="'grouping'"/>
	      <xsl:with-param name="arg" select="@name"/>
	    </xsl:call-template>
	    <xsl:apply-templates select="xs:sequence/*|xs:sequence/comment()"/>
	    <xsl:call-template name="emit-close-block"/>
	    <!-- -->
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- normally just keep going -->
	    <xsl:apply-templates select="*|comment()"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>

      <xsl:otherwise>
	<xsl:text>/* HERE */
</xsl:text>
	<xsl:call-template name="unhandled"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

  <xsl:template match="xs:simpleContent">
    <xsl:apply-templates select="*|comment()"/>
  </xsl:template>

  <xsl:template match="xs:extension[parent::xs:simpleContent]">
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'type'"/>
      <xsl:with-param name="arg">
	<xsl:call-template name="translate-type">
	  <xsl:with-param name="type-str" select="@base"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xs:attribute[@name = 'key' and @fixed = 'key']"/>

  <xsl:template match="xs:annotation">
    <xsl:apply-templates select="*|comment()"/>
  </xsl:template>

  <xsl:template match="xs:documentation[../../xs:annotation]">
    <xsl:if test="../xs:appinfo/flag[text() = 'text-choice']">
      <!-- FIXME pyang doesn't allow a 'description' within a type -->
      <xsl:text>// </xsl:text>
    </xsl:if>
    <xsl:call-template name="emit-statement">
      <xsl:with-param name="keyw" select="'description'"/>
      <xsl:with-param name="arg" select="text()"/>
      <xsl:with-param name="indent" select="-1"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xs:appinfo">
    <xsl:apply-templates select="*|comment()"/>
  </xsl:template>

  <xsl:template match="remove-if-empty"/>
  <xsl:template match="identifier"/>

  <xsl:template match="flag">
    <xsl:choose>
      <!-- these flags are accounted for -->
      <xsl:when test=". = 'identifier'"/>
      <xsl:when test=". = 'mandatory'"/>
      <xsl:when test=". = 'remove-empty'"/>
      <xsl:when test=". = 'text-choice'"/>
      <xsl:when test=". = 'autosort'"/>
      <xsl:when test=". = 'no-apply'"/>

      <!-- quietly ignored -->
      <xsl:when test=". = 'ranged'"/>
      <xsl:when test=". = 'current-product-support'"/>

      <!-- the rest are ignored, but kept as comments -->
      <xsl:otherwise>
	<xsl:call-template name="indent"/>
	<xsl:value-of select="concat('// flag: ', ., $nl)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="products">
    <xsl:text>/* products: </xsl:text>
    <xsl:for-each select="product">
      <xsl:value-of select="concat(text(), ' ')"/>
    </xsl:for-each>
    <xsl:text> */
</xsl:text>
  </xsl:template>

  <xsl:template match="interface-acceptable">
    <xsl:text>/* interface-acceptable prefixes: </xsl:text>
    <xsl:for-each select="interface-prefix">
      <xsl:value-of select="concat(text(), ' ')"/>
    </xsl:for-each>
    <xsl:text> */
</xsl:text>
  </xsl:template>

  <xsl:template match="interface-exclude">
    <xsl:text>/* interface-exclude prefixes: </xsl:text>
    <xsl:for-each select="interface-prefix">
      <xsl:value-of select="concat(text(), ' ')"/>
    </xsl:for-each>
    <xsl:text> */
</xsl:text>
  </xsl:template>




  <!-- catch-all to track progress (when nothing is unhandled we are done:-) -->
  <xsl:template match="*">
    <xsl:call-template name="unhandled"/>
  </xsl:template>



  <xsl:template match="*" mode="in-comment">
<!--    <xsl:value-of select="$nl"/> -->
<!--     <xsl:call-template name="indent"/> -->
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:for-each select="@*">
      <xsl:value-of select="concat(' ', name(),'=')"/>
      <xsl:text>&quot;</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&quot;</xsl:text>
    </xsl:for-each>
    <xsl:text>&gt;</xsl:text>
    <xsl:if test="count(*) > 0">
      <xsl:value-of select="$nl"/>
    </xsl:if>
    <xsl:apply-templates select="node()" mode="in-comment"/>
<!--
    <xsl:if test="not((preceding-sibling::node()[1])/text())">
      <xsl:value-of select="$nl"/>
    </xsl:if>
-->
    <xsl:text>&lt;/</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>&gt;</xsl:text>
<!-- 
    <xsl:if test="count(preceding-sibling::*|following-sibling::*) > 0">
      <xsl:value-of select="$nl"/>
    </xsl:if>
-->
    <xsl:value-of select="$nl"/>
  </xsl:template>

  <xsl:template match="text()" mode="in-comment">
    <xsl:value-of select="."/>
  </xsl:template>


  <!-- debugging -->
  <xsl:template match="text()">
    <xsl:text>/* Skipped text:
</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>*/
</xsl:text>
  </xsl:template>


</xsl:stylesheet>
