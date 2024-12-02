package parse

import tokenize.TK

enum class BinaryOp(
	val lPriority:Int,
	val rPriority:Int,
)
{
	// arithmetic operators
	ADD(10, 10),
	SUB(10, 10),
	MUL(11, 11),
	MOD(11, 11),
	POW(14, 13),
	DIV(11, 11),
	IDIV(11, 11),
	// bitwise operators
	BAND(6, 6),
	BOR(4, 4),
	BXOR(5, 5),
	SHL(7, 7),
	SHR(7, 7),
	// string operators
	CONCAT(9, 8),
	// comparison operators
	EQ(3, 3),
	LT(3, 3),
	LE(3, 3),
	NE(3, 3),
	GT(3, 3),
	GE(3, 3),
	// logical operators
	AND(2, 2),
	OR(1, 1),

	NOBINOPR(0, 0);

	companion object
	{
		operator fun get (tk:TK): BinaryOp
		{
			return when (tk)
			{
				TK('+') -> ADD
				TK('-') -> SUB
				TK('*') -> MUL
				TK('%') -> MOD
				TK('^') -> POW
				TK('/') -> DIV
				TK.IDIV -> IDIV
				TK('&') -> BAND
				TK('|') -> BOR
				TK('~') -> BXOR
				TK.LSH -> SHL
				TK.RSH -> SHR
				TK.CONCAT -> CONCAT
				TK.NEQ -> NE
				TK.EQ -> EQ
				TK('<') -> LT
				TK.LEQ -> LE
				TK('>') -> GT
				TK.GEQ -> GE
				TK.AND -> AND
				TK.OR -> OR
				else -> NOBINOPR
			}
		}
	}


}