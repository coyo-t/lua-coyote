@JvmInline
value class CharAttribute(val bits:Int)
{
	val isAlpha get () = ALPHABIT in bits
	val isAlNum get () = ALNUM in bits
	val isDigit get () = DIGITBIT in bits
	val isSpace get () = SPACEBIT in bits
	val isPrintable get () = PRINTBIT in bits
	val isHexDigit  get () = XDIGITBIT in bits

	companion object
	{
		private var bp = 0
		private fun flag () = 1 shl (bp++)

		private operator fun Int.contains (bit:Int)
			= (this and bit) != 0

		private val ALPHABIT = flag()
		private val DIGITBIT = flag()
		private val PRINTBIT = flag()
		private val SPACEBIT = flag()
		private val XDIGITBIT = flag()
		private val ALNUM = ALPHABIT or DIGITBIT

		val Non = CharAttribute(0)

		operator fun invoke (ch:Char?) = ch?.let { TABLE.getOrNull(ch.code) } ?: Non
	}
}

//* accept (U)ni(C)ode (ID)entifiers?
const val LUA_UCID = 0
val NONA = when (LUA_UCID) {
	// consider all non-ascii codepoints to be alphabetic
	1 -> 0x01

	// default
	else -> 0x00
}

val TABLE = intArrayOf(
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 00
	0x00, 0x08, 0x08, 0x08, 0x08, 0x08, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 10
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x0c, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, // 20
	0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
	0x16, 0x16, 0x16, 0x16, 0x16, 0x16, 0x16, 0x16, // 30
	0x16, 0x16, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
	0x04, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x05, // 40
	0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
	0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, // 50
	0x05, 0x05, 0x05, 0x04, 0x04, 0x04, 0x04, 0x05,
	0x04, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x05, // 60
	0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
	0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, // 70
	0x05, 0x05, 0x05, 0x04, 0x04, 0x04, 0x04, 0x00,
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA, // 80
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA, // 90
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA, // a0
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA, // b0
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	0x00, 0x00, NONA, NONA, NONA, NONA, NONA, NONA, // c0
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA, // d0
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA, // e0
	NONA, NONA, NONA, NONA, NONA, NONA, NONA, NONA,
	NONA, NONA, NONA, NONA, NONA, 0x00, 0x00, 0x00, // f0
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
).map(::CharAttribute)

