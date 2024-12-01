import kotlin.math.pow


class LStringReader(text: String): StringReader(text)
{
	val tokens = mutableListOf<Token>()
	val tokenRanges = mutableListOf<IntRange>()
	val tokenLineNumbers = mutableListOf<IntRange>()

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
		if (_lineNumber++ >= Int.MAX_VALUE)
		{
			throw IllegalStateException("Chunk has too many lines")
		}
		return true
	}

	fun readString (delimiter:Char): Token
	{
		// includes the starting delimiter for error messages
		val stringBegin = tokens.size
		val lineBegin = _lineNumber
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

						// hex bytearray, formatted as \${...}
						// similar to long utf8 escape sequences being \u{...}
						// should technically outmode the need for \# but
						// people probably already have base64 strings and dont
						// want to decode them manually
						'$' -> TODO("inline bytearray data")

						// base64 data escape, formatted as \#{...}
						// similar to long utf8 escape sequences being \u{...}
						'#' -> TODO("inline Base64 data")

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
						' ' -> {
							// non escape sequence.
							// this does nothing and is for weird cases
							// like a short utf8 that needs to be followed
							// by a number, \u20\ 00 -> " 00"
							// could also possibly be to make strings more readable
							// in niche cases
						}
						else -> {
							check(peek().isDigit()) { "Invalid escape sequence" }

							rewind()
							var acc = 0
							for (i in 1..3)
							{
								if (!peek().isDigit())
								{
									break
								}
								acc = acc * 10 + (read() - '0')
							}
							check (acc <= UByte.MAX_VALUE.toInt()) { "Decimal escape sequence value of $acc too large" }
							sb.append(acc.toChar())
						}
					}
				}
				else -> sb.append(ch)
			}
		}

		tokenRanges += stringBegin..tokens.size
		tokenLineNumbers += lineBegin.._lineNumber
		// vore ending delimiter
		skip()
		return Token.StringLiteral(sb.toString())//.also { println("string $it") })
	}

	fun voreNext (expect:Char): Boolean
	{
		skip()
		return vore(expect)
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

	fun addToken (tk:Token)
	{
		tokens += tk
		tokenLineNumbers += _lineNumber.._lineNumber
		// i dont feel like managing this part rn so its
		// lazy and not really working
		tokenRanges += _mark..cursor
	}

	infix fun MutableList<Token>.addzor (tk:Token)
		= addToken(tk)

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

	fun readMultilineString (spacings:Int, isComment: Boolean):Token?
	{
		// second [
		skip()
		val sb = StringBuilder()
		val start = tell()
		val startLine = _lineNumber
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
						if (!isComment)
						{
							tokenRanges += start..(tell()-spacings)
							tokenLineNumbers += startLine.._lineNumber
							return Token.StringLiteral(sb.toString())
						}
						return null
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

	fun throwMalformedNumber (): Nothing
	{
		throw RuntimeException("Malformed number literal")
	}

	fun readHexLiteralBody(): Token
	{
		// TODO: hex fractions, ie 0x100.292FE
		var acc = 0L
		while (peek() in HEXIDECIMAL_SYMBOLS)
		{
			val ch = read()
			if (ch != '_')
			{
				acc = (acc shl 4) or ch.hexToInt().toLong()
			}
		}
		return Token.IntLiteral(acc.toInt())
	}

	fun readBinaryLiteralBody(): Token
	{
		// TODO: binary fractions, ie 0b110.101
		var acc = 0L
		while (peek() in BINARY_DIGITS)
		{
			val ch = read()
			if (ch != '_')
			{
				acc = acc shl 1
				if (ch == '1')
				{
					acc += 1
				}
			}
		}
		return Token.IntLiteral(acc.toInt())
	}

	fun readOctalLiteralBody(): Token
	{
		// TODO: octal fractions, ie 0o712.113
		var acc = 0L
		while (peek() in OCTAL_DIGITS)
		{
			val ch = read()
			if (ch != '_')
			{
				acc = (acc shl 3) + (ch - '0')
			}
		}
		return Token.IntLiteral(acc.toInt())
	}

	fun readDegreesLiteralBody(): Token
	{
		var acc = 0L
		var wholePart = 0L
		var fractPlaces = -1
		var hasFrac = false
		while (peek() in FLOAT_DIGITS)
		{
			if (vore('.'))
			{
				if (hasFrac)
				{
					break
				}
				fractPlaces = tell()
				hasFrac = true
				wholePart = acc
				acc = 0L
				continue
			}
			val ch = read()
			if (ch != '_')
			{
				acc = (acc * 10) + (ch - '0')
			}
			else
			{
				fractPlaces++
			}
		}

		var number = (if (hasFrac) wholePart else acc).toDouble()
		if (hasFrac)
		{
			number = number + acc / 10.0.pow(tell() - fractPlaces)
		}
		return Token.NumberLiteral(Math.toRadians(number))
	}

	fun readNumericLiteral (): Token
	{
		if (peek() == '0')
		{
			skip()
			if (voreIn("xX"))
			{
				return readHexLiteralBody()
			}
			else if (voreIn("bB"))
			{
				return readBinaryLiteralBody()
			}
			else if (voreIn("oO"))
			{
				return readOctalLiteralBody()
			}
			else if (voreIn("dD"))
			{
				return readDegreesLiteralBody()
			}
			rewind()
		}

		var intAcc = 0L
		var hasExponent = false
		var exponent = 0
		var exponentSign = +1

		var hasFractionDot = false
		var fraction = 0L
		var fractionPlaces = -1
		while (true)
		{
			if (vore('_'))
			{
				// ignore separator
				// can probably result in weird edge
				// cases that i dont feel like finding rn
				fractionPlaces++
				continue
			}
			// exponent notation
			if (voreIn("eE"))
			{
				hasExponent = true
				if (vore('-'))
				{
					exponentSign = -1
				}
				else if (vore('+'))
				{
					// nothing, sign is already +1
				}
				else if (!peek().isDigit())
				{
					throwMalformedNumber()
				}
				while (peek().let { it.isDigit() || it == '_' })
				{
					val ch = read()
					if (ch != '_')
					{
						exponent = exponent * 10 + (ch - '0')
					}
				}
			}
			if (vore('.'))
			{
				if (hasFractionDot)
				{
					break
				}
				fractionPlaces = tell()
				hasFractionDot = true
				continue
			}
			val ch = peek()
			if (!ch.isDigit())
			{
				break
			}
			val digit = (ch - '0').toInt()
			skip()
			if (hasFractionDot)
			{
				fraction = fraction * 10 + digit
			}
			else
			{
				intAcc = intAcc * 10 + digit
			}
		}

		var numAcc = 0.0
		var intWasMadeNumber = false
		if (hasExponent)
		{
			if (exponentSign > 0)
			{
				intAcc = intAcc * ((10.0).pow(exponent)).toInt()
			}
			else
			{
				numAcc = intAcc.toDouble() * (10.0).pow(exponent*exponentSign)
				intWasMadeNumber = true
			}
		}

		if (!hasFractionDot && !intWasMadeNumber)
		{
//			if (intAcc !in Int.MIN_VALUE..Int.MAX_VALUE)
//			{
//				throw RuntimeException("Int literal doesnt fit!")
//			}
			return Token.IntLiteral(intAcc.toInt())
		}
		if (!intWasMadeNumber)
		{
			numAcc = intAcc.toDouble()
		}
		if (hasFractionDot)
		{
			numAcc += fraction.toDouble() / 10.0.pow(tell()-fractionPlaces)
		}
		return Token.NumberLiteral(numAcc)
	}

	fun handleDot (): Token
	{
		if (vore('.'))
		{
			if (vore('.'))
			{
				return Token.Symbol.ELLIPSIS
			}
			return Token.Symbol.CONCAT
		}
		// disallow leading-zeroless decimals
		return Token.Symbol.DOT
	}


	fun lex (): Token?
	{
		while (true)
		{
			when (val ch = peek())
			{
				'\n', '\r' -> voreNewline()
				' ', ESCF, '\t', ESCV -> skip()
				'-' -> {
					markSkip()
					if (!vore('-'))
					{
						return Token.Symbol.DASH
					}
					if (peek() == '[')
					{
						// maybe multiline comment
						val count = voreMultilineBrackets()
						if (count >= 2)
						{
							readMultilineString(count, true)
							return null
						}
					}
					// line comment
					skipWhile { !peekIsNewline() && it != EOS }
					return null
				}
				'[' -> {
					val count = voreMultilineBrackets()
					if (count >= 2)
					{
						return readMultilineString(count, false)
					}
					if (count == 0)
					{
						throw RuntimeException("invalid multiline string delimiter")
					}
					return Token.Symbol.LBRACKET
				}
				'=' -> {
					skip()
					return if (vore('='))
						Token.Symbol.DOUBLEEQ
					else
						Token.Symbol.EQ
				}
				'<' -> {
					skip()
					return if (vore('='))
						Token.Symbol.LEQ
					else if (vore('<'))
						Token.Symbol.LSH
					else
						Token.Symbol.LT
				}
				'>' -> {
					skip()
					return if (vore('='))
						Token.Symbol.GEQ
					else if (vore('>'))
						Token.Symbol.RSH
					else
						Token.Symbol.GT
				}
				'/' -> {
					skip()
					return if (vore('/'))
						Token.Symbol.DOUBLESLASH
					else
						Token.Symbol.SLASH
				}
				'~' -> {
					skip()
					return if (vore('='))
						Token.Symbol.NEQ
					else
						Token.Symbol.SQUIGGLE
				}
				':' -> {
					skip()
					return if (vore(':'))
						Token.Symbol.DOUBLECOLON
					else
						Token.Symbol.COLON
				}
				'\"', '\'' -> {
					skip()
					return readString(ch)
				}
				'.' -> {
					skip()
					return handleDot()
				}
				in '0'..'9' -> return readNumericLiteral()

				EOS -> {
					return Token.EndOfStream
				}
				else -> {
					if (ch in OKAY_TO_START_IDENTIFIER)
					{
						val id = voreWhile { it in IDENTIFIER_SYMBOLS }
						return if (id in Token.Keyword)
							Token.Keyword[id]
						else
							Token.Identifier(id)
					}
					// misc symbol
					return Token.Symbol(ch)
				}
			}
		}
	}


	companion object
	{
		// https://en.cppreference.com/w/c/language/escape
		private const val ESCF = '\u000c'
		private const val ESCV = '\u000b'

		private val UNDERSCORE = setOf('_')

		val ALPHABETICAL_SYMBOLS = (
			('a'..'z').toSet() +
			('A'..'Z').toSet()
		)
		private val DIGIT_SYMBOLS = ('0'..'9').toSet()
		private val FLOAT_DIGITS = DIGIT_SYMBOLS + setOf('.')

		private val OKAY_TO_START_IDENTIFIER = ALPHABETICAL_SYMBOLS + UNDERSCORE

		private val IDENTIFIER_SYMBOLS = OKAY_TO_START_IDENTIFIER + DIGIT_SYMBOLS
		private val HEXIDECIMAL_SYMBOLS = ALPHABETICAL_SYMBOLS + UNDERSCORE + DIGIT_SYMBOLS

		private val BINARY_DIGITS = ("01_").toSet()
		private val OCTAL_DIGITS = ('0'..'7').toSet() + UNDERSCORE

		private val CONSIDERED_WHITESPACE = setOf(
			'\u0009', // t
			'\u000a', // n
			'\u000b', // v
			'\u000c', // f
			'\u000d', // r
			'\u0020', // space
		)
	}
}