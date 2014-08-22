package com.googlecode.monkeybrown.xslmin;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import javax.xml.xpath.XPathExpressionException;
import junit.framework.TestCase;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test that elements were collapsed
 * I.E.
 * <xsl:element name="foo"/> gets rewritten to <foo/>
 *
 * or
 *
 * <xsl:element name="foo">
 * 	<xsl:attribute name="bar" select="'foobar'"/>
 * </xsl:element>
 *
 * gets rewritten to: <foo bar="foobar"/>
 *
 */
public class XslElementCollapserTest extends TestCase
{
	private boolean hasRun = false;

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
	 * Tests a known element was collapsed
	 */
	public void testSpecificElementCollapsed()
	{
		try
		{
			Node beforeName = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), "//xsl:template[@handle='collapse1']/xsl:element/@name", XPathConstants.NODE);
			Node after = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:template[@handle='collapse1']/" + beforeName.getNodeValue(), XPathConstants.NODE);
			assertNotNull(after);
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Tests attributes on a known element were collapsed into the element
	 */
	public void testSpecificElementAttributesCollapsed()
	{
		try
		{
			Node before = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), "//xsl:template[@handle='collapse1']/xsl:element", XPathConstants.NODE);
			Node after = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:template[@handle='collapse1']/" + before.getAttributes().getNamedItem("name").getNodeValue(), XPathConstants.NODE);
			assertNotNull(after);

			Map<String, String> expectedAttributes = new HashMap<String, String>();
			expectedAttributes.put("alpha", "text");
			expectedAttributes.put("func", "{concat($aParam, '-suffix')}");
			expectedAttributes.put("mix", "txtNode-text-{$aParam}");
			expectedAttributes.put("numeric", "0101");
			expectedAttributes.put("textNode", "text");
			expectedAttributes.put("var", "{$aParam}");
			expectedAttributes.put("xpath", "{@shape}");

			NamedNodeMap attributes = after.getAttributes();

			for (String key : expectedAttributes.keySet()) {
				Node attribute = attributes.getNamedItem(key);
				assertNotNull(attribute);
				assertEquals(expectedAttributes.get(key), attribute.getNodeValue());
			}
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Tests uncollapsible attributes on a known element were NOT collapsed into the element
	 */
	public void testSpecificElementAttributesNotCollapsed()
	{
		try
		{
			Node before = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), "//xsl:template[@handle='collapse1']/xsl:element", XPathConstants.NODE);
			Node after = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:template[@handle='collapse1']/" + before.getAttributes().getNamedItem("name").getNodeValue(), XPathConstants.NODE);
			assertNotNull(after);
			Node testAttr = after.getAttributes().getNamedItem("noCollapse1");
			assertEquals(null, testAttr);
			testAttr = after.getAttributes().getNamedItem("button");
			assertEquals(null, testAttr);
			boolean foundAttrWithNestedIf = false;
			boolean foundAttrNestedInIf = false;
			for(int i=0; i<after.getChildNodes().getLength(); i++)
			{
				Node next = after.getChildNodes().item(i);
				if(next.getNodeName().equals("xsl:attribute") && next.getAttributes().getNamedItem("name") != null && next.getAttributes().getNamedItem("name").getNodeValue().equals("noCollapse1"))
				{
					foundAttrWithNestedIf = true;
				}
				else if(next.getNodeName().equals("xsl:if"))
				{
					for(int j=0; j<next.getChildNodes().getLength(); j++)
					{
						if(next.getChildNodes().item(j).getNodeName().equals("xsl:attribute") && next.getChildNodes().item(j).getAttributes().getNamedItem("name") != null
										&& next.getChildNodes().item(j).getAttributes().getNamedItem("name").getNodeValue().equals("button"))
						{
							foundAttrNestedInIf = true;
						}

					}
				}
			}
			assertEquals("foundAttrWithNestedIf", true, foundAttrWithNestedIf);
			assertEquals("foundAttrNestedInIf", true, foundAttrNestedInIf);
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Loop through every xsl:element in the source xsl and make sure it has been collapsed in the result xsl
	 */
	public void testElementsCollapsed()
	{
		try
		{
			NodeList before = XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), "//xsl:element/@name[not(contains(.,'{'))]");
			for(int i=0; i<before.getLength(); i++)
			{
				String nextName = before.item(i).getNodeValue();
				Node after = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//" + nextName, XPathConstants.NODE);
				assertNotNull(after);
			}
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Elements which have a variable reference for their element name can not be collapsed,
	 * test that this has not beed attempted by errant minfication routines
	 */
	public void testExcludedElementsNotCollapsed()
	{
		try
		{
			double before = (Double) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), "count(//xsl:element/@name[contains(.,'{')])", XPathConstants.NUMBER);
			double after = (Double) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "count(//xsl:element/@name[contains(.,'{')])", XPathConstants.NUMBER);
			assertEquals("Nothing to test", true, before > 0);
			assertEquals(true, before == after);
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}
}
