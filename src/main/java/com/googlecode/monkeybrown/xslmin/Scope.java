package com.googlecode.monkeybrown.xslmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.w3c.dom.Node;

/**
 * Represents an XSL element that defines scope (i.e. either a template or the
 * stylesheet element itself).
 *
 * Note, while variables are scoped by their parent node in reality this is a
 * useless distinction for our purposes and is ignored. So there.
 *
 * @author Rick Brown
 */
public class Scope extends NamedNode
{
	private Map<String, NamedNode> scoped = new HashMap<String, NamedNode>();
	NameGenerator nameGenerator;
	boolean dontRenameParams = false; //if true xsl:param elements will not be renamed in this scope.

	/**
	 * This is a constructor. If you need javadoc to tell you what it does you are in trouble.
	 *
	 * @param node The underlying node which this class encapsulates.
	 */
	public Scope(final Node node)
	{
		this(node, new NameGenerator());
	}

	/**
	 * Creates a new instance of Scope which will use the provided NameGenerator
	 * to rename nodes with instead of creating a fresh one.
	 *
	 * @param node The underlying node which this class encapsulates.
	 * @param nameGenerator The NameGenerator this Scope should use.
	 */
	public Scope(final Node node,final NameGenerator nameGenerator)
	{
		super(node);
		this.nameGenerator = nameGenerator;
	}

	/**
	 * Removes the instance of NamedNode from the collection AND removes the underlying Node from the DOM!
	 * @param name The name the NamedNode is registered against.
	 * @return The instance of NamedNode removed from the collection (if the operation was successful).
	 */
	static NamedNode removeNode(final String name, final Map collection)
	{
		NamedNode result = (NamedNode)collection.remove(name);
		if(result != null)
		{
			Node template = result.node;
			Node parent = template.getParentNode();
			if(parent != null)
			{
				parent.removeChild(template);
			}
		}
		return result;
	}


	public void renameAllScoped()
	{
		Iterator<String> it = scoped.keySet().iterator();
		while(it.hasNext())
		{
			String nextName = it.next();
			NamedNode namedNode = scoped.get(nextName);
			if(!namedNode.rename(nameGenerator.getNextName(this)))
			{
				//System.out.println("Could not rename " + nextName + "  it must be a global param OR a variable set in a stylesheet that includes/imports this one");
			}
		}
	}

	/**
	 * Removes any elements that are not referenced anywhere in their scope.
	 * @param preserve If true the names of unused elements will be printed and not removed
	 * @return the number of items removed
	 */
	public int processUnused(final boolean preserve)
	{
		Iterator<String> it = scoped.keySet().iterator();
		List<String> toRemove = new ArrayList<String>();
		while(it.hasNext())
		{
			String nextName = it.next();
			NamedNode namedNode = scoped.get(nextName);
			if(!namedNode.hasReferences())
			{
				toRemove.add(nextName);
			}
		}
		for(String nextName : toRemove)
		{
			if(preserve)
			{
				System.out.println("Unused node: " + nextName);
			}
			else
			{
				Scope.removeNode(nextName, scoped);
				System.out.println("Removed node: " + nextName);
			}
		}
		return toRemove.size();
	}

	@Override
	public boolean rename(final String newName)
	{
		return rename(newName, "%s", "");
	}

	/**
	 *
	 * @param nodeName The name of a variable or parameter
	 * @return true if this scope contains either a variable or a parameter with this name
	 */
	public boolean contains(String nodeName)
	{
		Set names = scoped.keySet();
		return names.contains(nodeName);
	}

	public void addScoped(final Node node, final NamedNode namedNode, final String nodeName)
	{
		NamedNode newNamedNode = namedNode;
		if(scoped.containsKey(nodeName))
		{
			//we already have a NamedNode registered to this name! Can only be valid if it has no node.
			NamedNode oldNamedNode = scoped.get(nodeName);
			oldNamedNode.setNode(node);//we rely on the fact that this will throw an exception if the node is already present
			newNamedNode = oldNamedNode;
		}
		scoped.put(nodeName, newNamedNode);
	}

	public void addScoped(final Node node, final NamedNode namedNode)
	{
		String nodeName = getNodeName(node);
		if(nodeName == null)
		{
			nodeName = UUID.randomUUID().toString();
		}
		this.addScoped(node, namedNode, nodeName);
	}

	public void addScoped(final Node node)
	{
		this.addScoped(node, new NamedNode(node));
	}

	public void addReferenceToScoped(String nodeName, Node reference)
	{
		NamedNode namedNode = scoped.get(nodeName);
		if(namedNode == null)
		{
			//if we get here then we have encountered a reference to the node before we have found the node itself
			namedNode = new NamedNode();
			this.addScoped(null, namedNode, nodeName);
		}
		namedNode.addReference(reference);
	}
}
