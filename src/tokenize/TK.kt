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
		val _keywords = mutableMapOf<TK, String>()
		val _symbols = mutableMapOf<TK, String>()

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
		val DOUBLESLASH = symbol {
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
			_keywords[this] = "and"
		}
		val BREAK = keyword {
			_keywords[this] = "break"
		}
		val DO = keyword {
			_keywords[this] = "do"
		}
		val ELSE = keyword {
			_keywords[this] = "else"
		}
		val ELSEIF = keyword {
			_keywords[this] = "elseif"
		}
		val END = keyword {
			_keywords[this] = "end"
		}
		val FALSE = keyword {
			_keywords[this] = "false"
		}
		val FOR = keyword {
			_keywords[this] = "for"
		}
		val FUNCTION = keyword {
			_keywords[this] = "function"
		}
		val GOTO = keyword {
			_keywords[this] = "goto"
		}
		val IF = keyword {
			_keywords[this] = "if"
		}
		val IN = keyword {
			_keywords[this] = "in"
		}
		val LOCAL = keyword {
			_keywords[this] = "local"
		}
		val NIL = keyword {
			_keywords[this] = "nil"
		}
		val NOT = keyword {
			_keywords[this] = "not"
		}
		val OR = keyword {
			_keywords[this] = "or"
		}
		val REPEAT = keyword {
			_keywords[this] = "repeat"
		}
		val RETURN = keyword {
			_keywords[this] = "return"
		}
		val THEN = keyword {
			_keywords[this] = "then"
		}
		val TRUE = keyword {
			_keywords[this] = "true"
		}
		val UNTIL = keyword {
			_keywords[this] = "until"
		}
		val WHILE = keyword {
			_keywords[this] = "while"
		}

		val STR_LITERAL = TK()
		val NUM_LITERAL = TK()
		val INT_LITERAL = TK()
		val IDENTIFIER = TK()

		val keywords = _keywords
	}
}