import tokenize.LStringReader
import tokenize.Token
import kotlin.io.path.Path
import kotlin.io.path.readText


fun main ()
{
	val sr = LStringReader(
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
	val tokens = mutableListOf<Token>()
	while (true)
	{
		tell = sr.tell()
		line = sr.currentLineNumber()
		val tk = sr.lex()

		tokens += tk

		var ads = ""


		when (tk)
		{
			is Token.EndOfStream -> break
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


