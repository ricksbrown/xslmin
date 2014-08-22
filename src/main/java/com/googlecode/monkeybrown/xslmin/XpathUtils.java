package com.googlecode.monkeybrown.xslmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utilities to help with executing XPATH queries (and other stuff) for xslmin
 * @author Rick Brown
 */
public abstract class XpathUtils
{
	/**
	 * @param element The element whose ancestry we will check
	 * @param tagName The tagName of the element we are searching for in the ancestry
	 * @param limitTagName Stop searching if we hit this limit
	 * @return The first matching element, if found
	 */
	public static Element getAncestorOrSelf(final Element element, final String tagName, final String limitTagName)
	{
		Element result = null;
		Element next = element;
		String currentTagName;
		do
		{
			currentTagName = next.getTagName();
			if(currentTagName.equals(tagName))
			{
				result = next;
				break;
			}
			else
			{
				Node parent = next.getParentNode();
				if(parent != null && parent.getNodeType() == Node.ELEMENT_NODE)
				{
					next = (Element)parent;
				}
				else
				{
					break;
				}
			}
		}
		while(next != null && (limitTagName == null || !tagName.equals(limitTagName)));
		return result;
	}

	/**
	 * Loads an XML document from an InputStream
	 */
	public static Document loadXmlDoc(final InputStream stream)
	{
		Document result = null;
		try
		{
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setExpandEntityReferences(false);
			domFactory.setIgnoringComments(true);//strips comments
//			domFactory.setIgnoringElementContentWhitespace(true);//would be nice if it worked
		    domFactory.setNamespaceAware(true);
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    result = builder.parse(stream);
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Loads an XML document from a File
	 * @param uri The path the file.
	 */
	public static Document loadXmlDoc(final String uri)
	{
		Document result = null;
		try
		{
			File file = new File(uri);
			if(file.exists())
			{
					result = loadXmlDoc(new FileInputStream(file));

			}
			else
			{
				throw new IOException("File does not exist: " + file.getAbsolutePath());
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Executes an XPath query on the provided Document and returns the result.
	 */
	public static NodeList executeQuery(final Document doc, final String query) throws XPathExpressionException
	{
		return executeQuery((Node)doc, query);
	}

	/**
	 * Executes an XPath query in the context of the provided node and returns the result.
	 */
	public static NodeList executeQuery(final Node context, final String query) throws XPathExpressionException
	{
		return (NodeList) executeQuery(context, query, XPathConstants.NODESET);
	}

	/**
	 * Executes an XPath query in the context of the provided node and returns the result as the specified return type.
	 */
	public static Object executeQuery(final Node context, final String query, final QName returnType) throws XPathExpressionException
	{
		XPath xpath = getXpath();
		XPathExpression expr = xpath.compile(query);
		return expr.evaluate(context, returnType);
	}

	/**
	 * Serializes the provided DOM to the filesystem, passing the XML through the xslmin
	 * transform on the way.
	 * @param node The root node of the DOM (or a Document)
	 * @param path The path to target file.
	 */
	public static void xmlToFile(final Node dom, final String path)
	{
		try
		{
			Transformer transformer = getTransformer();
			DOMSource source = new DOMSource(dom);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return An instance of XPath to use in queries.
	 */
	private static XPath getXpath()
	{
		XPath result;
		XPathFactory factory = XPathFactory.newInstance();
		result = factory.newXPath();
		result.setNamespaceContext(new XslNamespaceContext());
		return result;
	}

	/**
	 * @return An instance of Transformer configured with xslmin.xsl ready to transform a DOM
	 */
	private static Transformer getTransformer()
	{
		Transformer transformer;
		try
		{
			Document xslt = loadXmlDoc(XpathUtils.class.getResourceAsStream("/xslmin.xsl"));
			TransformerFactory tFactory = TransformerFactory.newInstance();
			transformer = tFactory.newTransformer(new DOMSource(xslt));
		}
		catch (TransformerConfigurationException e)
		{
			transformer = null;
			System.err.println("Could not load resource xslmin.xsl");
			e.printStackTrace();
		}
		return transformer;
	}
}
