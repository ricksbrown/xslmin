package com.github.ricksbrown.xslmin;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

import javax.xml.xpath.XPathConstants;

import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Rick Brown
 */
public class XslNamedTemplateRenamerTest extends TestCase
{
	private boolean hasRun = false;
	private static final String ALL_TEMPLATE_XPATH = "//xsl:template";
	private static final String ALL_UNUSED_TEMPLATE_XPATH = ALL_TEMPLATE_XPATH + "[@handle='notused_plsremove']";

	@Override
	public void setUp()
	{
		if(!hasRun)
		{
			hasRun = true;
			XslMinTestUtils.runXslMin();
		}
	}

	/**
	 * Test the number of templates in the minified result is what we expect
	 */
	public void testTemplateCount()
	{
		testTemplateCountHelper(false);
	}

	/**
	 * Test the number of templates in the minified result is what we expect
	 */
	public void testTemplateCountStripped()
	{
		testTemplateCountHelper(true);
	}

	/**
	 * Test the number of templates in the minified result is what we expect
	 */
	private void testTemplateCountHelper(boolean useStripped)
	{
		String count = String.format("count(%s)", ALL_TEMPLATE_XPATH);
		try
		{
			double countAfter, expected, countBefore = (Double) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), count, XPathConstants.NUMBER);
			Node resultXsl, sourceXsl = XslMinTestUtils.getSourceXsl();
			if(useStripped)
			{
				resultXsl = XslMinTestUtils.getResultStrippedXsl();
			}
			else
			{
				resultXsl = XslMinTestUtils.getResultXsl();
			}
			countAfter = (Double) XpathUtils.executeQuery(resultXsl, count, XPathConstants.NUMBER);
			expected = countBefore;
			if(useStripped)
			{
				NodeList unused = getUnusedTemplates(sourceXsl);
				if(unused != null)
				{
					expected -= unused.getLength();
				}
			}
			assertEquals("(" + countBefore + " > 0) && (" + countAfter + " == " + expected + ")", true, (countBefore > 0) && (countAfter == expected));
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	public void testTemplates()
	{
		testTemplatesHelper(false);
	}

	public void testTemplatesStripped()
	{
		testTemplatesHelper(true);
	}

	private void testTemplatesHelper(boolean useStripped)
	{
		try
		{
			Node resultXsl, sourceXsl = XslMinTestUtils.getSourceXsl();
			if(useStripped)
			{
				resultXsl = XslMinTestUtils.getResultStrippedXsl();
			}
			else
			{
				resultXsl = XslMinTestUtils.getResultXsl();
			}
			NodeList beforeTemplates = XpathUtils.executeQuery(sourceXsl, ALL_TEMPLATE_XPATH);
			NodeList afterTemplates = XpathUtils.executeQuery(resultXsl, ALL_TEMPLATE_XPATH);
			assertTrue("Nothing to test!", beforeTemplates.getLength() > 0);
			for(int i=0; i<afterTemplates.getLength(); i++)
			{
				Node beforeTemplate = beforeTemplates.item(i);
				Node afterTemplate = afterTemplates.item(i);
				Node beforeNameAttribute = beforeTemplate.getAttributes().getNamedItem("name");
				if(beforeNameAttribute != null)
				{
					String beforeName = beforeNameAttribute.getNodeValue();
					String aftername = afterTemplate.getAttributes().getNamedItem("name").getNodeValue();
					boolean hasMatch = (beforeTemplate.getAttributes().getNamedItem("match") != null);
					System.out.println("Checking !(\"" + beforeName + "\".equals(\"" + aftername + "\"))" );
					assertFalse("named template '" + beforeName + "' should be renamed", beforeName.equals(aftername));
					NodeList beforeCallsToTemplate = getCallTemplates(beforeName, sourceXsl);
					int expected = beforeCallsToTemplate.getLength();
					if(expected > 0)
					{
						System.out.println("Checking calls to template: " + beforeName);
						NodeList afterCallsToTemplate = getCallTemplates(aftername, resultXsl);
						if(useStripped)
						{
							NodeList unused = getUnusedTemplateCalls(sourceXsl, beforeName);
							if(unused != null)
							{
								expected -= unused.getLength();
								System.out.println("UNUSED: " + unused.getLength());
							}
						}
						assertEquals(expected, afterCallsToTemplate.getLength());
						List<String> beforeParamNames = getParams(beforeTemplate);
						for(int j=0; j<afterCallsToTemplate.getLength(); j++)
						{
							List<String> paramNamesInCall = getWithParams(afterCallsToTemplate.item(j));
							for(int k=0; k<beforeParamNames.size(); k++)
							{
								String nextName = beforeParamNames.get(k);
								boolean paramNameInSource = paramNamesInCall.contains(nextName);
								if(!hasMatch)
								{
									if(paramNameInSource)
									{
										String message = "Calling template " + beforeName + " (" + aftername + ")";
										message += " with param " + nextName + " not renamed ";
										fail(message);
									}
								}
								else if(!paramNameInSource)
								{
									System.out.println("SUSPICIOUS: did not find param '" + "' in call to " + beforeName);
								}
							}
						}
					}
					else
					{
						//System.out.println("No calls to template: " + beforeName);
					}
				}
			}
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/*
	 * These are really just calls that are nested inside an unused template
	 */
	private NodeList getUnusedTemplateCalls(Node xslDoc, String templateName)
	{
		NodeList result = null;
		try
		{
			String query;
			if(templateName != null)
			{
				query = "//xsl:template[@handle='notused_plsremove']//xsl:call-template[@name='" + templateName + "']";
			}
			else
			{
				query = "//xsl:template[@handle='notused_plsremove']//xsl:call-template";
			}
			result = XpathUtils.executeQuery(xslDoc, query);
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
		return result;
	}

	private NodeList getUnusedTemplates(Node xslDoc)
	{
		NodeList result = null;
		try
		{
			result = XpathUtils.executeQuery(xslDoc, ALL_UNUSED_TEMPLATE_XPATH);
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
		return result;
	}

	private List<String> getParams(Node template)
	{
		List<String> result = new ArrayList<String>();
		NodeList kids = template.getChildNodes();
		for(int i=0; i<kids.getLength(); i++)
		{
			Node kid = kids.item(i);
			if(kid.getNodeType() == Node.ELEMENT_NODE)
			{
				String tagName = ((Element) kid).getTagName();
				if("xsl:param".equals(tagName))
				{
					result.add(kid.getAttributes().getNamedItem("name").getNodeValue());
				}
			}
		}
		return result;
	}

	private List<String> getWithParams(Node callTemplate)
	{
		List<String> result = new ArrayList<String>();
		NodeList kids = callTemplate.getChildNodes();
		for(int i=0; i<kids.getLength(); i++)
		{
			Node kid = kids.item(i);
			if(kid.getNodeType() == Node.ELEMENT_NODE)
			{
				String tagName = ((Element) kid).getTagName();
				if("xsl:with-param".equals(tagName))
				{
					result.add(kid.getAttributes().getNamedItem("name").getNodeValue());
				}
			}
		}
		return result;
	}

	private NodeList getCallTemplates(String templateName, Node xslDoc)
	{
		String query = "//xsl:call-template[@name='" + templateName + "']";
		NodeList result = null;
		try
		{

			result = XpathUtils.executeQuery(xslDoc, query);
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
		return result;
	}


}
