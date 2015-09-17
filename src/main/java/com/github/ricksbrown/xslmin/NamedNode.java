/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ricksbrown.xslmin;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Knows "stuff" about xsl elements which have a name attribute (e.g. xsl:variable
 * and xsl:param) and which can be referenced by other elements.
 *
 * "Stuff" includes things like:
 * - how to rename them
 * - what other nodes reference them
 * - how to rename other nodes that refer to them
 *
 * @author Rick Brown
 */
public class NamedNode
{
	private List<Node> referenceAttributes = new ArrayList<Node>();//attibute nodes which reference this node
	Node node;

	/**
	 * @param node the node who we are asking "what's your name"
	 * @return The value of the "name" attribute on this node, or null.
	 */
	public static String getNodeName(final Node node)
	{
		String result = null;
		if(node != null)
		{
			Node nameNode = node.getAttributes().getNamedItem("name");
			if(nameNode != null)
			{
				result = nameNode.getNodeValue();
			}
		}
		return result;
	}

	/**
	 * @param node The named node which this class encapsulates.
	 */
	public NamedNode(final Node node)
	{
		this.node = node;
	}

	/**
	 * If you call this constructor you are agreeing to populate the node member
	 * variable before methods on this instance are called.
	 */
	public NamedNode()
	{
		this.node = null;
	}

	/**
	 *
	 * @return
	 */
	public String getNodeName()
	{
		return NamedNode.getNodeName(this.node);
	}

	/**
	 * @return true if any other nodes in the stylesheet reference this one
	 */
	public boolean hasReferences()
	{
		return referenceAttributes.size() > 0;
	}

	/**
	 * Allows late setting of the node, but only if it has not already been set
	 * @param node
	 */
	public void setNode(final Node node)
	{
		//TODO oh boy, the whole thing needs to be rewritten to use Element instead of node
		Element element = (Element) node;
		
		if(this.node == null)
		{
			this.node = element;
		}
		else
		{
			Element thisElement = (Element) this.node;
			if(thisElement.getTagName().equals(element.getTagName()))
			{
				throw new IllegalStateException("Variable redeclared in the same template: " + element.getAttribute("name"));
				/* This doesn't really work. Sigh, should have used sax parser instead of treewalker.
				if(thisElement.getParentNode() == element.getParentNode())
				{
					System.out.println("ERROR: Duplicate variable declarations " + element.getAttribute("name"));
				}
				else
				{
					//still dodgy in XSLT 1.0 but we'll try anyway
					System.out.println("WARN: Duplicate variable declarations " + element.getAttribute("name"));
					this.addReference(element.getAttributeNode("name"));//TODO this will throw out the reference count
				}
				*/
			}
			else
			{
				throw new IllegalStateException("The node should not be re-set " + element.getAttribute("name"));
			}
		}
	}

	/**
	 * Register an attribute which contains a reference to this node.
	 * For example if the node is an xsl:variable called "foo" then the
	 * attribute may be the select from an xsl:value-of which contains a
	 * reference to "$foo".
	 * @param attribute
	 */
	public void addReference(final Node attribute)
	{
		if(attribute.getNodeType() == Node.ATTRIBUTE_NODE)
		{
			referenceAttributes.add(attribute);
		}
		else
		{
			throw new IllegalArgumentException("Attribute nodes only");
		}
	}

	/**
	 *
	 * @param newName The new name for the underlying node
	 * @return true if the node was renamed
	 */
	public boolean rename(final String newName)
	{
		return rename(newName, "\\$\\b%s\\b(?!['\\-\\._])", "\\$");
	}

	/**
	 *
	 * @param newName The new name for the underlying node
	 * @param usageRe The regular expression used to replace the oldName in any
	 * reference attributes. The regex should be printf formatted, it will be
	 * formatted with one arg, the node's old name.
	 * @param prefix If the regex matches this prefix will be appended to the
	 * new name in the replacement. It can be an empty string.
	 *
	 * Note: if the reference attribute is an EXACT match for the node name the
	 * regex will be ignored (and the prefix will not be used, it will be a one
	 * for one replacement). The regex is only used when there is some searching
	 * to be done in the reference attribute.
	 *
	 * @return true if the node was renamed
	 */
	boolean rename(final String newName, final String usageRe, final String prefix)
	{
		boolean result = false;
		if(node != null)
		{
			Node nameAttr = node.getAttributes().getNamedItem("name");
			if(nameAttr != null)
			{
				String nodeName = nameAttr.getNodeValue();
				//System.out.println("Renaming " + nameNode.getNodeValue() + " to " + newName);
				String matchRe = String.format(usageRe, nodeName);
				nameAttr.setNodeValue(newName);
				for(Node attr : referenceAttributes)
				{
					String attributeValue = attr.getNodeValue();
					if(nodeName.equals(attributeValue))
					{
						/*
						 * If the node is an xsl:param then there are two ways to reference it:
						 * 1. With a '$', e.g. $nodeName
						 * 2. As the name attribute of an xsl:with-param, e.g. <xsl:with param name="nodeName"
						 */
						attr.setNodeValue(newName);
						result = true;
					}
					else
					{
						//System.out.println("Renaming " + attributeValue + " with " + newName);
						attr.setNodeValue(attributeValue.replaceAll(matchRe, prefix + newName));
						result = true;
						 //System.out.println("Renamed to " + attr.getNodeValue());
					}
				}
			}
		}
		return result;
	}
}
