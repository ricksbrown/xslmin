package com.github.ricksbrown.xslmin;

import org.w3c.dom.Node;

public class XslMinTestUtils
{
	public static final String TEST_XSL = "target/test-classes/test.xsl";
	public static final String TEST_OUT = "test.min.xsl";
	public static final String TEST_OUT_STRIPPED = "test.min.stripped.xsl";

	public static void runXslMin()
	{
		String [] args = {TEST_XSL, TEST_OUT, "-p"};
		String [] argsStripped = {TEST_XSL, TEST_OUT_STRIPPED};
		XslMin.main(args);
		XslMin.main(argsStripped);
	}

	public static Node getResultXsl()
	{
		return XpathUtils.loadXmlDoc(TEST_OUT).getDocumentElement();
	}

	public static Node getResultStrippedXsl()
	{
		return XpathUtils.loadXmlDoc(TEST_OUT_STRIPPED).getDocumentElement();
	}

	public static Node getSourceXsl()
	{
		return XpathUtils.loadXmlDoc(TEST_XSL).getDocumentElement();
	}
}
