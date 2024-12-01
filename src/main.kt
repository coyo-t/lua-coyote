
const val TEST = "'This is a\\x20test\\r\\n string!\\\r\nyeah'"
val MULTILINE = """
local test = "abcdeeznuts"
local a = [[
	Yeah what about it??
]]
local b = [[
	this is a pretty long string eh?
	uuhhhhhhh.
	yeah.
	probably.
	haha, WOW!!!!!!
]]
local c = 'this is a \
weirdo string that\
continues itself with whacks'
local dzz = 'this\32string\32has\32no\32spaces\32lol'

if a == test then
	print'Oigh jeez this guy agian'
end
-- this is a COMMENT!!!!!!
do
	thisis = notacomment
end
--[===[ this is
	a SUPER
	-- [[
		sorry 2 interrupt :]
	]]
comment
]===]
""".trimIndent()


val NUMBERSTEST = """
	local int_simple = 292202
	local int_exp = 1e6
	local int_exp_num = 1e-6
	local int_sep = 123_456_789
	
	local num_simple_1 = 292.202
	local num_simple_2 = 3.141
	local num_no_end = 4.
	local num_sep = 123_456.789
	local num_sep_weird_1 = 123._456
	local num_sep_weird_2 = 123_.456
	local num_sep_weird_3 = 123_.456
	local num_sep_weird_4 = 123.456_
	local num_sep_weird_6 = 1.0E-1_0
	local num_sep_weird_5 = 1__2__3_____.______45_6___
""".trimIndent()

val NUMBER_HEX_TESTS = """
	a = 0x20
	b = 0x292202
	c = 0xFF
	d = 0xFF_FF
	e = 0xFF_FFFF
	f = 0xFFFF_FFFF
""".trimIndent()

val NUMBER_BIN_TESTS = """
	a = 0b1111
	b = 0b00_00
	c = 0b0001
""".trimIndent()

val NUMBER_OCTAL_TESTS = """
	a = 0o0
	a = 0o0_7
	b = 0O10
	c = 0o20
	d = 0o30
""".trimIndent()

val NUMBER_DEGREES_TESTS = """
	a = 0d0.0
	a = 0d11.25
	a = 0d22.5
	a = 0d45
	a = 0d90
	a = 0d180
	a = 0d360
""".trimIndent()

fun main ()
{
	val sr = LStringReader(
//		MULTILINE
//		NUMBERSTEST
//		NUMBER_HEX_TESTS
//		NUMBER_BIN_TESTS
//		NUMBER_OCTAL_TESTS
		NUMBER_DEGREES_TESTS
	)
	sr.lex()

	var just = false
	for ((i, tk) in sr.tokens.withIndex())
	{
		var ads = ""
		if (tk is Token.NumberLiteral || tk is Token.IntLiteral)
		{
			ads = "\t"
		}
		just = false
		println("$ads${sr.tokenLineNumbers[i]} $tk")
	}

//	sr.skip(3)
//	sr.readMultilineString(4, false)
}


