import tokenize.LTokenizer
import tokenize.SemanticsInfo
import tokenize.TK
import kotlin.io.path.Path


class LexerState
{
	var current: TK = TK.NULL
	var lookahead: TK = TK.NULL
	val tokens = mutableListOf<TK>()
	val tokenInfo = mutableListOf<SemanticsInfo?>()
}

fun main ()
{
	val sr = LTokenizer(
		Path(
		"./assets/testfiles",
//		"octalnumbers"
		"multiline"
//		"bytearrayescapeseq"
//		"base64esc"
//		"unicodeesc"
		)
	)

	val lex = LexerState()

	with (lex)
	{
		var tell = 0
		var line = 0
		while (true)
		{
			sr.skipToNext()
			tell = sr.tell()
			line = sr.currentLineNumber()
			current = sr.lex()

			tokens += current
			tokenInfo += sr.popSemantics()

			var ads = ""

			when (current)
			{
				in TK.EOS -> break
				else -> {
					println("$ads${line..sr.currentLineNumber()} $current")

				}
			}
		}
		println("Endzor")
	}

}


