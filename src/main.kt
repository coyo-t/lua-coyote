const val EOS = Char.MAX_VALUE

sealed class Token
{
	class Keyword private constructor (val kw: String): Token()
	{
		init { registered += kw }

		companion object
		{
			private val registered = mutableSetOf<String>()

			operator fun contains (s:String) = s in registered

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

	class StringLiteral (val body:String): Token()
	class Comment: Token()
	class IntLiteral (val value:Int): Token()
	class NumberLiteral (val value:Double): Token()
	class Identifier (val name:String):Token()
}






val CONSIDERED_WHITESPACE = setOf(
	'\u0009',
	'\u000a',
	'\u000b',
	'\u000c',
	'\u000d',
	'\u0020',
)

class LStringReader(text: String): StringReader(text)
{
	val tokens = mutableListOf<Token>()
	val tokenRanges = mutableListOf<IntRange>()

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


