/*****************************************************************************

JEP - Java Math Expression Parser 2.3.1
      January 26 2006
      (c) Copyright 2004, Nathan Funk and Richard Morris
      See LICENSE.txt for license information.

*****************************************************************************/

package org.nfunk.jep.evaluation;

import java.util.Stack;

import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.function.PostfixMathCommandI;

/**
 * 
 * @author nathan
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class CommandEvaluator {
	private int                 nCommands, nParam;
	private CommandElement      command;
	private Stack               stack;
	private PostfixMathCommandI pfmc;
	private int                 i;
	
	public CommandEvaluator() {
		stack = new Stack();
	}
	
	public Object evaluate(CommandElement[] commands, SymbolTable symTab) throws Exception {
		
		int nCommands = commands.length;
		
		stack.removeAllElements();
		
		// for each command
		i = 0;
		while (i<nCommands) {
			command = commands[i];

			switch (command.getType()) {
				case CommandElement.FUNC: {
					// Function
					pfmc = command.getPFMC();
				
					// set the number of current parameters 
					// (it is no faster to first check getNumberOfParameters()==-1)
					pfmc.setCurNumberOfParameters(command.getNumParam());
				
					pfmc.run(stack);
					break;
				}
				case CommandElement.VAR: {
					// Variable
					stack.push(symTab.getValue(command.getVarName()));					
					break;
				}
				default: {
					// Constant
					stack.push(command.getValue());
				}
			}
			/*
			if (command instanceof ASTVarNode) {
				// Variable
				stack.push(symTab.get(((ASTVarNode)command).getName()));
			} else if (command instanceof PostfixMathCommandI) {
				// Function
				pfmc = (PostfixMathCommandI)command;
				
				// set the number of current parameters 
				// (it is no faster to first check getNumberOfParameters()==-1)
				nParam = ((Integer)commands.elementAt(++i)).intValue();
				pfmc.setCurNumberOfParameters(nParam);
				
				pfmc.run(stack);
			} else {
			}*/
			
			i++;
		}
		if (stack.size() != 1) {
			throw new Exception("CommandEvaluator.evaluate(): Stack size is not 1");
		}
		return stack.pop();
	}
}
