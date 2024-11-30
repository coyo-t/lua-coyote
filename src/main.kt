// https://en.cppreference.com/w/c/language/escape
const val EOS = Char.MAX_VALUE

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
			return "<Symbol: $symbol>"
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
	class StringLiteral (val body:String): Token()
	{
		override fun toString(): String
		{
			return "<String Literal: \"$body\">"
		}
	}
	class Comment: Token()
	{
		override fun toString(): String
		{
			return "<Comment>"
		}
	}
	class IntLiteral (val value:Int): Token()
	{
		override fun toString(): String
		{
			TODO()
		}
	}
	class NumberLiteral (val value:Double): Token()
	{
		override fun toString(): String
		{
			TODO()
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


const val ESCF = '\u000c'
const val ESCV = '\u000b'

val UNDERSCORE = setOf('_')

val ALPHABETICAL_SYMBOLS = (
	('a'..'z').toSet() +
	('A'..'Z').toSet()
)
val DIGIT_SYMBOLS = ('0'..'9').toSet()

val OKAY_TO_START_IDENTIFIER = ALPHABETICAL_SYMBOLS + UNDERSCORE

val IDENTIFIER_SYMBOLS = OKAY_TO_START_IDENTIFIER + DIGIT_SYMBOLS
val HEXIDECIMAL_SYMBOLS = ALPHABETICAL_SYMBOLS + UNDERSCORE + DIGIT_SYMBOLS


val CONSIDERED_WHITESPACE = setOf(
	'\u0009', // t
	'\u000a', // n
	'\u000b', // v
	'\u000c', // f
	'\u000d', // r
	'\u0020', // space
)

class LStringReader(text: String): StringReader(text)
{
	val tokens = mutableListOf<Token>()
	val tokenRanges = mutableListOf<IntRange>()
	val tokenLineNumbers = mutableListOf<Int>()

	fun peekIsNewline () = peek().let { it=='\r'||it=='\n' }

	fun voreNewline (doRewind:Boolean=false): Boolean
	{
		if (doRewind)
		{
			rewind()
		}
		if (!peekIsNewline())
		{
			return false
		}
		val pev = peek()
		skip()
		if (peekIsNewline() && peek() != pev)
		{
			skip()
		}
		if (lineNumber++ >= Int.MAX_VALUE)
		{
			throw IllegalStateException("Chunk has too many lines")
		}
		return true
	}

	fun readString (delimiter:Char)
	{
		// includes the starting delimiter for error messages
		val stringBegin = tokens.size
		val sb = StringBuilder()
		while (peek() != delimiter)
		{
			when (val ch = read())
			{
				EOS, '\r', '\n' -> throw RuntimeException("Unfinished String")
				'\\' -> {
					when (val escCh = read())
					{
						'a' -> sb.append('\u0007')
						'b' -> sb.append('\u0008')
						'f' -> sb.append('\u000c')
						'n' -> sb.append('\u000a')
						'r' -> sb.append('\u000d')
						't' -> sb.append('\u0009')
						'v' -> sb.append('\u000b')
						'x' ->
						{
							val hi = read().hexToInt()
							val lo = read().hexToInt()
							sb.append(((hi shl 4) or lo).toChar())
						}
						'u' -> TODO("i dont feel like adding the utf8 esc stuff rn")

						'\r', '\n' -> sb.append('\n').also { voreNewline(true) }

						'\"', '\'', '\\' ->  sb.append(escCh)

						'z' -> {
							while (peek() in CONSIDERED_WHITESPACE)
							{
								if (peekIsNewline())
								{
									voreNewline()
								}
								else
								{
									skip()
								}
							}
						}
						else -> {
							check(peek().isDigit()) { "Invalid escape sequence" }
							TODO("dont feel like adding decimal esc seqs")
						}
					}
				}
				else -> sb.append(ch)
			}
		}

		tokenRanges += stringBegin..tokens.size
		// vore ending delimiter
		skip()
		tokens += Token.StringLiteral(sb.toString().also { println("string $it") })
	}

	fun voreNext (expect:Char): Boolean
	{
		skip()
		return vore(expect)
	}

	fun voreIfIn (set:Set<Char>): Boolean
	{
		if (peek() in set)
		{
			return true.also { skip() }
		}
		return false
	}

	fun voreWhile (cond:(Char)->Boolean):String
	{
		val start = tell()
		while (cond(peek()))
		{
			skip()
		}
		val count = tell()-start
		return if (count == 0) "" else text.substring(start, tell())
	}



	/**
	read a sequence '[=\*[' or ']=\*]', leaving the last bracket. If
	sequence is well formed, return its number of '='s + 2; otherwise,
	return 1 if it is a single bracket (no '='s and no 2nd bracket);
	otherwise (an unfinished '[==...') return 0.
	*/
	fun voreMultilineBrackets ():Int
	{
		check(peek().let { it=='[' || it == ']' })

		val delim = read()

		val count = skipWhile('=')

		return when
		{
			peek() == delim -> count + 2
			count == 0 -> 1
			else -> 0
		}
	}

	fun readMultilineString (spacings:Int, isComment: Boolean)
	{
		// second [
		skip()
		val sb = StringBuilder()
		val start = tell()
		val startLine = lineNumber
		while (true)
		{
			when (val ch = peek())
			{
				EOS -> {
					val kind = if (isComment) "comment" else "string"
					throw RuntimeException("unfinished multiline $kind (starts @ line $startLine")
				}
				']' -> {
					if (voreMultilineBrackets() == spacings)
					{
						// closing ]
						skip()
						tokens += if (isComment)
							Token.Comment()
						else
							Token.StringLiteral(sb.toString()).also { println("string ${it.body}") }
						tokenRanges += start..(tell()-spacings)
						return
					}
					sb.append(ch)
				}
				'\r', '\n' -> {
					if (!isComment)
						sb.append('\n')
					voreNewline()
				}
				else -> {
					if (!isComment)
						sb.append(ch)
					skip()
				}
			}
		}
	}

	fun lex ()
	{
		while (true)
		{
			when (val ch = peek())
			{
				'\n', '\r' -> voreNewline()
				' ', ESCF, '\t', ESCV -> skip()
				'-' -> {
					skip()
					if (!vore('-'))
					{
						tokens += Token.Symbol.DASH
						continue
					}
					if (peek() == '[')
					{
						// maybe multiline comment
						val count = voreMultilineBrackets()
						if (count >= 2)
						{
							readMultilineString(count, true)
							continue
						}
					}
					// line comment
					skipWhile { peekIsNewline() && it != EOS }
				}
				'[' -> {
					val count = voreMultilineBrackets()
					if (count >= 2)
					{
						readMultilineString(count, false)
						continue
					}
					if (count == 0)
					{
						throw RuntimeException("invalid multiline string delimiter")
					}
					tokens += Token.Symbol.LBRACKET
				}
				'=' -> {
					tokens += if (voreNext('='))
						Token.Symbol.DOUBLEEQ
					else
						Token.Symbol.EQ
				}
				'<' -> {
					skip()
					tokens += if (vore('='))
						Token.Symbol.LEQ
					else if (vore('<'))
						Token.Symbol.LSH
					else
						Token.Symbol.LT
				}
				'>' -> {
					skip()
					tokens += if (vore('='))
						Token.Symbol.GEQ
					else if (vore('>'))
						Token.Symbol.RSH
					else
						Token.Symbol.GT
				}
				'/' -> {
					tokens += if (voreNext('/'))
						Token.Symbol.DOUBLESLASH
					else
						Token.Symbol.SLASH
				}
				'~' -> {
					tokens += if (voreNext('='))
						Token.Symbol.NEQ
					else
						Token.Symbol.SQUIGGLE
				}
				':' -> {
					tokens += if (voreNext(':'))
						Token.Symbol.DOUBLECOLON
					else
						Token.Symbol.COLON
				}
				'\"', '\'' ->  readString(ch)
				'.' -> {
					skip()
					if (vore('.'))
					{
						if (vore('.'))
						{
							tokens += Token.Symbol.ELLIPSIS
							continue
						}
						tokens += Token.Symbol.CONCAT
						continue
					}
					// disallow leading-zeroless decimals
					tokens += Token.Symbol.DOT
				}
				in '0'..'9' -> {
					TODO("Numberz")
				}
				EOS -> tokens += Token.EndOfStream
				else -> {
					if (ch in OKAY_TO_START_IDENTIFIER)
					{
						val id = voreWhile { it in OKAY_TO_START_IDENTIFIER }
						tokens += if (id in Token.Keyword)
							Token.Keyword[id]
						else
							Token.Identifier(id)
						continue
					}
					// misc symbol
					tokens += Token.Symbol(ch)
				}
			}
		}
	}


}


const val TEST = "'This is a\\x20test\\r\\n string!\\\r\nyeah'"
val MULTILINE = """
	[==[abcdefg
	ahahahaWOW!!!!!!
	what?????
	]==]
""".trimIndent()

fun main ()
{
	val sr = LStringReader(MULTILINE)
	sr.skip(3)
	sr.readMultilineString(4, false)
//	println((sr.tokens.first() as? Token.StringLiteral)?.body)
//	val ub = Path("./assets/ubyte.txt").readText(Charsets.ISO_8859_1)
//	println(ub.escapeilize())
}


