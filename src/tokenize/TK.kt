package tokenize

operator fun Char.contains (tk:TK)
	= this in tk

@JvmInline
value class TK (val numeric: Int)
{
	constructor(ch:Char):this(ch.code)
	private constructor():this(SPECIAL_START++)

	operator fun contains (other:TK)
		= other.numeric == numeric

	operator fun contains (other:Char)
		= other.code == numeric

	companion object
	{
		private var SPECIAL_START = 0x100

		private fun newTk (bloc:TK.()->Unit):TK
		{
			return TK().apply(bloc)
		}

		val NULL = TK(-2)
		val EOS = TK(-1)

		val DASH = TK()
		val LBRACKET = TK()
		val EQ = TK()
		val DOUBLEEQ = TK()
		val LEQ = TK()
		val LSH = TK()
		val LT = TK()
		val GEQ = TK()
		val RSH = TK()
		val GT = TK()
		val DOUBLESLASH = TK()
		val SLASH = TK()
		val NEQ = TK()
		val SQUIGGLE = TK()
		val DOUBLECOLON = TK()
		val COLON = TK()
		val ELLIPSIS = TK()
		val CONCAT = TK()
		val DOT = TK()

		val AND = TK()
		val BREAK = TK()
		val DO = TK()
		val ELSE = TK()
		val ELSEIF = TK()
		val END = TK()
		val FALSE = TK()
		val FOR = TK()
		val FUNCTION = TK()
		val GOTO = TK()
		val IF = TK()
		val IN = TK()
		val LOCAL = TK()
		val NIL = TK()
		val NOT = TK()
		val OR = TK()
		val REPEAT = TK()
		val RETURN = TK()
		val THEN = TK()
		val TRUE = TK()
		val UNTIL = TK()
		val WHILE = TK()

		val STR_LITERAL = TK()
		val NUM_LITERAL = TK()
		val INT_LITERAL = TK()
		val IDENTIFIER = TK()

		val keywords = mapOf(
			"and" to AND,
			"break" to BREAK,
			"do" to DO,
			"else" to ELSE,
			"elseif" to ELSEIF,
			"end" to END,
			"false" to FALSE,
			"for" to FOR,
			"function" to FUNCTION,
			"goto" to GOTO,
			"if" to IF,
			"in" to IN,
			"local" to LOCAL,
			"nil" to NIL,
			"not" to NOT,
			"or" to OR,
			"repeat" to REPEAT,
			"return" to RETURN,
			"then" to THEN,
			"true" to TRUE,
			"until" to UNTIL,
			"while" to WHILE,
		)
	}
}