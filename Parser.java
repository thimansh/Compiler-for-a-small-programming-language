package cop5556fa17;

import java.util.*;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;

import cop5556fa17.AST.*;
import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	Scanner scanner;
	Token t;
	ArrayList<ASTNode> arr = new ArrayList<ASTNode>();

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	Program program() throws SyntaxException {
		Token n = null;
		if (t.kind == IDENTIFIER) {
			n = t;
		}
		match(IDENTIFIER);
		Program p = null;
		if (isKind(KW_int) || isKind(KW_image) || isKind(KW_boolean) || isKind(KW_url) || isKind(KW_file)
				|| isKind(IDENTIFIER)) {
			while (isKind(KW_int) || isKind(KW_image) || isKind(KW_boolean) || isKind(KW_url) || isKind(KW_file)
					|| isKind(IDENTIFIER)) {
				if (isKind(KW_int) || isKind(KW_image) || isKind(KW_boolean) || isKind(KW_url) || isKind(KW_file)) {
					arr.add(declaration());
					match(SEMI);
				} else if (isKind(IDENTIFIER)) {
					arr.add(statement());
					match(SEMI);
				}
			}
		} else if (isKind(EOF)) {
		} else {
			throw new SyntaxException(t, "Exception in Program at " + t.line + ":" + t.pos_in_line);
		}
		p = new Program(n, n, arr);
		return p;
	}

	Declaration declaration() throws SyntaxException {
		Declaration d = null;
		if (isKind(KW_int) || isKind(KW_boolean)) {
			d = vardeclare();
		} else if (isKind(KW_image)) {
			d = imagedeclare();
		} else if (isKind(KW_url) || isKind(KW_file)) {
			d = sourcesinkdeclare();
		} else {
			throw new SyntaxException(t, "Exception in declaration at " + t.line + ":" + t.pos_in_line);
		}
		return d;
	}

	Declaration_Image imagedeclare() throws SyntaxException {
		Token n = null;
		if (t.kind == KW_image) {
			n = t;
		}
		match(KW_image);
		Expression e0 = null;
		Expression e1 = null;
		Source s0 = null;
		if (isKind(LSQUARE)) {
			consume();
			e0 = expression();
			match(COMMA);
			e1 = expression();
			match(RSQUARE);
		}
		Token a = t;
		match(IDENTIFIER);
		if (isKind(OP_LARROW)) {
			consume();
			s0 = source();
		}
		return new Declaration_Image(n, e0, e1, a, s0);
	}

	Source source() throws SyntaxException {
		Source s0 = null;
		Token n = t;
		Expression e0 = null;
		if (isKind(STRING_LITERAL)) {
			consume();
			s0 = new Source_StringLiteral(n, n.getText());
		} else if (isKind(OP_AT)) {
			consume();
			e0 = expression();
			s0 = new Source_CommandLineParam(n, e0);
		} else if (isKind(IDENTIFIER)) {
			consume();
			s0 = new Source_Ident(n, n);
		} else {
			throw new SyntaxException(t, "Exception in source at " + t.line + ":" + t.pos_in_line);
		}
		return s0;
	}

	Declaration_SourceSink sourcesinkdeclare() throws SyntaxException {
		Token n = t;
		Source s0 = null;
		if (isKind(KW_url) || isKind(KW_file)) {
			consume();
		}
		Token a = t;
		match(IDENTIFIER);
		match(OP_ASSIGN);
		s0 = source();
		return new Declaration_SourceSink(n, n, a, s0);
	}

	Declaration_Variable vardeclare() throws SyntaxException {
		Expression e0 = null;
		Token n = t;
		Token a = null;
		if (isKind(KW_int) || isKind(KW_boolean)) {
			consume();
		}
		a = t;
		match(IDENTIFIER);
		if (isKind(OP_ASSIGN)) {
			consume();
			e0 = expression();
		}
		return new Declaration_Variable(n, n, a, e0);
	}

	Statement statement() throws SyntaxException {
		Sink sink1 = null;
		Source s0 = null;
		Expression e0 = null;
		Index i = null;
		Token n = t;
		Statement s = null;
		if (isKind(IDENTIFIER)) {
			consume();
		} else {
			throw new SyntaxException(t, "Exception in VariableDeclaration at " + t.line + ":" + t.pos_in_line);
		}
		if (isKind(LSQUARE) || isKind(OP_ASSIGN)) {
			if (isKind(LSQUARE)) {
				consume();
				i = LhsSelector();
				if (isKind(RSQUARE)) {
					consume();
				} else {
					throw new SyntaxException(t, "Exception in VariableDeclaration at " + t.line + ":" + t.pos_in_line);
				}
				match(OP_ASSIGN);
				e0 = expression();
			} else if (isKind(OP_ASSIGN)) {
				consume();
				e0 = expression();
			}
			s = new Statement_Assign(n, new LHS(n, n, i), e0);
		} else if (isKind(OP_RARROW)) {
			consume();
			sink1 = sink();
			s = new Statement_Out(n, n, sink1);
		} else if (isKind(OP_LARROW)) {
			consume();
			s0 = source();
			s = new Statement_In(n, n, s0);
		} else {
			throw new SyntaxException(t, "Exception in VariableDeclaration at " + t.line + ":" + t.pos_in_line);
		}

		return s;
	}

	Sink sink() throws SyntaxException {
		Token n = t;
		Sink s = null;
		if (isKind(IDENTIFIER)) {
			consume();
			s = new Sink_Ident(n, n);
		} else if (isKind(KW_SCREEN)) {
			consume();
			s = new Sink_SCREEN(n);
		} else
			throw new SyntaxException(t, "Exception in sink at " + t.line + ":" + t.pos_in_line);
		return s;
	}

	Expression expression() throws SyntaxException {
		Token n = t;
		Expression or = Orexpression();
		Expression e0 = null;
		Expression e1 = null;
		if (isKind(OP_Q)) {
			consume();
			e0 = expression();
			match(OP_COLON);
			e1 = expression();
		}
		if (e0 == null && e1 == null)
			return or;
		return new Expression_Conditional(n, or, e0, e1);
	}

	Expression Orexpression() throws SyntaxException {
		Token n = t;
		Token op = null;
		Expression e0 = AndExpression();
		Expression e1 = null;
		while (isKind(OP_OR)) {
			op = t;
			consume();
			e1 = AndExpression();
			e0 = new Expression_Binary(n, e0, op, e1);
		}
		return e0;
	}

	Expression AndExpression() throws SyntaxException {
		Token n = t;
		Token op = null;
		Expression e0 = EqExpression();
		Expression e1 = null;
		while (isKind(OP_AND)) {
			op = t;
			consume();
			e1 = EqExpression();
			e0 = new Expression_Binary(n, e0, op, e1);
		}
		return e0;
	}

	Expression EqExpression() throws SyntaxException {
		Token n = t;
		Token op = null;
		Expression e0 = RelExpression();
		Expression e1 = null;
		while (isKind(OP_EQ) || isKind(OP_NEQ)) {
			op = t;
			consume();
			e1 = RelExpression();
			e0 = new Expression_Binary(n, e0, op, e1);
		}
		return e0;
	}

	Expression RelExpression() throws SyntaxException {
		Token n = t;
		Token op = null;
		Expression e0 = AddExpression();
		Expression e1 = null;
		while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
			op = t;
			consume();
			e1 = AddExpression();
			e0 = new Expression_Binary(n, e0, op, e1);
		}
		return e0;
	}

	Expression AddExpression() throws SyntaxException {
		Token n = t;
		Token op = null;
		Expression e0 = MultExpression();
		Expression e1 = null;
		while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
			op = t;
			consume();
			e1 = MultExpression();
			e0 = new Expression_Binary(n, e0, op, e1);
		}
		return e0;
	}

	Expression MultExpression() throws SyntaxException {
		Token n = t;
		Token op = null;
		Expression e0 = UnaryExpression();
		Expression e1 = null;
		while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
			op = t;
			consume();
			e1 = UnaryExpression();
			e0 = new Expression_Binary(n, e0, op, e1);
		}
		return e0;
	}

	Expression Primary() throws SyntaxException {
		Token n = t;
		Expression p = null;
		if (isKind(INTEGER_LITERAL) || isKind(BOOLEAN_LITERAL)) {
			if (isKind(INTEGER_LITERAL)) {
				consume();
				p = new Expression_IntLit(n, n.intVal());
			} else {
				consume();
				p = new Expression_BooleanLit(n, Boolean.parseBoolean((n.getText())));
			}
		} else if (isKind(LPAREN)) {
			consume();
			p = expression();
			match(RPAREN);
		} else
			p = functionapplication();
		return p;
	}

	Expression functionapplication() throws SyntaxException {
		Token n = t;
		Expression e0 = null;
		Index i = null;
		Expression p = null;
		FunctionName();
		if (isKind(LPAREN) || isKind(LSQUARE)) {
			if (isKind(LPAREN)) {
				consume();
				e0 = expression();
				match(RPAREN);
				p = new Expression_FunctionAppWithExprArg(n, n.kind, e0);
			} else if (isKind(LSQUARE)) {
				consume();
				i = Selector();
				match(RSQUARE);
				p = new Expression_FunctionAppWithIndexArg(n, n.kind, i);
			}
		}
		return p;
	}

	void FunctionName() throws SyntaxException {
		if (isKind(KW_sin) || isKind(KW_cos) || isKind(KW_atan) || isKind(KW_abs) || isKind(KW_cart_x)
				|| isKind(KW_cart_y) || isKind(KW_polar_a) || isKind(KW_polar_r))
			consume();
		else
			throw new SyntaxException(t, "Exception in FunctionName at " + t.line + ":" + t.pos_in_line);
	}

	Expression UnaryExpression() throws SyntaxException {
		Token n = t;
		Expression e0 = null;
		Token op = null;
		Expression p = null;
		if (isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(OP_EXCL)) {
			op = t;
			consume();
			e0 = UnaryExpression();
			p = new Expression_Unary(n, op, e0);
		} else if (isKind(INTEGER_LITERAL) || isKind(LPAREN) || isKind(KW_sin) || isKind(KW_cos) || isKind(KW_atan)
				|| isKind(KW_abs) || isKind(KW_cart_x) || isKind(KW_cart_y) || isKind(KW_polar_a) || isKind(KW_polar_r)
				|| isKind(BOOLEAN_LITERAL)) {
			p = Primary();
		} else if (isKind(IDENTIFIER)) {
			p = IdentOrPixelSelectorExpression();
		} else if (isKind(KW_y) || isKind(KW_x) || isKind(KW_r) || isKind(KW_a) || isKind(KW_X) || isKind(KW_Y)
				|| isKind(KW_Z) || isKind(KW_A) || isKind(KW_R) || isKind(KW_DEF_X) || isKind(KW_DEF_Y)) {
			consume();
			p = new Expression_PredefinedName(n, n.kind);
		} else
			throw new SyntaxException(t, "Exception in UnaryExpression at " + t.line + ":" + t.pos_in_line);
		return p;
	}

	Index LhsSelector() throws SyntaxException {
		match(LSQUARE);
		Index i = null;
		if (isKind(KW_x)) {
			i = XySelector();
		} else if (isKind(KW_r)) {
			i = RaSelector();
		} else {
			throw new SyntaxException(t, "Exception in LhsSelector at " + t.line + ":" + t.pos_in_line);
		}
		match(RSQUARE);
		return i;
	}

	Index RaSelector() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token n = null;
		Token n1 = null;
		if (isKind(KW_r)) {
			n = t;
			consume();
			e0 = new Expression_PredefinedName(n, n.kind);
		} else {
			throw new SyntaxException(t, "Exception matching token at " + t.line + ":" + t.pos_in_line);
		}
		match(COMMA);
		if (isKind(KW_a)) {//changed in Assn 6 Before KW_A
			n1 = t;
			consume();
			e1 = new Expression_PredefinedName(n1, n1.kind);
		} else {
			throw new SyntaxException(t, "Exception matching token at " + t.line + ":" + t.pos_in_line);
		}
		return new Index(n, e0, e1);
	}

	Index XySelector() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token n = null;
		Token n1 = null;
		if (isKind(KW_x)) {
			n = t;
			consume();
			e0 = new Expression_PredefinedName(n, n.kind);
		} else {
			throw new SyntaxException(t, "Exception matching token at " + t.line + ":" + t.pos_in_line);
		}
		match(COMMA);
		if (isKind(KW_y)) {
			n1 = t;
			consume();
			e1 = new Expression_PredefinedName(n1, n1.kind);
		} else {
			throw new SyntaxException(t, "Exception matching token at " + t.line + ":" + t.pos_in_line);
		}
		return new Index(n, e0, e1);
	}

	Expression IdentOrPixelSelectorExpression() throws SyntaxException {
		Token n = t;
		Index i = null;
		Expression p = null;
		match(IDENTIFIER);
		if (isKind(LSQUARE)) {
			consume();
			i = Selector();
			match(RSQUARE);
			p = new Expression_PixelSelector(n, n, i);
		} else
			p = new Expression_Ident(n, n);
		return p;
	}

	Index Selector() throws SyntaxException {
		Token n = t;
		Expression e1 = expression();
		match(COMMA);
		Expression e2 = expression();
		return new Index(n, e1, e2);
	}

	public void consume() {
		t = scanner.nextToken();
	}

	public boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	public void match(Kind kind) throws SyntaxException {
		if (isKind(kind)) {
			consume();
		} else {
			throw new SyntaxException(t, "Exception matching token at " + t.line + ":" + t.pos_in_line);
		}
	}

	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
