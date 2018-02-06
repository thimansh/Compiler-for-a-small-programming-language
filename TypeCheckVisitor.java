package cop5556fa17;

import java.util.*;
import java.net.*;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.*;


public class TypeCheckVisitor implements ASTVisitor {

	Map<String, Declaration> SymbolTable = new HashMap<String, Declaration>();// nc

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line " + t.line + " pos " + t.pos_in_line + ": " + message);
			this.t = t;
		}

	}
	// make function lookuptype
	// make function lookupdec

	public Type lookupType(String name) {
		if (SymbolTable.containsKey(name)) {
			return SymbolTable.get(name).declareType;
		} else
			return null;
	}

	public Declaration lookupDec(String name) {
		if (SymbolTable.containsKey(name)) {
			return SymbolTable.get(name);
		} else
			return null;
	}

	public void insert(String name, Declaration obj) {
		SymbolTable.put(name, obj);
	}

	/**
	 * The program name is only used for naming the class. It does not rule out
	 * variables with the same name. It is returned for convenience.
	 * 
	 * @throws Exception
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node : program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		Expression e=declaration_Variable.getE();
		if(lookupDec(declaration_Variable.getName())!=null){
			throw new SemanticException(declaration_Variable.firstToken,"Error in visiting Declaration_Variable");
		}else{
			declaration_Variable.declareType=TypeUtils.getType(declaration_Variable.getType());
			
			if(e!=null){
				e.visit(this, arg);
				if(declaration_Variable.declareType!=e.expType){
					throw new SemanticException(declaration_Variable.firstToken,"Error in visiting Declaration_Variable 2...found an expression");
				}
			}
			insert(declaration_Variable.getName(),declaration_Variable);
			
		}
		return declaration_Variable.declareType;
		
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
	Expression e0=expression_Binary.getE0();
	e0.visit(this, arg);
	Expression e1=expression_Binary.getE1();
	e1.visit(this, arg);
	//System.out.println((e0.expType==e1.expType) + " " + (expression_Binary.expType!=null));
	if(!(e0.expType==e1.expType)){
		throw new SemanticException(expression_Binary.firstToken,"Exception in expression Binary");
	}
	else{
		Kind s=expression_Binary.getop();
		switch(s){
			case OP_EQ:
			case OP_NEQ: expression_Binary.expType=Type.BOOLEAN;break;
			case OP_GE:
			case OP_GT:
			case OP_LT:
			case OP_LE:{
				if(e0.expType==Type.INTEGER){
					expression_Binary.expType=Type.BOOLEAN;
				}
				}break;
			case OP_AND:
			case OP_OR:{
			if(e0.expType==Type.INTEGER || e0.expType==Type.BOOLEAN){
				expression_Binary.expType=e0.expType;
			}
			}break;
			case OP_DIV:
			case OP_MINUS:
			case OP_MOD:
			case OP_PLUS:
			case OP_POWER:
			case OP_TIMES:{
				
			if(e0.expType==Type.INTEGER){
				expression_Binary.expType=Type.INTEGER;
			}
			}break;
			default: {
				expression_Binary.expType=null;
			}
		}
		if(expression_Binary.expType==null){
			throw new SemanticException(expression_Binary.firstToken,"Exception in expression_Binary");
		}
	}
	return expression_Binary.expType;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		Expression e=expression_Unary.getE();
		e.visit(this, arg);
		Kind op=expression_Unary.getOp();
		switch(op){
		case OP_EXCL:
		{
			if(e.expType==Type.BOOLEAN ||e.expType==Type.INTEGER){
				expression_Unary.expType=e.expType;
			}
		}break;
		case OP_PLUS:
		case OP_MINUS:
		{
			if(e.expType==Type.INTEGER){
				expression_Unary.expType=Type.INTEGER;
			}
		}break;
		default:
			e.expType=null;
		}
		
		if(e.expType==null){
			throw new SemanticException(expression_Unary.firstToken,"Exception in Expression_Unary Visit");
		}
		return expression_Unary.expType;
		
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		Expression e0=index.getE0();
		e0.visit(this, arg);
		Expression e1=index.getE1();
		e1.visit(this, arg);
		boolean val=false;
		if(!(e0.expType==Type.INTEGER && e1.expType==Type.INTEGER))
		{
			throw new SemanticException(index.firstToken,"Exception in visiting Index");
		}
		else{
			
			index.setCartesian(!(index.e0.toString().contains("KW_r") && index.e1.toString().contains("KW_a")));
//			if(e0.firstToken.kind==Kind.KW_r && e1.firstToken.kind==Kind.KW_a)  {
//			index.setCartesian(false);
//			val=false;
//			}else if(e0.firstToken.kind==Kind.KW_x && e1.firstToken.kind==Kind.KW_y){
//				index.setCartesian(true);
//				val=true;
//			}
				//debugged and changed in Assn 6
		}
//		System.out.println(e0.firstToken.kind +"+++++++++++++++++++++"+e1.firstToken.kind);
//		System.out.println("_____________________________" + val + "=============");
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg) throws Exception {
		Index i=expression_PixelSelector.getIndex();
		
		Type nametype=lookupType(expression_PixelSelector.getName());
		//System.out.println(" exppixel " + nametype );
		
		if(nametype==Type.IMAGE){
			expression_PixelSelector.expType=Type.INTEGER;
		}
		else if(i==null){
			expression_PixelSelector.expType=nametype;
		}
		else{
			//System.out.println("/////////////////////"+expression_PixelSelector.isCartesian());
			//i.visit(this, arg);
			//System.out.println("/////////////////////"+expression_PixelSelector.isCartesian());
			expression_PixelSelector.expType=null;
		}
		if(expression_PixelSelector.expType==null){
			throw new SemanticException(expression_PixelSelector.firstToken,"Exception in Expression_PixelSelector");
		}
		
		if(i!=null)
			i.visit(this, arg);
		
		return expression_PixelSelector.expType;
	
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg) throws Exception {
		Expression trueE=expression_Conditional.trueE();
		trueE.visit(this, arg);
		Expression falseE=expression_Conditional.falseE();
		falseE.visit(this, arg);
		Expression condition=expression_Conditional.conditionE();
		condition.visit(this, arg);
		
		if(condition.expType==Type.BOOLEAN && trueE.expType==falseE.expType){
			expression_Conditional.expType=trueE.expType;
		}else
		{
			throw new SemanticException(expression_Conditional.firstToken,"Exception in Expression_Conditional");
		}
		
		return expression_Conditional.expType;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		Expression xSize=declaration_Image.xSize();
		Expression ySize=declaration_Image.ySize();
		Source s=declaration_Image.source();
		if(lookupType(declaration_Image.getName())!=null){
			throw new SemanticException(declaration_Image.firstToken,"Error in visiting Declaration_Variable");
		}else{
			insert(declaration_Image.getName(),declaration_Image);
			declaration_Image.declareType=Type.IMAGE;
			if(xSize!=null){
				xSize.visit(this, arg);
				if(ySize!=null){
					ySize.visit(this,arg);
				if(!(xSize.expType==Type.INTEGER &&ySize.expType==Type.INTEGER)){
					throw new SemanticException(declaration_Image.firstToken,"Exception in declaration_Image");	
				}
				}else{
					throw new SemanticException(declaration_Image.firstToken,"Exception in declaration_Image");
				}
			}
		}
		if(s!=null)
		s.visit(this, arg);
		return declaration_Image.declareType;
	}
	public boolean isValid(String fou){
		try{
			URL u=new URL(fou);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		if(isValid(source_StringLiteral.getFileOrUrl()))
		source_StringLiteral.sourceType=Type.URL;
		else
			source_StringLiteral.sourceType=Type.FILE;
			
		return source_StringLiteral.sourceType;
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg) throws Exception {
		Expression paramNum=source_CommandLineParam.getparam();
		paramNum.visit(this, arg);
		source_CommandLineParam.sourceType=null;//assignment 6 changes ma'm 
		if(paramNum.expType!=Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken,"Exception in source_CommandLineParam");
		}
		
		return source_CommandLineParam.sourceType;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
	source_Ident.sourceType=lookupType(source_Ident.getName());
	if(!(source_Ident.sourceType==Type.FILE || source_Ident.sourceType==Type.URL)){
		throw new SemanticException(source_Ident.firstToken,"Exception in source_Ident");
	}
	return source_Ident.sourceType;
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		Source s=declaration_SourceSink.getSource();
		s.visit(this, arg);
		if(lookupType(declaration_SourceSink.getName())!=null){
			throw new SemanticException(declaration_SourceSink.firstToken,"Error in visiting Declaration_Variable");
		}else{
			insert(declaration_SourceSink.getName(),declaration_SourceSink);	
			declaration_SourceSink.declareType=TypeUtils.getType(declaration_SourceSink.firstToken);
			if(s.sourceType==declaration_SourceSink.declareType || s.sourceType==null){//assignment 6 changes ma'm
				
			}
			else {
				throw new SemanticException(declaration_SourceSink.firstToken,"Exception in declaration_SourceSink");
			}
		}
		return declaration_SourceSink.declareType;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		expression_IntLit.expType=Type.INTEGER;
		return expression_IntLit.expType;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		Expression argu=expression_FunctionAppWithExprArg.arg();
		argu.visit(this, argu);
		
		if(argu.expType==Type.INTEGER){
			expression_FunctionAppWithExprArg.expType=Type.INTEGER;
		}else{
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken,"Exception in declaration_Image");
		}
		return expression_FunctionAppWithExprArg.expType;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		Index argu=expression_FunctionAppWithIndexArg.arg();
		argu.visit(this, argu);
		expression_FunctionAppWithIndexArg.expType=Type.INTEGER;
		
		return expression_FunctionAppWithIndexArg.expType;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg) throws Exception {
		expression_PredefinedName.expType=Type.INTEGER;
		return expression_PredefinedName.expType;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		Sink s=statement_Out.getSink();
		s.visit(this, arg);
		statement_Out.setDec(lookupDec(statement_Out.getName()));
		if(lookupDec(statement_Out.getName())==null)
		throw new SemanticException(statement_Out.firstToken,"Exception in Statement_out");
		Type nameType=lookupType(statement_Out.getName());
		if(!(((nameType==Type.INTEGER || nameType==Type.BOOLEAN) && s.sinkType==Type.SCREEN)||(nameType==Type.IMAGE && (s.sinkType==Type.FILE || s.sinkType==Type.SCREEN))))
			throw new SemanticException(statement_Out.firstToken,"Exception in Statement_Out");
		return null;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		Source s=statement_In.getSource();
		s.visit(this, arg);
		Declaration namedeclare=lookupDec(statement_In.getName());
		statement_In.setDec(namedeclare);
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		LHS lhs=statement_Assign.getLhs();
		lhs.visit(this, arg);
		Expression e=statement_Assign.getE();
		e.visit(this, arg);
		if(lhs.LHSType==e.expType || (lhs.LHSType==Type.IMAGE && e.expType==Type.INTEGER )){
			statement_Assign.setCartesian(lhs.isCartesian());
			//System.out.println("999999999999999"+lhs.isCartesian());
		}
		else{
			throw new SemanticException(statement_Assign.firstToken,"Exception in Statement Assign");
		}
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		lhs.setDeclare(lookupDec(lhs.getName()));
		lhs.LHSType=lookupType(lhs.getName());
		//System.out.println("LHSnew:   "  + lhs.getName());
		Index i=lhs.getIndex();
		if(i!=null){
		i.visit(this, arg);
		//System.out.println("Index is not null");
		lhs.setCartesian(i.isCartesian());//DOUBTFUL
		//System.out.println(i.isCartesian());
		}
		else
		{
			lhs.setCartesian(false);
		}
		return lhs.LHSType;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		sink_SCREEN.sinkType=Type.SCREEN;
		return sink_SCREEN.sinkType;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		sink_Ident.sinkType=lookupType(sink_Ident.getName());
		if(sink_Ident.sinkType!=Type.FILE){
			throw new SemanticException(sink_Ident.firstToken,"Exception in Sink_Ident");
		}
		return sink_Ident.sinkType;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		expression_BooleanLit.expType=Type.BOOLEAN;
		return expression_BooleanLit.expType;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		expression_Ident.expType=lookupType(expression_Ident.getName());
		return expression_Ident.expType;
	}

}
