package tokenize

operator fun Char.contains (tk:TK)
	= this in tk

@JvmInline
value class TK (val numeric: Int)
{
	constructor(ch:Char):this(ch.code)
	private constructor():this(SPECIAL_COUNTER++)

	operator fun contains (other:TK)
		= other.numeric == numeric

	operator fun contains (other:Char)
		= other.code == numeric

	companion object
	{
		private const val SPECIAL_START = 0x100
		private var SPECIAL_COUNTER = SPECIAL_START

		val _categories = mutableMapOf<TK, String>()
		val _tk2keywordname = mutableMapOf<TK, String>()
		val _keywordname2tk = mutableMapOf<String, TK>()
		val _symbols = mutableMapOf<TK, String>()

		private fun TK.addkw (s:String)
		{
			_tk2keywordname[this] = s
			_keywordname2tk[s] = this
		}

		private fun keyword (bloc:TK.()->Unit):TK
		{
			val outs = TK()
			_categories[outs] = "keyword"
			bloc.invoke(outs)
			return outs
		}

		private fun symbol (bloc:TK.()->Unit):TK
		{
			val outs = TK()
			_categories[outs] = "symbol"
			bloc.invoke(outs)
			return outs
		}

		val NULL = TK(-2)
		val EOS = TK(-1)

		val DASH = symbol {
			_symbols[this] = "-"
		}
		val LBRACKET = symbol {
			_symbols[this] = "["
		}
		val EQ = symbol {
			_symbols[this] = "="
		}
		val DOUBLEEQ = symbol {
			_symbols[this] = "=="
		}
		val LEQ = symbol {
			_symbols[this] = "<="
		}
		val LSH = symbol {
			_symbols[this] = "<<"
		}
		val LT = symbol {
			_symbols[this] = "<"
		}
		val GEQ = symbol {
			_symbols[this] = ">"
		}
		val RSH = symbol {
			_symbols[this] = ">>"
		}
		val GT = symbol {
			_symbols[this] = ">"
		}
		val IDIV = symbol {
			_symbols[this] = "//"
		}
		val SLASH = symbol {
			_symbols[this] = "/"
		}
		val NEQ = symbol {
			_symbols[this] = "~="
		}
		val SQUIGGLE = symbol {
			_symbols[this] = "~"
		}
		val DOUBLECOLON = symbol {
			_symbols[this] = "::"
		}
		val COLON = symbol {
			_symbols[this] = ":"
		}
		val ELLIPSIS = symbol {
			_symbols[this] = "..."
		}
		val CONCAT = symbol {
			_symbols[this] = ".."
		}
		val DOT = symbol {
			_symbols[this] = "."
		}

		val AND = keyword {
			addkw("and")
		}
		val BREAK = keyword {
			addkw("break")
		}
		val DO = keyword {
			addkw("do")
		}
		val ELSE = keyword {
			addkw("else")
		}
		val ELSEIF = keyword {
			addkw("elseif")
		}
		val END = keyword {
			addkw("end")
		}
		val FALSE = keyword {
			addkw("false")
		}
		val FOR = keyword {
			addkw("for")
		}
		val FUNCTION = keyword {
			addkw("function")
		}
		val GOTO = keyword {
			addkw("goto")
		}
		val IF = keyword {
			addkw("if")
		}
		val IN = keyword {
			addkw("in")
		}
		val LOCAL = keyword {
			addkw("local")
		}
		val NIL = keyword {
			addkw("nil")
		}
		val NOT = keyword {
			addkw("not")
		}
		val OR = keyword {
			addkw("or")
		}
		val REPEAT = keyword {
			addkw("repeat")
		}
		val RETURN = keyword {
			addkw("return")
		}
		val THEN = keyword {
			addkw("then")
		}
		val TRUE = keyword {
			addkw("true")
		}
		val UNTIL = keyword {
			addkw("until")
		}
		val WHILE = keyword {
			addkw("while")
		}

		val STR_LITERAL = TK()
		val NUM_LITERAL = TK()
		val INT_LITERAL = TK()
		val IDENTIFIER = TK()

		val keywords = _keywordname2tk
	}
}