
val DEC_NUMBERS = "0123456789".toSet()
val HEX_NUMBERS = DEC_NUMBERS + "abcdefABCDEF".toSet()

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

class StringReader
{

	operator fun StringBuilder.plusAssign (e:Char)
	{
		append(e)
	}

	var text = ""
	var cursor = 0

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
						'x' -> {

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


