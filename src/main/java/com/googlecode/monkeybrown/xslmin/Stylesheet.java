package com.googlecode.monkeybrown.xslmin;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

/**
 * Represents an xsl:stylesheet.
 *
 * Knows about all of the parts of the stylesheet that need renaming, including:
 * xsl:template
 * xsl:param
 * xsl:variable
 *
 * @author Rick Brown
 */
public class Stylesheet extends Scope
{
	private static final String REF_TO_VAR_RE =  "\\$(\\b['\\-\\._\\w]+\\b)";
	private final Pattern refToVarRe;
	private Map<String, Scope> templates = new HashMap<String, Scope>();
	private XslElementCollapser xslElementCollapser;
	NameGenerator nameGenerator;


	/**
	 * Creates a new instance of Stylesheet - duh.
	 * @param rootNode The stylesheet element.
	 */
	public Stylesheet(final Node rootNode)
	{
		super(rootNode);
		this.dontRenameParams = true;
		nameGenerator = new NameGenerator();
		xslElementCollapser = new XslElementCollapser();
		refToVarRe = Pattern.compile(REF_TO_VAR_RE);
		Document document = rootNode.getOwnerDocument();
		DocumentTraversal traversal = (DocumentTraversal) document;
		TreeWalker walker = traversal.createTreeWalker(rootNode, NodeFilter.SHOW_ELEMENT, null, true);
		walkLevel(walker, this);
	}

	/**
	 * Fetch an instance of Scope which represents a previously registered template.
	 * @param name The name the template is registered against.
	 */
	public Scope getTemplate(final String name)
	{
		return templates.get(name);
	}

	/**
	 * Removes any elements that are not referenced anywhere in the stylesheet.
	 * The obvious danger is if this stylesheet is referenced by another then
	 * we don't have the full picture.
	 *
	 * TODO: When we remove a node it is possible that other nodes are no longer being
	 * referenced anymore - we should ensure proper cleanup is carried out.
	 *
	 * @param preserve If true the names of unused elements will be printed and not removed
	 * @return the total number of items removed
	 */
	public int processAllUnused(final boolean preserve)
	{
		int result = 0, removed = this.processUnused(preserve);
		String verb = preserve? "Unused " : "Removed ";
		if(removed > 0)
		{
			System.out.println(verb + removed + " in global scope");
		}
		List<String> toRemove = new ArrayList<String>();
		Iterator<String> it = templates.keySet().iterator();
		while(it.hasNext())
		{
			String nextName = it.next();
			Scope scope = templates.get(nextName);
			//checking 'dontRenameParams' is a bit of a hack but it is faster than looking up the match attribute again
			if(!scope.dontRenameParams && !scope.hasReferences())
			{
				//if we are in here then "scope" is a template with no match attribute (and no references), therefore it MUST have a name
				toRemove.add(nextName);
			}
			else
			{
				removed = scope.processUnused(preserve);
				if(removed > 0)
				{
					result += removed;
					System.out.println(verb + removed + " in " + nextName);
				}

			}
		}
		for(String nextName : toRemove)
		{
			if(!preserve)
			{
				Scope.removeNode(nextName, templates);
			}

			System.out.println(verb + " template: " + nextName);
		}
		return result + toRemove.size();
	}

	/**
	 * Rename all named nodes in this scope.
	 */
	@Override
	public void renameAllScoped()
	{
		super.renameAllScoped();
		renameAllTemplates();
	}

	public void collapseElements()
	{
		try
		{
			xslElementCollapser.rewriteElements();
		}
		catch(XPathExpressionException ex)
		{
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * Add a template to the scope of this stylesheet.
	 * If the template has a name it will be registered against that name.
	 *
	 * @param template An xsl:template node.
	 * @return A new instance of Scope representing this template
	 */
	private Scope addTemplate(final Node template)
	{
		String name = getNodeName(template);
		if(name == null)
		{
			name = UUID.randomUUID().toString();

		}
		return addTemplate(template, name);
	}

	/**
	 * Add a template to the scope of this stylesheet and register it to the name provided
	 * regardless of what its name attribute is.
	 *
	 * @param template An xsl:template node.
	 * @param name The name to register this template against
	 * @return A new instance of Scope representing this template
	 */
	private Scope addTemplate(final Node template, final String name)
	{
		Scope result = new Scope(template, super.nameGenerator);
		templates.put(name, result);
		return result;
	}

	/**
	 * Rename all named templates in this stylesheet.
	 */
	private void renameAllTemplates()
	{
		Iterator<String> it = templates.keySet().iterator();
		while(it.hasNext())
		{
			String nextName = it.next();
			Scope scope = templates.get(nextName);
			scope.rename(nameGenerator.getNextName(this));
			scope.renameAllScoped();
		}
	}

	/**
	 * Look for any likely references to variables or param in all the attributes on the node
	 * and register them in the provided scope.
	 * @param node The node whose attributes we will scan.
	 * @param scope The scope the reference should be registered to.
	 */
	private void findReferences(final Node node, final Scope scope)
	{
		NamedNodeMap attributes = node.getAttributes();
		for(int i=0; i<attributes.getLength(); i++)
		{
			Node attr = attributes.item(i);
			String nodeVal = attr.getNodeValue();
			Matcher m = refToVarRe.matcher(nodeVal);
			while(m.find())
			{
				String refTo = m.group(1);
				//System.out.println("Found reference to " + refTo + " in " + nodeVal);
				if(scope.contains(refTo))
				{
					scope.addReferenceToScoped(refTo, attr);
				}
				else//it's global
				{
					if(this.contains(refTo))
					{
						this.addReferenceToScoped(refTo, attr);
					}
					else
					{
						this.addScoped(null, new NamedNode(), refTo);
						this.addReferenceToScoped(refTo, attr);
					}
				}
			}
		}
	}

	/**
	 * Walk a level of the DOM and do "stuff" with the nodes we encounter on the way.
	 * @param walker Our treewalker.
	 * @param currentScope The scope any variable declarations etc will be registered against.
	 */
	private void walkLevel(final TreeWalker walker, final Scope currentScope)
	{
		Node current = walker.getCurrentNode();
		String tagName = ((Element) current).getTagName();

		Scope newScope = null;
		if(tagName.equals("xsl:template"))
		{
			/**
			 * When we encounter a template:
			 * - Enter a new scope - all variable and param declarations are scoped to it (they
			 * are actually scoped to their parent but it amounts to the same thing because you
			 * can't declare the same variable/param twice in the same template even in a different
			 * scope).
			 * - Add the template to the Stylesheet instance. It may have already been registered
			 * (if we encountered a call to this template earlier) in which case we need to add this
			 * node to the registered instance.
			 */
			String name = getNodeName(current);
			Node match = current.getAttributes().getNamedItem("match");
			if(name != null)
			{
				newScope = this.getTemplate(name);

				if(newScope != null)
				{
					newScope.setNode(current);
				}
				else
				{
					newScope = addTemplate(current, name);
				}
			}
			else
			{
				if(match != null)//this MUST be true if the name is not set
				{
					Node priority = current.getAttributes().getNamedItem("priority");
					Node mode = current.getAttributes().getNamedItem("mode");
					name = "@match=" + match.getNodeValue();
					if(priority != null)
					{
						name += "@priority=" + priority.getNodeValue();
					}
					if(mode != null)
					{
						name += "@mode=" + mode.getNodeValue();
					}
					newScope = this.getTemplate(name);
					if(newScope != null)
					{
						if(newScope.node == null)
						{
							newScope.setNode(current);
						}
						else
						{
							System.out.println("WARNING: Duplicate template: " + name);
							newScope = this.addTemplate(current);
						}
					}
					else
					{
						newScope = addTemplate(current, name);
					}
				}
				else
				{
					System.out.println("WARNING: Template with no name and no match! What the?");
					newScope = this.addTemplate(current);
				}

			}
			if(match != null)
			{
				newScope.dontRenameParams = true;
			}
		}
		else if(tagName.equals("xsl:call-template"))
		{
			/*
			 * Note, it is not possible to find this sort of construct: <xsl:call-template name="$somevar"/>
			 */
			String name = getNodeName(current);
			Scope callingTemplate = this.getTemplate(name);
			if(callingTemplate == null)
			{
				callingTemplate = addTemplate(null, name);
			}
			callingTemplate.addReference(current.getAttributes().getNamedItem("name"));
		}
		else if(tagName.equals("xsl:with-param"))
		{
			/*
			 * Note, xsl:with-param is NOT always nested inside an xsl:call-template
			 * For example this is legal:
				<xsl:apply-templates>
					<xsl:with-param name="myTable" select="$myTable"/>
				</xsl:apply-templates>
			 * This can't be renamed and should be left alone.
			 */
			Node paramNameAttr = current.getAttributes().getNamedItem("name");
			String paramName = paramNameAttr.getNodeValue();
			Element callTemplate = XpathUtils.getAncestorOrSelf((Element)current, "xsl:call-template", "xsl:apply-templates");
			if(callTemplate != null)
			{
				String name = getNodeName(callTemplate);
				Scope callingTemplate = this.getTemplate(name);
				callingTemplate.addReferenceToScoped(paramName, paramNameAttr);
			}
		}
		else if(tagName.equals("xsl:variable"))
		{
			currentScope.addScoped(current);
		}
		else if(tagName.equals("xsl:param") && !currentScope.dontRenameParams)
		{
			currentScope.addScoped(current);//don't rename global params or params in templates with a "match" attribute
		}
		else if(tagName.equals("xsl:element"))
		{
			String name = getNodeName(current);
			if(name != null && name.indexOf('{') < 0)
			{
				NamedNodeMap attributes = current.getAttributes();
				if(attributes.getNamedItem("use-attribute-sets") == null && attributes.getNamedItem("namespace") == null)
				{
					xslElementCollapser.addCandidate(current);
		}
			}
		}
		findReferences(current, currentScope);

		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling())
		{
			if(newScope != null)
			{
				walkLevel(walker, newScope);
			}
			else
			{
				walkLevel(walker, currentScope);
			}
		}
		walker.setCurrentNode(current);
	}
}
