package com.github.ricksbrown.xslmin;

import javax.xml.xpath.XPathConstants;

import javax.xml.xpath.XPathExpressionException;
import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XslLocalParamRenamerTest extends TestCase
{
	private static final String LOCAL_PARAM_XPATH = "//xsl:template/descendant::xsl:param";

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
	 * Test that the local params still exist
	 */
	public void testAllStillExist()
	{
		try
		{
			String count = String.format("count(%s)", LOCAL_PARAM_XPATH);
			double countBefore = (Double) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), count, XPathConstants.NUMBER);
			double countAfter = (Double) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), count, XPathConstants.NUMBER);
			assertEquals(true, (countBefore > 0) && (countAfter == countBefore));
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Test that the local params have the same value they did before minification
	 */
	public void testValuesUntouched()
	{
		try
		{
			Node firstLocalParamSelect = (Node)XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), LOCAL_PARAM_XPATH + "/@select", XPathConstants.NODE);
			assertEquals("'localA'", firstLocalParamSelect.getNodeValue());
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Test that the local params were actually renamed
	 */
	public void testDefinitionsWereRenamed()
	{
		try
		{
			NodeList params = XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:template[not(@match)]/descendant::xsl:param/@name");
			for(int i=0; i<params.getLength(); i++)
			{
				assertEquals(1, params.item(i).getNodeValue().length());
			}
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Test that local params in templates with a match attribute were NOT renamed
	 */
	public void testExcludedDefinitionsNotRenamed()
	{
		try
		{
			String xpath = "//xsl:template[@match]/descendant::xsl:param/@name";
			NodeList beforeMin = XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), xpath);
			NodeList afterMin = XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), xpath);
			for(int i=0; i< Math.min(beforeMin.getLength(), afterMin.getLength()); i++)
			{
				assertTrue(beforeMin.item(i).getNodeValue().equals(afterMin.item(i).getNodeValue()));
			}
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}



	/**
	 * Test that local variables and local params are not renamed to the same name in the same scope
	 * PRECONDITION: both the local variable renaming AND the local parameter renaming must have been executed for this test to prove anything
	 */
	public void testNoClash()
	{
		try
		{
			Node var1 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:template/descendant::xsl:variable/@name", XPathConstants.NODE);//first var
			Node param1 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:template/descendant::xsl:param/@name", XPathConstants.NODE);//first param
			assertFalse(var1.getNodeValue().equals(param1.getNodeValue()));
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	public void testExcludedWithParamsNotRenamed()
	{
		NodeList beforeExcluded = getExplicitNoRenameWithParams(XslMinTestUtils.getSourceXsl());
		NodeList afterExcluded = getExplicitNoRenameWithParams(XslMinTestUtils.getResultXsl());
		assertTrue("Nothing to test", beforeExcluded.getLength() > 0);
		for(int i=0; i<beforeExcluded.getLength(); i++)
		{
			Element before = (Element) beforeExcluded.item(i);
			String beforeName = before.getAttribute("name");
			Element after = (Element) afterExcluded.item(i);
			String afterName = after.getAttribute("name");
			assertEquals(beforeName, afterName);
		}
	}

	private NodeList getExplicitNoRenameWithParams(Node xslDoc)
	{
		NodeList result = null;
		try
		{
			result = XpathUtils.executeQuery(xslDoc, "//xsl:with-param[@handle='norename']");
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
		return result;
	}
}
