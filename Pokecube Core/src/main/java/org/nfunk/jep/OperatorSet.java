/* @author rich
 * Created on 03-Aug-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.nfunk.jep;

import org.nfunk.jep.function.Add;
import org.nfunk.jep.function.Assign;
import org.nfunk.jep.function.Comparative;
import org.nfunk.jep.function.Cross;
import org.nfunk.jep.function.Divide;
import org.nfunk.jep.function.Dot;
import org.nfunk.jep.function.List;
import org.nfunk.jep.function.Logical;
import org.nfunk.jep.function.Modulus;
import org.nfunk.jep.function.Multiply;
import org.nfunk.jep.function.Not;
import org.nfunk.jep.function.Power;
import org.nfunk.jep.function.Subtract;
import org.nfunk.jep.function.UMinus;

/**
 * The standard set of operators used in JEP.
 * <p>
 * 
 * <pre>
 * OperatorSet opSet = new OperatorSet();
 * Operator myOp = opSet.getAdd();
 * </pre> 
 * <p>   
 * @author Rich Morris
 * Created on 19-Oct-2003
 */
public class OperatorSet {
		
	/** everyone can read but not write these operators **/
	protected Operator OP_GT     =  new Operator(">",new Comparative(Comparative.GT));
	protected Operator OP_LT     =  new Operator("<",new Comparative(Comparative.LT));
	protected Operator OP_EQ     =  new Operator("==",new Comparative(Comparative.EQ));
	protected Operator OP_LE     =  new Operator("<=",new Comparative(Comparative.LE));
	protected Operator OP_GE     =  new Operator(">=",new Comparative(Comparative.GE));
	protected Operator OP_NE     =  new Operator("!=",new Comparative(Comparative.NE));

	protected Operator OP_AND    =  new Operator("&&",new Logical(0));
	protected Operator OP_OR     =  new Operator("||",new Logical(1));
	protected Operator OP_NOT    = new Operator("!",new Not());

	protected Operator OP_ADD   =  new Operator("+",new Add());
	protected Operator OP_SUBTRACT  =  new Operator("-",new Subtract());
	protected Operator OP_UMINUS =  new Operator("UMinus","-",new UMinus());

	protected Operator OP_MULTIPLY    =  new Operator("*",new Multiply());
	protected Operator OP_DIVIDE = new Operator("/",new Divide());
	protected Operator OP_MOD    = new Operator("%",new Modulus());
	/** unary division i.e. 1/x or x^(-1) **/ 
	protected Operator OP_UDIVIDE =  new Operator("UDivide","^-1",null);

	protected Operator OP_POWER  = new Operator("^",new Power());

	protected Operator OP_ASSIGN = new Operator("=",new Assign()); 
	protected Operator OP_DOT = new Operator(".",new Dot()); 
	protected Operator OP_CROSS = new Operator("^^",new Cross()); 
	protected Operator OP_LIST = new Operator("LIST",new List()); 
	
	public OperatorSet()
	{
	}
	
	public Operator getAdd() {return OP_ADD;	}

	public Operator getAnd() {	return OP_AND;	}

	public Operator getAssign() {return OP_ASSIGN;	}
	public Operator getCross() {return OP_CROSS;	}
	public Operator getDivide() {return OP_DIVIDE;	}
	public Operator getDot() {return OP_DOT;	}
	public Operator getEQ() {return OP_EQ;	}
	public Operator getGE() {return OP_GE;	}
	public Operator getGT() {return OP_GT;	}

	public Operator getLE() {return OP_LE;	}
	public Operator getList() {return OP_LIST;	}
	public Operator getLT() {return OP_LT;	}
	public Operator getMod() {return OP_MOD;	}
	public Operator getMultiply() {return OP_MULTIPLY;	}
	public Operator getNE() {return OP_NE;	}

	public Operator getNot() {return OP_NOT;	}
	/** Gets the list of operators. Note subclasses should override this method. */	
	public Operator[] getOperators() {
		Operator ops[] = new Operator[]{
		OP_GT,OP_LT,OP_GE,OP_LE,OP_EQ,OP_NE,OP_AND,OP_OR,OP_NOT,
		OP_ADD,OP_SUBTRACT,OP_UMINUS,OP_MULTIPLY,
		OP_DIVIDE,OP_MOD,OP_POWER,
		OP_ASSIGN,OP_DOT,OP_CROSS,OP_LIST};
		return ops;
	}
	public Operator getOr() {return OP_OR;	}
	public Operator getPower() {return OP_POWER;	}
	public Operator getSubtract() {return OP_SUBTRACT;	}
	public Operator getUMinus() {return OP_UMINUS;	}
	public void printOperators()
	{
		Operator ops[] = getOperators();
		for(int i=0;i<ops.length;++i)
			System.out.println(ops[i].toString());
	}

}
