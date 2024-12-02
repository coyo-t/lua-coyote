package parse

enum class ExprKind
{
	/*
	when 'expdesc' describes the last expression of a list,
	this kind means an empty list (so, no expression)
	*/
	VOID,

	/* constant nil */
	NIL,

	/* constant true */
	TRUE,

	/* constant false */
	FALSE,

	/* constant in 'k'; info = index of constant in 'k' */
	K,

	/* floating constant; nval = numerical float value */
	KFLT,

	/* integer constant; ival = numerical integer value */
	KINT,

	/* string constant; strval = TString address; (string is fixed by the lexer) */
	KSTR,

	/* expression has its value in a fixed register; info = result register */
	NONRELOC,

	/* local variable; var.ridx = register index; var.vidx = relative index in 'actvar.arr'  */
	LOCAL,

	/* upvalue variable; info = index of upvalue in 'upvalues' */
	UPVAL,

	/* compile-time <const> variable; info = absolute index in 'actvar.arr'  */
	CONST,

	/*
	indexed variable;
	ind.t = table register;
	ind.idx = key's R index
	*/
	INDEXED,

	/*
	indexed upvalue;
	ind.t = table upvalue;
	ind.idx = key's K index
	*/
	INDEXUP,

	/*
	indexed variable with constant integer;
	ind.t = table register;
	ind.idx = key's value
	*/
	INDEXI,

	/*
	indexed variable with literal string;
	ind.t = table register;
	ind.idx = key's K index
	*/
	INDEXSTR,
	/*
	expression is a test/comparison;
	info = pc of corresponding jump instruction
	*/
	JMP,

	/*
	expression can put result in any register; info = instruction pc
	*/
	RELOC,

	/* expression is a function call; info = instruction pc */
	CALL,

	/* vararg expression; info = instruction pc */
	VARARG
}