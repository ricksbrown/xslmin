package com.github.ricksbrown.xslmin;

import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * TODO normalise doc
 *
 * The bootstrap class that knows how to run the minification process from start
 * to finish.
 *
 * @author Rick Brown
 */
public class XslMin
{
	/**
	 * @param args first arg: input xsl path, second arg: output xsl path
	 */
	public static void main(String[] args)
	{
		try
		{
			if(args.length >= 2)
			{
				String inputXslPath = args[0];
				String outputXslPath = args[1];
				boolean preserve = false;
				if(args.length == 3)
				{
					String arg2 = args[2];
					if("-p".equals(arg2) || "-preserve".equals(arg2))
					{
						preserve = true;
					}
				}
				System.out.println(String.format("Begining minification, input %s output: %s", inputXslPath, outputXslPath));
				if(preserve)
				{
					System.out.println("Preserve is ON - unused elements will not be removed.");
				}
				else
				{
					System.out.println("Preserve is OFF - unused elements will be removed.");
				}
				long start = System.currentTimeMillis();
				XslMin.minify(inputXslPath, outputXslPath, preserve);
				long end = System.currentTimeMillis();
				System.out.println("Finished minification (" + (end-start) + "ms)");
			}
			else
			{
				System.out.println("Usage: java -jar xslmin.jar inputXslPath outputXslPath [-p[reserve]]");
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param inputXslPath The full path to the XSL file we want to minify
	 * @param outputXslPath The full path to the file we want to create with the result of the minification
	 * @param preserve If true unused elements will not be stripped
	 * @throws XPathExpressionException if you are naughty
	 */
	private static void minify(final String inputXslPath, final String outputXslPath, final boolean preserve) throws XPathExpressionException
	{

		Document doc = XpathUtils.loadXmlDoc(inputXslPath);
		Node newDocRoot = doc.getDocumentElement();

		long start = System.currentTimeMillis();
		Stylesheet stylesheet = new Stylesheet(newDocRoot);
		long end = System.currentTimeMillis();
		System.out.println("Building view took " + (end-start) + "ms");

		start = System.currentTimeMillis();
		stylesheet.processAllUnused(preserve);
		end = System.currentTimeMillis();
		System.out.println("Check usage took " + (end-start) + "ms");

		start = System.currentTimeMillis();
		stylesheet.renameAllScoped();
		end = System.currentTimeMillis();
		System.out.println("Renaming took " + (end-start) + "ms");

		start = System.currentTimeMillis();
		stylesheet.collapseElements();
		end = System.currentTimeMillis();
		System.out.println("Collapsing took " + (end-start) + "ms");

		try
		{
			XpathUtils.xmlToFile(newDocRoot, outputXslPath);
		}
		catch(Throwable ex)
		{
			System.err.println("Could not create file " + outputXslPath);
			System.err.println(ex.getMessage());
		}
	}
}
