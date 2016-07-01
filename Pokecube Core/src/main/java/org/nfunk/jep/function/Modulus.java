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
public class Modulus extends PostfixMathCommand
{
	public Modulus()
	{
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
			double divisor = ((Number)param2).doubleValue();
			double dividend = ((Number)param1).doubleValue();
		
			double result = dividend % divisor;
	
			inStack.push(new Double(result));
		}
		else
		{
			throw new ParseException("Invalid parameter type");
		}
		return;
	}
}
