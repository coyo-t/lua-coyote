package tokenize

sealed class Token
{
	class Keyword private constructor (val kw: String): Token()
	{
		init
		{
			registered += kw
			nametable[kw] = this
		}

		override fun toString(): String
		{
			return "<Keyword: $kw>"
		}

		companion object
		{
			private val registered = mutableSetOf<String>()
			private val nametable = mutableMapOf<String, Token>()

			operator fun contains (s:String) = s in registered

			operator fun get (k:String) = nametable[k]!!

			val AND = Keyword("and")
			val BREAK = Keyword("break")
			val DO = Keyword("do")
			val ELSE = Keyword("else")
			val ELSEIF = Keyword("elseif")
			val END = Keyword("end")
			val FALSE = Keyword("false")
			val FOR = Keyword("for")
			val FUNCTION = Keyword("function")
			val GOTO = Keyword("goto")
			val IF = Keyword("if")
			val IN = Keyword("in")
			val LOCAL = Keyword("local")
			val NIL = Keyword("nil")
			val NOT = Keyword("not")
			val OR = Keyword("or")
			val REPEAT = Keyword("repeat")
			val RETURN = Keyword("return")
			val THEN = Keyword("then")
			val TRUE = Keyword("true")
			val UNTIL = Keyword("until")
			val WHILE = Keyword("while")
		}
	}

	class Symbol (val symbol: String): Token()
	{
		constructor (ch:Char): this(ch.toString())

		override fun toString(): String
		{
			return "<Symbol: $symbol >"
		}

		companion object
		{
			val DASH = Symbol("-")
			val LBRACKET = Symbol("[")
			val EQ = Symbol("=")
			val DOUBLEEQ = Symbol("==")
			val LEQ = Symbol("<=")
			val LSH = Symbol("<<")
			val LT = Symbol("<")
			val GEQ = Symbol(">=")
			val RSH = Symbol(">>")
			val GT = Symbol(">")
			val DOUBLESLASH = Symbol("//")
			val SLASH = Symbol("/")
			val NEQ = Symbol("~=")
			val SQUIGGLE = Symbol("~")
			val DOUBLECOLON = Symbol("::")
			val COLON = Symbol(":")
			val ELLIPSIS = Symbol("...")
			val CONCAT = Symbol("..")
			val DOT = Symbol(".")
		}
	}

	//TODO:
	//	string literals need to have their data
	//	in a bytebuffer or memorysegment, as thats what
	//	they are in lua. storing them in strings is largely
	//	convienent but inconsistent
	class StringLiteral (val body:String): Token()
	{
		override fun toString(): String
		{
			return "<String Literal: \"$body\">"
		}
	}

	class IntLiteral (val value:Int): Token()
	{
		override fun toString(): String
		{
			return "<Int Literal: $value >"
		}
	}

	class NumberLiteral (val value:Double): Token()
	{
		override fun toString(): String
		{
			return "Number Literal: $value >"
//			return "Number Literal: %.8f >".format(value)
		}
	}

	class Identifier (val name:String):Token()
	{
		override fun toString(): String
		{
			return "<Identifier: $name>"
		}
	}

	data object EndOfStream : Token()
	{
		override fun toString(): String
		{
			return "<End-of-Stream>"
		}
	}
}