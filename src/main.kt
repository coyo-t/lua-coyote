import kotlin.properties.Delegates.observable


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
	private var curr = EOS

	fun get (i:Int):Char
	{
		if (currDirty)
		{
			curr = text.getOrElse(i) { EOS }
			currDirty = false
		}
		return curr
	}

	fun skip () = cursor++
	fun rewind () = cursor--

	fun peek () = get(cursor)

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



class LStringReader(text: String): StringReader(text)
{

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

	fun tryHexDigit () = read().hexToInt()

	fun readString (): String
	{
		val delimiter = read()
		val outs = StringBuilder()
		while (true)
		{
			when (val ch = read())
			{
				EOS, '\r', '\n' -> throw RuntimeException("Unfinished String")
				'\\' -> {
					when (val escCh = read())
					{
						'a' -> outs.append('\u0007')
						'b' -> outs.append('\u0008')
						'f' -> outs.append('\u000c')
						'n' -> outs.append('\u000a')
						'r' -> outs.append('\u000d')
						't' -> outs.append('\u0009')
						'v' -> outs.append('\u000b')
						'x' -> outs.append(run {
							val hi = tryHexDigit()
							val lo = tryHexDigit()
							((hi shl 4) or lo).toChar()
						})
						'u' -> TODO("i dont feel like adding all the utf8 esc stuff rn")

						'\r', '\n' -> outs.append('\n').also { voreNewline(true) }

						'\"', '\'', '\\' ->  outs.append(escCh)

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
			}
		}
	}

}


const val TEST = """'This is a test\\n string!'"""

fun main ()
{
	val sr = LStringReader(TEST)
//	println(sr.readString('\''))
}


