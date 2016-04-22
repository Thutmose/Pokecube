/*****************************************************************************

JEP - Java Math Expression Parser 2.3.1
      January 26 2006
      (c) Copyright 2004, Nathan Funk and Richard Morris
      See LICENSE.txt for license information.

*****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Real extends PostfixMathCommand
{
	public Real()
	{
		numberOfParameters = 1;
	}
	
	public Number re(Object param) throws ParseException {
		if (param instanceof Complex)
			return new Double(((Complex)param).re());
		else if (param instanceof Number)
			return ((Number)param);

		throw new ParseException("Invalid parameter type");
	}
	
	@Override
	public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
		inStack.push(re(param));//push the result on the inStack
		return;
	}

}
