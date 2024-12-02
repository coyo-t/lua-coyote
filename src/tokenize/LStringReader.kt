package tokenize

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import kotlin.math.pow


class LStringReader(path: Path): StringReader(path)
{
	val tokens = mutableListOf<Token>()
	val tokenRanges = mutableListOf<IntRange>()
	val tokenLineNumbers = mutableListOf<IntRange>()

	private var tempBuffer = ByteBuffer.allocate(128)

	private fun tempClear ()
	{
		tempBuffer.clear()
	}

	private fun tempPutChar (v:Char)
	{
		tempPutInt(v.code)
	}

	private fun tempPutInt (i:Int)
	{
		tempPutByte((i and 0xFF).toByte())
	}

	private fun tempPutByte (v: Byte)
	{
		with (tempBuffer)
		{
			val cap = capacity()
			val pos = position()
			if (pos+1 > cap)
			{
				val news = ByteBuffer.allocateDirect(cap + (cap ushr 1))
				news.put(flip()).position(pos)
				tempBuffer = news
			}
		}
		with (tempBuffer)
		{
			put(v)
		}
	}

	private fun tempCreateStringToken (): Token
	{
		with (ByteBuffer.allocateDirect(tempBuffer.flip().limit()))
		{
			put(tempBuffer)
			return Token.StringLiteral(flip())
		}
	}

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

	fun doUtf8EscSequenceThing()
	{
		//							TODO("not working right")
		if (vore('{'))
		{
			TODO("long unicode escape sequences not implemented")
		}
		// require at least one digit
		var FUCKCH = read()
		var acc = FUCKCH.hexToIntOrThrow()
		// up to 4
		for (i in 1..3)
		{
			val ch = peek().hexToInt()
			if (ch < 0)
			{
				break
			}
			skip()
			acc = (acc shl 4) or ch
		}

		if (acc < 0x80)
		{
			// already an ascii value, just add it to the
			// stringbuilder
			tempPutInt(acc)
		}
		else
		{
			// 8 is overkill, longest utf8 encoded value tmk is 4 bytes
			val outs = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())

			// number of bytes put (backwards) into buffer
			var n = 1
			var mfb = 0x3F
			do
			{
				// add continuator bytes
				outs.put(
					outs.capacity() - (n++),
					(0x80 or (acc and 0x3F)).toByte()
				)
				// remove added bits
				acc = acc ushr 6
				// now there's one less bit available in first byte
				mfb = mfb ushr 1
				// vv still needs continue byte?
			} while (acc > mfb)
			// add first byte
			outs.put(
				outs.capacity() - n,
				((mfb.inv() shl 1) or acc).toByte()
			)
			outs.flip()
			(1..outs.limit()).forEach {
				tempPutInt(outs.get().toInt() and 0xFF)
			}
		}
	}

	fun doByteArrayEscSequenceThing()
	{
		// hex bytearray, formatted as \${...}
		// similar to long utf8 escape sequences being \u{...}
		// should technically outmode the need for \# but
		// people probably already have base64 strings and dont
		// want to decode them manually
		if (!vore('{'))
		{
			throw RuntimeException("Expected a {")
		}
		if (vore('}'))
		{
			// short circuit
			return
		}
		// hex data literally cant be larger than half the string's
		// length anyway (as one byte in text is
		// represented with two bytes). it would be
		// better to count the bytes first, but this
		// is "fine" as a solution for a niche escape sequence
		val outs = ByteBuffer.allocateDirect(size)

		var working = 0
		var even = false
		while (true)
		{
			if (vore(EOS))
			{
				throw RuntimeException("Unfinished hex data literal")
			}
			if (vore('}'))
			{
				break
			}
			val ch = read()
			if (ch == '\r' || ch == '\n')
			{
				voreNewline(true)
				continue
			}
			if (ch !in HEXIDECIMAL_SYMBOLS)
			{
				continue
			}
			working = (working shl 4) or ch.hexToInt()
			if (even)
			{
				outs.put(working.toByte())
			}
			even = !even
		}
		// esc data was unevenly balanced, IE
		// \${FF FF F}
		// there are a few ways to handle this, but in this case
		// raise an error to the user as this might be a copy-paste
		// error
		check(!even) { "Hex data literal uneven" }

		// TODO: need bulk put op
		for (i in 0..<outs.flip().limit())
		{
			tempPutByte(outs.get(i))
		}
		//							val ob = ByteArray(outs.limit()).apply { outs.get(this) }
		//							sb.append(String(ob, Charsets.ISO_8859_1))
	}

	fun doBase64EscapeSequenceThing()
	{
		// base64 data escape, formatted as \#{...}
		// similar to long utf8 escape sequences being \u{...}
		if (!vore('{'))
		{
			throw RuntimeException("Expected a {")
		}
		if (vore('}'))
		{
			// short circuit
			return
		}
		// the data the base64 string encodes literally
		// cant be longer than the string itself so this
		// is a lazy approximation. would be better to count
		// the characters first but whatever
		// maybe the lexer should have a secondary "scratch"
		// buffer the same length of the string
		val outs = ByteBuffer.allocateDirect(size)

		var bits = 0
		var working = 0
		while (true)
		{
			if (vore(EOS))
			{
				throw RuntimeException("Unfinished base64 literal")
			}
			if (vore('}'))
			{
				break
			}
			val ch = read()
			if (ch == '\r' || ch == '\n')
			{
				voreNewline(true)
				continue
			}
			if (ch !in BASE64_DIGITS)
			{
				continue
			}
			working = working shl 6
			if (ch != '=')
			{
				working = working or ch.base64ToInt()
			}
			bits += 6
			if (bits >= 24)
			{
				with(outs)
				{
					put((working ushr 16).toByte())
					put((working ushr 8).toByte())
					put(working.toByte())
				}
				bits = 0
			}
		}
		// esc data wasnt padded with ='s
		// while the padding isnt nessicary, im making it
		// required anyway. bite me.
		// raise an error to the user as this might be a copy-paste
		// error
		check(bits == 0) { "Base64 data not padded" }

		// TODO: need bulk put op
		for (i in 0..<outs.flip().limit())
		{
			tempPutByte(outs.get(i))
		}

		//							outs.flip()
		//							val ob = ByteArray(outs.limit()).apply { outs.get(this) }
		//							sb.append(String(ob, Charsets.ISO_8859_1))
	}

	fun doEmptyEscapeSequenceThing()
	{
		// non escape sequence.
		// this does nothing and is for weird cases
		// like a short utf8 that needs to be followed
		// by a number, \u20\ 00 -> " 00"
		// could also possibly be to make strings more readable
		// in niche cases
	}

	fun doZapEscapeSequenceThing()
	{
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

	fun doDecimalEscapeSequenceThing()
	{
		tempPutInt(
			(1..3)
			.map { peek() }
			.takeWhile(Char::isDigit)
			.fold(0) { acc, ch -> acc*10+(ch-'0').also { skip() } }
			.apply {
				check(this <= UByte.MAX_VALUE.toInt()) {
					"Decimal escape sequence value of $this too large"
				}
			}
		)
	}

	fun stringHandleEscapeSequence()
	{
		when (val escCh = read())
		{
			'a' -> tempPutChar('\u0007')
			'b' -> tempPutChar('\u0008')
			'f' -> tempPutChar('\u000c')
			'n' -> tempPutChar('\u000a')
			'r' -> tempPutChar('\u000d')
			't' -> tempPutChar('\u0009')
			'v' -> tempPutChar('\u000b')
			'x' ->
			{
				val hi = read().hexToIntOrThrow()
				val lo = read().hexToIntOrThrow()
				tempPutInt((hi shl 4) or lo)
			}

			'u' -> doUtf8EscSequenceThing()
			'$' -> doByteArrayEscSequenceThing()
			'#' -> doBase64EscapeSequenceThing()


			'\r', '\n' -> tempPutChar('\n').also { voreNewline(true) }

			'\"', '\'', '\\' -> tempPutChar(escCh)

			'z' -> doZapEscapeSequenceThing()
			' ' -> doEmptyEscapeSequenceThing()
			else ->
			{
				check(peek().isDigit()) { "Invalid escape sequence" }
				rewind()
				doDecimalEscapeSequenceThing()
			}
		}
	}

	fun readString (delimiter:Char): Token
	{
		// includes the starting delimiter for error messages
		val stringBegin = tokens.size
		val lineBegin = _lineNumber
		tempClear()
		while (peek() != delimiter)
		{
			when (val ch = read())
			{
				EOS, '\r', '\n' -> throw RuntimeException("Unfinished String")
				'\\' -> stringHandleEscapeSequence()
				else -> tempPutChar(ch)
			}
		}

		tokenRanges += stringBegin..tokens.size
		tokenLineNumbers += lineBegin.._lineNumber
		// vore ending delimiter
		skip()
//		return Token.StringLiteral(sb.toString())
		return tempCreateStringToken()
	}

	fun voreWhile (cond:(Char)->Boolean):String
	{
		val start = tell()
		while (cond(peek()))
		{
			skip()
		}
		val count = tell()-start
		return if (count == 0) "" else run {
			val outs = ByteArray(count)
			val p = data.position()
			data.get(start, outs, 0, count)
			data.position(p)
			String(outs, Charsets.ISO_8859_1)
		}
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

	fun readMultilineString (spacings:Int, isComment: Boolean):Token?
	{
		// second [
		skip()
		tempClear()
//		val sb = StringBuilder()
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
							return tempCreateStringToken()
						}
						return null
					}
					tempPutChar(ch)
				}
				'\r', '\n' -> {
					if (!isComment)
						tempPutChar('\n')
					voreNewline()
				}
				else -> {
					if (!isComment)
						tempPutChar(ch)
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
				acc = (acc shl 4) or ch.hexToIntOrThrow().toLong()
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


	fun lex (): Token
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
						return Token.Symbol.DASH
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
					// mismatching line endings will get caught next loop
					skipWhile { !peekIsNewline() && it != EOS }
					continue
				}
				'[' -> {
					val count = voreMultilineBrackets()
					if (count >= 2)
					{
						return readMultilineString(count, false)!!
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

		private val BASE64_DIGITS = (
			ALPHABETICAL_SYMBOLS + DIGIT_SYMBOLS + setOf('=', '/', '+')
		)

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