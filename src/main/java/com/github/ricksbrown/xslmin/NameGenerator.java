package com.github.ricksbrown.xslmin;

/**
 * Generates short names/identifiers which are unique in the given Scope.
 *
 * @author Rick Brown
 */
public class NameGenerator
{
	private int idx;
	
	public NameGenerator()
	{
		this.idx = 0;
	}

	/**
	 * Get the next name which does not clash with any other names in the Scope.
	 */
	public String getNextName(Scope scope)
	{
		String result;
		do
		{
			result = getNextName();
		}
		while(scope.contains(result));
		return result;
	}

	/**
	 * When called this instance of NameGenerator will begin generating names
	 * at the start of its sequence.
	 */
	public void reset()
	{
		this.idx = 0;
	}

	private String getNextName()
	{
		return toBase26(this.idx++);
	}

	private static String toBase26(int number)
	{
		number = Math.abs(number);
		String converted = "";
		// Repeatedly divide the number by 26 and convert the
		// remainder into the appropriate letter.
		do
		{
			int remainder = number % 26;
			converted = (char)(remainder + 'A') + converted;
			number = (number - remainder) / 26;
		}
		while (number > 0);

		return converted;
	}
}
