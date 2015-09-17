package com.github.ricksbrown.xslmin;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import junit.framework.TestCase;
import org.w3c.dom.Node;

public class XslWhitespaceTest extends TestCase
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
	 * Test that whitespace in targeted attributes was correctly normalized
	 */
	public void testXslPreserveSpace()
	{
		try
		{
			Node xslPreserveSpace = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:preserve-space/@elements", XPathConstants.NODE);
			String elements = xslPreserveSpace.getNodeValue();
			assertFalse(elements.contains("  "));
			assertFalse(elements.contains("\n"));
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Test that whitespace in targeted attributes was correctly normalized
	 */
	public void testXslStripSpace()
	{
		try
		{
			Node xslStripSpace = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), "//xsl:strip-space/@elements", XPathConstants.NODE);
			String elements = xslStripSpace.getNodeValue();
			assertFalse(elements.contains("  "));
			assertFalse(elements.contains("\n"));
		}
		catch (XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}
}
