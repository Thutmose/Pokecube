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

/**
 * Implements the arcTanH function.
 * 
 * @author Nathan Funk
 * @since 2.3.0 beta 2 - Now returns Double result rather than Complex for -1<x<1 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArcTanH extends PostfixMathCommand
{
	public ArcTanH()
	{
		numberOfParameters = 1;
	}

	public Object atanh(Object param)
		throws ParseException
	{
		if (param instanceof Complex)
		{
			return ((Complex)param).atanh();
		}
		else if (param instanceof Number)
		{
			double val = ((Number)param).doubleValue();
			if(val > -1.0 && val < 1) {
				double res = Math.log((1+val)/(1-val))/2;
				return new Double(res);
			}
			else
			{
				Complex temp = new Complex(val,0.0);
				return temp.atanh();
			}
		}

		throw new ParseException("Invalid parameter type");
	}

	@Override
	public void run(Stack inStack)
		throws ParseException
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
		inStack.push(atanh(param));//push the result on the inStack
		return;
	}

}
