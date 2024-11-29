


fun Char?.hexToInt (): Int
{
	return when (this)
	{
		null -> throw IllegalArgumentException("Expecting a hex digit")
		in '0'..'9' -> this - '0'
		in 'a'..'f' -> this - 'W'
		in 'A'..'F' -> this - '7'
		else -> throw IllegalArgumentException("Expecting a hex digit")
	}
}

class StringReader
{
	operator fun StringBuilder.plusAssign (e:Char)
	{
		append(e)
	}



	var text = ""
	var cursor = 0
	var lineNumber = 1
	var current: Char? = null


	fun next ()
	{
		current = text.getOrNull(cursor++)
	}

	val curIsNewline get() = current == '\n' || current == '\r'

	fun setInput (source:String)
	{
		text = source
		current = source.getOrNull(0)
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
		val outs = StringBuilder()
		while (current != delimiter)
		{
			when (val ch = current)
			{
				null -> throw IllegalStateException("unfinished string (eos)")
				'\r', '\n' -> throw IllegalStateException("unfinished string (newline)")
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
							val spaces = "\r\n ".toSet()

						}
					}
				}
			}
		}
		TODO()
	}
}




fun main ()
{

}


