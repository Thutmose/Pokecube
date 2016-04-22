/*****************************************************************************

JEP - Java Math Expression Parser 2.3.1
      January 26 2006
      (c) Copyright 2004, Nathan Funk and Richard Morris
      See LICENSE.txt for license information.

*****************************************************************************/

package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Logical extends PostfixMathCommand
{
	public static final int AND = 0;
	public static final int OR = 1;
	int id;
	public Logical(int id_in)
	{
		id = id_in;
		numberOfParameters = 2;
	}
	
	@Override
	public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		
		Object param2 = inStack.pop();
		Object param1 = inStack.pop();
		
		
		if ((param1 instanceof Number) && (param2 instanceof Number))
		{
			double x = ((Number)param1).doubleValue();
			double y = ((Number)param2).doubleValue();
			int r;
			
			switch (id)
			{
				case 0:
					// AND
					r = ((x!=0d) && (y!=0d)) ? 1 : 0;
					break;
				case 1:
					// OR
					r = ((x!=0d) || (y!=0d)) ? 1 : 0;
					break;
				default:
					r = 0;
			}
			
			inStack.push(new Double(r)); // push the result on the inStack
		}
		else
		{
			throw new ParseException("Invalid parameter type");
		}
		return;
	}
}
