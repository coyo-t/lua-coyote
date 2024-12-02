package parse

import tokenize.TK

enum class UnaryOp
{
	MINUS,
	BNOT,
	NOT,
	LEN,
	NOUNOPR;


	val priority = 12

	companion object
	{
		operator fun get (tk: TK): UnaryOp
		{
			return when (tk)
			{
				TK.NOT -> NOT
				TK('-') -> MINUS
				TK('~') -> BNOT
				TK('#') -> LEN
				else -> NOUNOPR
			}
		}
	}
}


