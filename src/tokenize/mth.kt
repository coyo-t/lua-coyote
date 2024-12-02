package tokenize
fun Char.base64ToIntOrThrow (): Int
{

	val outs = base64ToInt()
	if (outs < 0)
	{
		throw IllegalArgumentException("Not a base64 digit")
	}
	return outs
}

fun Char.base64ToInt (): Int
{
	return when (this)
	{
		in 'A'..'Z' -> this - 'A'
		in 'a'..'z' -> this - 'G'
		in '0'..'9' -> this.code + 4
		'+' -> 0b00_111110
		'/' -> 0b00_111111
		else -> -1
	}
}

fun Char.hexToIntOrThrow (): Int
{

	val outs = hexToInt()
	if (outs < 0)
	{
		throw IllegalArgumentException("Not a hex digit")
	}
	return outs
}

fun Char.hexToInt (): Int
{
	return when (this)
	{
		in '0'..'9' -> this - '0'
		in 'a'..'f' -> this - 'W'
		in 'A'..'F' -> this - '7'
		else -> -1
	}
}


