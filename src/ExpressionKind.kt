/* kinds of variables/expressions */
enum class ExpressionKind
{
	/*
	when 'expdesc' describes the last expression of a list,
	this kind means an empty list (so, no expression)
	*/
	VVOID,

	/* constant nil */
	VNIL,

	/* constant true */
	VTRUE,

	/* constant false */
	VFALSE,

	/* constant in 'k'; info = index of constant in 'k' */
	VK,

	/* floating constant; nval = numerical float value */
	VKFLT,

	/* integer constant; ival = numerical integer value */
	VKINT,

	/* string constant; strval = TString address; (string is fixed by the lexer) */
	VKSTR,

	/* expression has its value in a fixed register; info = result register */
	VNONRELOC,

	/* local variable; var.ridx = register index; var.vidx = relative index in 'actvar.arr'  */
	VLOCAL,

	/* upvalue variable; info = index of upvalue in 'upvalues' */
	VUPVAL,

	/* compile-time <const> variable; info = absolute index in 'actvar.arr'  */
	VCONST,

	/*
	indexed variable;
	ind.t = table register;
	ind.idx = key's R index
	*/
	VINDEXED,

	/*
	indexed upvalue;
	ind.t = table upvalue;
	ind.idx = key's K index
	*/
	VINDEXUP,

	/*
	indexed variable with constant integer;
	ind.t = table register;
	ind.idx = key's value
	*/
	VINDEXI,

	/*
	indexed variable with literal string;
	ind.t = table register;
	ind.idx = key's K index
	*/
	VINDEXSTR,
	/*
	expression is a test/comparison;
	info = pc of corresponding jump instruction
	*/
	VJMP,

	/*
	expression can put result in any register; info = instruction pc
	*/
	VRELOC,

	/* expression is a function call; info = instruction pc */
	VCALL,

	/* vararg expression; info = instruction pc */
	VVARARG
}