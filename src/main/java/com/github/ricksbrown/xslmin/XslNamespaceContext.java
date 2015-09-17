package com.github.ricksbrown.xslmin;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class XslNamespaceContext implements NamespaceContext
{

	@Override
	public String getNamespaceURI(String nsPrefix)
	{
		String result;
		if ("xsl".equals(nsPrefix))
		{
			result = "http://www.w3.org/1999/XSL/Transform";
		}
		else
		{
			result = "";
		}
		return result;
	}

	@Override
	public String getPrefix(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator getPrefixes(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
