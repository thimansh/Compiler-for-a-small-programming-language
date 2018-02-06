package cop5556fa17;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	public String getDesc(Type t) {
		switch (t) {
		case INTEGER:
			return "I";
		case BOOLEAN:
			return "Z";
		case IMAGE:
			return ImageSupport.ImageDesc;
		case FILE:
		case URL:
			return ImageSupport.StringDesc;
		default:
			throw new RuntimeException("unimplemented type called: " + t);
		}

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		FieldVisitor fv = cw.visitField(ACC_STATIC, "x", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "y", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "X", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "Y", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "r", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "a", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "R", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "A", "I", null, new Integer(0));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "Z", "I", null, new Integer(16777215));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "DEF_X", "I", null, new Integer(256));
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, "DEF_Y", "I", null, new Integer(256));
		fv.visitEnd();
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// if GRADE, generates code to add string to log
		// CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		// and instructions to main method, respectively

		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		// generates code to add string to log
		// CodeGenUtils.genLog(GRADE, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		// handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		Object initValue = null;
		String getdesc = getDesc(declaration_Variable.declareType);
		if (getdesc.equals("I"))
			initValue = new Integer(0);
		if (getdesc.equals("Z"))
			initValue = new Boolean(true);

		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_Variable.getName(),
				getDesc(declaration_Variable.declareType), null, initValue);
		fv.visitEnd();
		if (declaration_Variable.getE() != null) {
			declaration_Variable.getE().visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.getName(),
					getDesc(declaration_Variable.declareType));
		}

		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		Label lTrue = new Label();
		Label lFalse = new Label();
		expression_Binary.getE0().visit(this, arg);
		expression_Binary.getE1().visit(this, arg);

		switch (expression_Binary.getop()) {
		case OP_EQ:
			mv.visitJumpInsn(IF_ICMPEQ, lTrue);
			mv.visitLdcInsn(false);
			break;
		case OP_NEQ:
			mv.visitJumpInsn(IF_ICMPNE, lTrue);
			mv.visitLdcInsn(false);
			break;
		case OP_GE:
			mv.visitJumpInsn(IF_ICMPGE, lTrue);
			mv.visitLdcInsn(false);
			break;
		case OP_GT:
			mv.visitJumpInsn(IF_ICMPGT, lTrue);
			mv.visitLdcInsn(false);
			break;
		case OP_LT:
			mv.visitJumpInsn(IF_ICMPLT, lTrue);
			mv.visitLdcInsn(false);
			break;
		case OP_LE:
			mv.visitJumpInsn(IF_ICMPLE, lTrue);
			mv.visitLdcInsn(false);
			break;
		case OP_AND:
			mv.visitInsn(IAND);
			break;
		case OP_OR:
			mv.visitInsn(IOR);
			break;
		case OP_DIV:
			mv.visitInsn(IDIV);
			break;
		case OP_MINUS:
			mv.visitInsn(ISUB);
			break;
		case OP_MOD:
			mv.visitInsn(IREM);
			break;
		case OP_PLUS:
			mv.visitInsn(IADD);
			break;
		case OP_TIMES:
			mv.visitInsn(IMUL);
			break;
		default:
			break;
		}
		mv.visitJumpInsn(GOTO, lFalse);
		mv.visitLabel(lTrue);
		mv.visitLdcInsn(true);
		mv.visitLabel(lFalse);
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.expType);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {

		expression_Unary.getE().visit(this, arg);
		if (getDesc(expression_Unary.getE().expType).equals("Z")) {
			if (expression_Unary.getOp() == Kind.OP_EXCL) {
				Label lTrue = new Label();
				Label lFalse = new Label();
				mv.visitJumpInsn(IFNE, lFalse);
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, lTrue);
				mv.visitLabel(lFalse);
				mv.visitLdcInsn(false);
				mv.visitLabel(lTrue);
			}
		} else {
			switch (expression_Unary.getOp()) {
			case OP_EXCL:
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
				break;
			case OP_MINUS:
				mv.visitInsn(INEG);
				break;
			default:
				break;
			}
		}
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.expType);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		//System.out.println("000000000888888888888"+index.isCartesian());
		index.getE0().visit(this, arg);
		index.getE1().visit(this, arg);
//		System.out.println("***************" + index.getE0().firstToken);
//		System.out.println("***************" + index.getE1().firstToken);
//		System.out.println("0000000001111111111111"+index.isCartesian());
		//System.out.println(x);
		if (!index.isCartesian()) {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}

		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.getName(), ImageSupport.ImageDesc);
		//System.out.println("---------------"+ expression_PixelSelector.getIndex().isCartesian());
		expression_PixelSelector.getIndex().visit(this, arg);
		//System.out.println("---------------"+ expression_PixelSelector.getIndex().isCartesian());
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Label lTrue = new Label();
		Label lFalse = new Label();
		expression_Conditional.conditionE().visit(this, arg);
		mv.visitJumpInsn(IFNE, lTrue);
		expression_Conditional.falseE().visit(this, arg);
		mv.visitJumpInsn(GOTO, lFalse);
		mv.visitLabel(lTrue);
		expression_Conditional.trueE().visit(this, arg);
		mv.visitLabel(lFalse);

		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueE().expType);
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// Object initValue = null;
		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_Image.getName(), getDesc(declaration_Image.declareType),
				null, null);
		fv.visitEnd();
		if (declaration_Image.source() != null) {
			declaration_Image.source().visit(this, arg);
			if (declaration_Image.xSize() != null && declaration_Image.ySize() != null) {
				declaration_Image.xSize().visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize().visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);

		} else {
			if (declaration_Image.xSize() != null && declaration_Image.ySize() != null) {
				declaration_Image.xSize().visit(this, arg);
				declaration_Image.ySize().visit(this, arg);
			} else {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
		}

		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.getName(), ImageSupport.ImageDesc);

		return null;

	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(new String(source_StringLiteral.getFileOrUrl()));
		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.getparam().visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.getName(), ImageSupport.StringDesc);
		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_SourceSink.getName(),
				getDesc(declaration_SourceSink.declareType), null, null);
		fv.visitEnd();
		if (declaration_SourceSink.getSource() != null) {
			declaration_SourceSink.getSource().visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.getName(), ImageSupport.StringDesc);
		}
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		mv.visitLdcInsn(new Integer(expression_IntLit.value()));
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_IntLit.expType);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg().visit(this, arg);
		switch (expression_FunctionAppWithExprArg.func()) {
		case KW_abs:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
			break;
		case KW_log:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		System.out.println("---------------========="+ expression_FunctionAppWithIndexArg.arg().isCartesian());
		expression_FunctionAppWithIndexArg.arg().getE0().visit(this, arg);
		expression_FunctionAppWithIndexArg.arg().getE1().visit(this, arg);
		System.out.println("---------------======="+ expression_FunctionAppWithIndexArg.arg().isCartesian());
		switch (expression_FunctionAppWithIndexArg.func()) {
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_aSig, false);
			break;
		default:
			break;
		}
		return null;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		switch (expression_PredefinedName.getKind()) {
		case KW_x:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			break;
		case KW_y:
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			break;
		case KW_X:
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			break;
		case KW_Y:
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			break;
		case KW_r:
			mv.visitFieldInsn(GETSTATIC, className, "r", "I");
			break;
		case KW_a:
			mv.visitFieldInsn(GETSTATIC, className, "a", "I");
			break;
		case KW_R:
			mv.visitFieldInsn(GETSTATIC, className, "R", "I");
			break;
		case KW_A:
			mv.visitFieldInsn(GETSTATIC, className, "A", "I");
			break;
		case KW_DEF_X:
			mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
			break;
		case KW_DEF_Y:
			mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
			break;
		case KW_Z:
			mv.visitFieldInsn(GETSTATIC, className, "Z", "I");
			break;
		default:
			break;
		}

		return null;

	}

	/**
	 * For Integers and booleans, the only "sink"is the screen, so generate code to
	 * print to console. For Images, load the Image onto the stack and visit the
	 * Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {

		if (getDesc(statement_Out.getDec().declareType).equals(ImageSupport.ImageDesc)) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.getName(),
					getDesc(statement_Out.getDec().declareType));
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().declareType);
			statement_Out.getSink().visit(this, arg);
		} else {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.getName(),
					getDesc(statement_Out.getDec().declareType));
			if (getDesc(statement_Out.getDec().declareType).equals(ImageSupport.ImageDesc))
				statement_Out.getSink().visit(this, arg);
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().declareType);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(" + getDesc(statement_Out.getDec().declareType) + ")V", false);
		}
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 * In HW5, you only need to handle INTEGER and BOOLEAN Use
	 * java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean to convert
	 * String to actual type.
	 *  
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {

		statement_In.getSource().visit(this, arg);

		switch (statement_In.getDec().declareType) {
		case BOOLEAN:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			break;
		case INTEGER:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			break;
		case IMAGE:
			if (((Declaration_Image) statement_In.getDec()).xSize() != null
					&& ((Declaration_Image) statement_In.getDec()).ySize() != null) {
				((Declaration_Image) statement_In.getDec()).xSize().visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				((Declaration_Image) statement_In.getDec()).xSize().visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			break;
		default:
			break;
		}
		mv.visitFieldInsn(PUTSTATIC, className, statement_In.getName(), getDesc(statement_In.getDec().declareType));
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if (getDesc(statement_Assign.getLhs().LHSType).equals(ImageSupport.ImageDesc)) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.getLhs().getName(), ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.getLhs().getName(), ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
			Label getxVal = new Label();
			Label comparexVal = new Label();
			Label getyVal = new Label();
			Label compareyVal = new Label();
			Label insideLoop = new Label();
			mv.visitLabel(getxVal);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitJumpInsn(GOTO, comparexVal);
			mv.visitLabel(getyVal);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitJumpInsn(GOTO, compareyVal);
			mv.visitLabel(insideLoop);
			System.out.println("000000000"+statement_Assign.isCartesian());
			if (!statement_Assign.isCartesian()) {
				mv.visitFieldInsn(GETSTATIC, className, "x", "I");
				mv.visitFieldInsn(GETSTATIC, className, "y", "I");
				mv.visitInsn(DUP2);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_aSig,
						false);
				mv.visitFieldInsn(PUTSTATIC, className, "r", "I");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_rSig,
						false);
				mv.visitFieldInsn(PUTSTATIC, className, "a", "I");
			}
			statement_Assign.getE().visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.getLhs().getName(), ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			statement_Assign.getLhs().visit(this, arg);

			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitLabel(compareyVal);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitJumpInsn(IF_ICMPLT, insideLoop);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitLabel(comparexVal);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitJumpInsn(IF_ICMPLT, getyVal);
		} else {
			statement_Assign.getE().visit(this, arg);
			statement_Assign.getLhs().visit(this, arg);
		}
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		if (getDesc(lhs.LHSType).equals(ImageSupport.ImageDesc)) {
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		} else
			mv.visitFieldInsn(PUTSTATIC, className, lhs.getName(), getDesc(lhs.LHSType));
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.getName(), ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);// ,
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		mv.visitLdcInsn(new Boolean(expression_BooleanLit.getval()));
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_BooleanLit.expType);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, expression_Ident.getName(), getDesc(expression_Ident.expType));
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.expType);
		return null;
	}

}
