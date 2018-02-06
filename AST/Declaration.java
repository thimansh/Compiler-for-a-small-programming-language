package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;

public abstract class Declaration extends ASTNode {
	public Type declareType;
	public Declaration(Token firstToken) {
		super(firstToken);
	}
	@Override
	abstract public Object visit(ASTVisitor v,Object arg) throws Exception;



}
