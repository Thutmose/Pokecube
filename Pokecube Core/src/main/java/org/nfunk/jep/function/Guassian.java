package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Guassian extends PostfixMathCommand
{
    private java.util.Random rand;

    public Guassian()
    {
        numberOfParameters = 0;
        this.rand = new java.util.Random();
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        checkStack(inStack);// check the stack
        inStack.push(new Double(rand.nextGaussian()));
        return;
    }
}