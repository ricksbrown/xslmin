package com.github.ricksbrown.xslmin;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collapses xsl:element and xsl:attribute elements to their short forms.
 *
 * <xsl:element name="foo"/> gets rewritten to <foo/>
 *
 * <xsl:element name="foo">
 * 	<xsl:attribute name="bar" select="$foobar"/>
 * </xsl:element>
 *
 * gets rewritten to: <foo bar="{$foobar}"/>
 *
 * This should not cause problems with the correct output type set, but just in case,
 * the following HTML Elements are not allowed to have an end tag:

	AREA:	http://www.w3.org/TR/html401/struct/objects.html#edef-AREA
	BASE:	http://www.w3.org/TR/html401/struct/links.html#edef-BASE
	BR:		http://www.w3.org/TR/html401/struct/text.html#edef-BR
	COL:	http://www.w3.org/TR/html401/struct/tables.html#edef-COL
	HR:		http://www.w3.org/TR/html401/present/graphics.html#edef-HR
	IMG:	http://www.w3.org/TR/html401/struct/objects.html#edef-IMG
	INPUT:	http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT
	LINK:	http://www.w3.org/TR/html401/struct/links.html#edef-LINK
	META:	http://www.w3.org/TR/html401/struct/global.html#edef-META
	PARAM:	http://www.w3.org/TR/html401/struct/objects.html#edef-PARAM
 *
 * TODO full rewrite to benefit from the new treewalker "way of the samurai"
 *
 * @author Rick Brown
 */
public class XslElementCollapser
{
	private List<Node> elementsToCollapse;

	public XslElementCollapser()
	{
		elementsToCollapse = new ArrayList<Node>();
	}

	public void rewriteElements() throws XPathExpressionException
	{
		for(Node next : elementsToCollapse)
		{
			Document doc = next.getOwnerDocument();
			String elementName = NamedNode.getNodeName(next);
			Element element = doc.createElement(elementName);
			collapseAttributes(next, element);
			while(next.hasChildNodes())
			{
				Node nextChild = next.getFirstChild();
				if(nextChild.getNodeType() == Node.ELEMENT_NODE)
				{
					element.appendChild(nextChild);
				}
				else
				{
					next.removeChild(nextChild);
				}
			}
			next.getParentNode().replaceChild(element, next);
		}
	}

	public void addCandidate(Node xslElement)
	{
		elementsToCollapse.add(xslElement);
	}

	/**
	 * Does the attribute collapsing.
	 */
	private void collapseAttributes(Node oldNode, Element newNode) throws XPathExpressionException
	{
		NodeList attrElements = XpathUtils.executeQuery(oldNode, "xsl:attribute[count(text()|./xsl:text|./xsl:value-of)=count(node())]");
		for(int i=0; i<attrElements.getLength(); i++)//for each xsl:attribute element
		{
			Node next = oldNode.removeChild(attrElements.item(i));
			NodeList nextKids = next.getChildNodes();
			String attributeName = NamedNode.getNodeName(next);
			StringBuilder attributeValue = new StringBuilder();
			for(int j=0; j < nextKids.getLength(); j++)//for each child of the xsl:attribute element
    		{
				Node kid = nextKids.item(j);
				if(kid.getNodeType() == Node.TEXT_NODE)
				{
					String nextVal = kid.getNodeValue().trim();
					if(nextVal.length() > 0)
					{
						attributeValue.append(nextVal);
	}
}
				else if(kid.getNodeName().equals("xsl:text"))
				{
					attributeValue.append(kid.getTextContent());
				}
				else if(kid.getNodeName().equals("xsl:value-of"))
				{
					String valSelect = kid.getAttributes().getNamedItem("select").getNodeValue();
					/*
					 * useful regexps:
					 * "^(['|\"])(.+)\\1$" string literal
					 * "^\\{?\\$[^}]+\\}?$" variable reference (with or without curly braces)
					 * "^\\d+$" numeric
					 * "^(?:[a-z]+:)?[a-z-]+\\([^)]+\\)$" xpath function
					 */
					if(valSelect.matches("^(['|\"])(.+)\\1$"))
					{
						attributeValue.append(valSelect.replaceAll("^['|\"]|['|\"]$", ""));
					}
					else
					{
						attributeValue.append("{");
						attributeValue.append(valSelect);
						attributeValue.append("}");
					}
				}
				else
				{
					System.out.println("What the...? " + kid.getNodeName());
				}
    		}
			if(attributeValue.length() > 0)
			{
				newNode.setAttribute(attributeName, attributeValue.toString());
			}
		}
	}
}
