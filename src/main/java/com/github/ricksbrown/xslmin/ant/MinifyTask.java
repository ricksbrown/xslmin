package com.github.ricksbrown.xslmin.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.github.ricksbrown.xslmin.XslMin;

/**
 * <p>
 * This class implements a simple Ant task to invoke the xsl minification from Ant.
 * The primary benefit of this is to run minification without forking execution of
 * the JAR in a new JVM.
 * </p>
 * <p>
 * Usage:
 *
 *<code>
	&lt;taskdef name="xslmin"
		classname="com.github.ricksbrown.xslmin.ant.MinifyTask"
		classpath="xslmin.jar"/>

	&lt;xslmin in="${srcdir}/myxsl.xsl" out="${outdir}/myxsl.xsl" preserve="true"/>
  </code></p>
 *
 *
 *
 * @author Rick Brown
 */
public class MinifyTask extends Task
{
	String out;
	String in;
	boolean preserve = false;

	/**
	 * @param preserve If true then unused templates will not be stripped.
	 */
	public void setPreserve(Boolean preserve)
	{
		this.preserve = preserve;
	}

	/**
	 * @param out The path to the output file created by the minifier
	 */
	public void setOut(String out)
	{
		this.out = out;
	}

	/**
	 *
	 * @param in The path to the file we want to minify
	 */
	public void setIn(String in)
	{
		this.in = in;
	}

	@Override
	public void execute() throws BuildException
	{
		if(this.preserve)
		{
			String[] args = {in, out, "-p"};
			XslMin.main(args);
		}
		else
		{
			String[] args = {in, out};
			XslMin.main(args);
		}
	}
}
