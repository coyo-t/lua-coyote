import tokenize.LTokenizer
import tokenize.SemanticsInfo
import tokenize.TK
import tokenize.Token
import kotlin.io.path.Path


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

	var tell = 0
	var line = 0
	val tokens = mutableListOf<TK>()
	val tokenInfo = mutableListOf<SemanticsInfo?>()
	while (true)
	{
		tell = sr.tell()
		line = sr.currentLineNumber()
		val tk = sr.lex()

		tokens += tk
		tokenInfo += sr.popSemantics()

		var ads = ""


		when (tk)
		{
			in TK.EOS -> break
//			is Token.StringLiteral -> {
//
//				println("tokenize.Token: ${tk}")
//				println()
////				println("tokenize.Token: ${String(data, Charsets.UTF_8)}")
//			}
			else -> {
				println("$ads${line..sr.currentLineNumber()} $tk")

			}
		}
	}
	println("Endzor")
}


