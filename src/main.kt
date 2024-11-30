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

class StringReader(var text:String)
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
			skip()
			return true
		}
		return false
	}

	fun peekIsNewline () = peek() == '\r' || peek() == '\n'

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
							while (peek().isWhitespace())
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


class LStringReader
{
	operator fun StringBuilder.plusAssign (e:Char)
	{
		append(e)
	}



	var text = ""
	var cursor = 1
	var lineNumber = 1
	var current = EOS


	fun next ()
	{
		current = text.getOrElse(cursor++) { EOS }
	}

	val curIsNewline get() = current == '\n' || current == '\r'

	fun setInput (source:String)
	{
		text = source
		current = source.getOrElse(0) { EOS }
		cursor = 1
	}

	fun checkNext1 (ch:Char): Boolean
	{
		if (ch == current)
		{
			next()
			return true
		}
		return false
	}

	fun incrLineNumber ()
	{
		check (curIsNewline)
		val pev = current
		next()
		if (curIsNewline && current != pev)
		{
			next()
		}
		if (lineNumber++ >= Int.MAX_VALUE)
		{
			throw IllegalStateException("Chunk has too many lines")
		}
	}

	fun readString (delimiter:Char): String
	{
		next()
		val outs = StringBuilder()
		while (current != delimiter)
		{
			when (val ch = current)
			{
				EOS, '\r', '\n' -> throw IllegalStateException("unfinished string")
				'\\' -> {
					next()
					when (val escCh = current)
					{
						'a' -> outs += '\u0007'
						'b' -> outs += '\u0008'
						'f' -> outs += '\u000c'
						'n' -> outs += '\u000a'
						'r' -> outs += '\u000d'
						't' -> outs += '\u0009'
						'v' -> outs += '\u000b'
						'x' -> outs += run {
							val hi = escCh.hexToInt().also { next() }
							val lo = current.hexToInt().also { next() }
							((hi shl 4) or lo).toChar()
						}
						'u' -> TODO("i dont feel like adding all the utf8 esc stuff rn")

						'r', 'n' -> outs += '\n'.also { incrLineNumber() }

						'\"', '\'', '\\' ->  outs += ch

						'z' -> {
							next() // skip z
							while (CharAttribute(current).isSpace)
							{
								if (curIsNewline)
								{
									incrLineNumber()
								}
								else
								{
									next()
								}
							}
						}
						else -> {
							check(CharAttribute(cursor).isDigit) { "Invalid escape sequence" }
							TODO("dont feel like adding decimal esc seqs")
						}
					}
				}
				else -> outs += ch.also { next() }
			}
		}
		return outs.toString()
	}
}


const val TEST = """'This is a test\\n string!'"""

fun main ()
{
	val sr = LStringReader()
	sr.setInput(TEST)
	println(sr.readString('\''))
}


