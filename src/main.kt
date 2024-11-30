import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.properties.Delegates.observable


fun String.escapeilize ():String
{
	val outs = StringBuilder()
	val hxch = "0123456789ABCDEF"
	for (ch in this)
	{
		when (ch)
		{
			'\u0007' -> outs.append("\\a")
			'\u0008' -> outs.append("\\b")
			'\u000c' -> outs.append("\\f")
			'\u000a' -> outs.append("\\n")
			'\u000d' -> outs.append("\\r")
			'\u0009' -> outs.append("\\t")
			'\u000b' -> outs.append("\\v")
			'\"' -> outs.append("\\\"")
			'\'' -> outs.append("\\\'")
			'\\' -> outs.append("\\\\")
			else -> {
				if (ch in ' '..'~')
				{
					outs.append(ch)
				}
				else
				{
					val cp = ch.code
					outs.append("\\x")
					outs.append(hxch[(cp shr 4) and 0b00001111])
					outs.append(hxch[cp and 0b00001111])
				}
			}
		}
	}
	return outs.toString()
}


fun Char.hexToInt (): Int
{
	return when (this)
	{
		in '0'..'9' -> this - '0'
		in 'a'..'f' -> this - 'W'
		in 'A'..'F' -> this - '7'
		else -> throw IllegalArgumentException("Expecting a hex digit")
	}
}

const val EOS = Char.MAX_VALUE

val CONSIDERED_WHITESPACE = setOf(
	'\u0009',
	'\u000a',
	'\u000b',
	'\u000c',
	'\u000d',
	'\u0020',
)

open class StringReader(var text:String)
{
	var cursor by observable(0) { _, _, _ ->
		currDirty = true
	}

	var lineNumber = 1

	private var currDirty = true
	private var _cur = EOS
	private var _pev = EOS-1

	fun get (i:Int):Char
	{
		return text.getOrElse(i) { EOS }
	}

	fun skip () = cursor++
	fun rewind () = cursor--

	fun pev (): Char
	{
		return _pev
	}

	fun peek (): Char
	{
		if (currDirty)
		{
			_pev = _cur
			_cur = get(cursor)
			currDirty = false
		}
		return _cur
	}

	fun read () = get(cursor++)

	fun vore (expect:Char): Boolean
	{
		if (peek() == expect)
		{
			return true.also { skip() }
		}
		return false
	}

	fun skipWhile (eq:Char)
	{
		while (peek() == eq)
		{
			skip()
		}
	}

	inline fun skipWhile (fn:(Char)->Boolean)
	{
		while (fn(peek()))
		{
			skip()
		}
	}

}

enum class TKType
{
	KW_AND,
	KW_BREAK,
	KW_DO,
	KW_ELSE,
	KW_ELSEIF,
	KW_END,
	KW_FALSE,
	KW_FOR,
	KW_FUNCTION,
	KW_GOTO,
	KW_IF,
	KW_IN,
	KW_LOCAL,
	KW_NIL,
	KW_NOT,
	KW_OR,
	KW_REPEAT,
	KW_RETURN,
	KW_THEN,
	KW_TRUE,
	KW_UNTIL,
	KW_WHILE,

	`//`,
	`..`,
	`...`,
	`==`,
	`>=`,
	`<=`,
	`~=`,
	`<<`,
	`>>`,
	`::`,

	STRING_LITERAL,
	INT_LITERAL,
	IDENTIFIER,
	NUMBER_LITERAL,
}

class LStringReader(text: String): StringReader(text)
{
	val tokens = mutableListOf<Int>()
	val tokenRanges = mutableListOf<IntRange>()
	val tokenTypes = mutableListOf<TKType>()

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

	fun readString ()
	{
		// includes the starting delimiter for error messages
		val stringBegin = tokens.size
		val delimiter = read()
		tokens += delimiter.code
		tokenTypes += TKType.STRING_LITERAL
		while (peek() != delimiter)
		{
			when (val ch = read())
			{
				EOS, '\r', '\n' -> throw RuntimeException("Unfinished String")
				'\\' -> {
					when (val escCh = read())
					{
						'a' -> tokens += '\u0007'.code
						'b' -> tokens += '\u0008'.code
						'f' -> tokens += '\u000c'.code
						'n' -> tokens += '\u000a'.code
						'r' -> tokens += '\u000d'.code
						't' -> tokens += '\u0009'.code
						'v' -> tokens += '\u000b'.code
						'x' ->
						{
							val hi = read().hexToInt()
							val lo = read().hexToInt()
							tokens += (hi shl 4) or lo
						}
						'u' -> TODO("i dont feel like adding the utf8 esc stuff rn")

						'\r', '\n' -> tokens += '\n'.code.also { voreNewline(true) }

						'\"', '\'', '\\' ->  tokens += escCh.code

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
				else -> tokens += ch.code
			}
		}

		tokenRanges += stringBegin..tokens.size
		tokens += read().code // vore ending delimiter
	}

	fun readMultilineString ()
	{
		TODO()
	}


}


const val TEST = "'This is a\\x20test\\r\\n string!\\\r\nyeah'"


fun main ()
{
	val sr = LStringReader(TEST)

//	val ub = Path("./assets/ubyte.txt").readText(Charsets.ISO_8859_1)
//	println(ub.escapeilize())
}


