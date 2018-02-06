package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.*;

public abstract class Sink extends ASTNode {
	public Type sinkType;
	public Sink(Token firstToken) {
		super(firstToken);
	}
	@Override
	abstract public Object visit(ASTVisitor v,Object arg) throws Exception;
	

}
