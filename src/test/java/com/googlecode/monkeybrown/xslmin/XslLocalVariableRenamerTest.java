package com.googlecode.monkeybrown.xslmin;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathConstants;

import javax.xml.xpath.XPathExpressionException;
import junit.framework.TestCase;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XslLocalVariableRenamerTest extends TestCase
{
	private static final String LOCAL_VAR_XPATH = "//xsl:template/descendant::xsl:variable";
	private static final String ALL_UNUSED_VAR_XPATH = LOCAL_VAR_XPATH + "[@handle='notused_plsremove']";

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

	private static NodeList getUnusedVariables(Node xslDoc)
	{
		NodeList result = null;
		try
		{
			result = XpathUtils.executeQuery(xslDoc, ALL_UNUSED_VAR_XPATH);
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
		return result;
	}

	private static NodeList getLocalVariables(Node xslDoc)
	{
		NodeList result = null;
		try
		{
			result = XpathUtils.executeQuery(xslDoc, LOCAL_VAR_XPATH);
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
		return result;
	}

	/**
	 * Test the number of variables in the minified result is what we expect
	 */
	private void testVariableCountHelper(boolean useStripped)
	{
		String count = String.format("count(%s)", LOCAL_VAR_XPATH);
		try
		{
			Node resultXsl, sourceXsl = XslMinTestUtils.getSourceXsl();
			double countAfter, expected, countBefore = (Double) XpathUtils.executeQuery(sourceXsl, count, XPathConstants.NUMBER);
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
				NodeList unused = getUnusedVariables(sourceXsl);
				//System.out.println("UNUSED: " + unused.getLength());
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

	/**
	 * Test the number of variables in the minified result is what we expect
	 */
	public void testVariableCount()
	{
		testVariableCountHelper(false);
	}

	/**
	 * Test the number of variables in the minified result is what we expect
	 */
	public void testVariableCountStripped()
	{
		testVariableCountHelper(true);
	}

	/**
	 * Test that the local variables have the same value they did before minification
	 */
	public void testValuesUntouched()
	{
		NodeList vars = getLocalVariables(XslMinTestUtils.getResultXsl());
		assertEquals("localB", vars.item(0).getFirstChild().getTextContent());
	}

	/**
	 * Test that the local variables were actually renamed
	 */
	public void testDefinitionsWereRenamed()
	{
		NodeList vars = getLocalVariables(XslMinTestUtils.getResultXsl());
		for(int i=0; i<vars.getLength(); i++)
		{
			assertEquals(true, vars.item(i).getAttributes().getNamedItem("name").getNodeValue().length() == 1);
		}
	}

	/**
	 * Test that the references to local variables were correctly renamed
	 */
	public void testReferencesWereRenamed()
	{
		String xpath = "//xsl:template[@handle='tmpl1']/xsl:variable[position()=%d]";
		try
		{
			Node ref1 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), String.format(xpath, 1), XPathConstants.NODE);
			Node ref2 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), String.format(xpath, 2), XPathConstants.NODE);
			Node ref3 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), String.format(xpath, 3), XPathConstants.NODE);
			String ref1Name = ref1.getAttributes().getNamedItem("name").getNodeValue();
			String ref2Name = ref2.getAttributes().getNamedItem("name").getNodeValue();
			String ref3Value = ref3.getAttributes().getNamedItem("select").getNodeValue();

			assertEquals(String.format("concat($%s,$%s)", ref1Name, ref2Name), ref3Value);
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Test that the references to local variables were correctly renamed
	 */
	public void testPartialMatchesNotRenamed()
	{
		try
		{
			String varNameXp = "//xsl:template/descendant::xsl:variable[@handle='partialMatchA']/@name";
			String valOfSelectXp = "//xsl:template/descendant::xsl:value-of[@handle='partialMatchA']/@select";
			//First run the test on the UN-minified XSL to check this test is testing something
			Node var = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), varNameXp, XPathConstants.NODE);
			Node valOf = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), valOfSelectXp, XPathConstants.NODE);
			assertTrue(valOf.getNodeValue().indexOf(var.getNodeValue()) > -1);//check that this unit test is working
			var = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), varNameXp, XPathConstants.NODE);
			valOf = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), valOfSelectXp, XPathConstants.NODE);
			assertEquals(-1, valOf.getNodeValue().indexOf(var.getNodeValue()));//check that the minifier didn't do evil
		}
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * Test that the references to local variables were correctly renamed (Google Code Issue #1)
	 */
	public void testPartialMatchesNotRenamedNonWordChar()
	{
		try
		{
			String vars = "//xsl:template/descendant::xsl:value-of[@handle='varMatchA']/@select";
			String fakeVars = "//xsl:template/descendant::xsl:value-of[@handle='varMatchB']/@select";

			Node varsB4 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), vars, XPathConstants.NODE);
			Node fakeVarsB4 = (Node) XpathUtils.executeQuery(XslMinTestUtils.getSourceXsl(), fakeVars, XPathConstants.NODE);

			Node varsAfter = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), vars, XPathConstants.NODE);
			Node fakeVarsAfter = (Node) XpathUtils.executeQuery(XslMinTestUtils.getResultXsl(), fakeVars, XPathConstants.NODE);

			assertEquals(fakeVarsB4.getNodeValue(), fakeVarsAfter.getNodeValue());
			assertTrue(varsB4.getNodeValue().indexOf('-') > 0);
			assertFalse(varsAfter.getNodeValue().indexOf('-') > 0);
		}
		catch(XPathExpressionException ex)
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
		catch(XPathExpressionException ex)
		{
			fail(ex.getMessage());
		}
	}

	/**
	 * This test checks that no local variables have the same name as global variables.
	 * Actually this is not really a requirement - it is possible for a local variable to
	 * have the same name as a global variable.
	 *
	 * The problem is that it is HARD to ensure that a template does not reference a global
	 * AND a local variable which have been renamed to the same name. The simplest way to keep
	 * this from happening is to use the same NameGenerator instance for all variables so none
	 * ever clash. This results in a slightly larger result file but should be very safe.
	 *
	 */
	public void testNoClashWithGlobalVariables()
	{
		NodeList globals = XslGlobalVariableRenamerTest.getGlobalVariables();
		NodeList locals = getLocalVariables(XslMinTestUtils.getResultXsl());
		List<String> clashes = new ArrayList<String>();
		for(int i=0; i<locals.getLength(); i++)
		{
			String localName = locals.item(i).getAttributes().getNamedItem("name").getNodeValue();
			for(int j=0; j<globals.getLength(); j++)
			{
				String globalName = globals.item(j).getAttributes().getNamedItem("name").getNodeValue();
				if(globalName.equals(localName))
				{
					clashes.add(localName);
				}
			}
		}
		assertTrue("The following variables clashed: " + clashes.toString(), clashes.size() == 0);
	}
}
