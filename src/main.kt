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
//		"multiline"
//		"bytearrayescapeseq"
			"base64esc"
//		"unicodeesc"
		).readText(Charsets.ISO_8859_1)
	)

	var tell = 0
	var line = 0
	val tokens = mutableListOf<Token>()
	while (true)
	{
		tell = sr.tell()
		line = sr.currentLineNumber()
		val tk = sr.lex()
		if (tk == null)
		{
			continue
		}

		tokens += tk

		var ads = ""

//		println("$ads${line..sr.currentLineNumber()} $tk")

		when (tk)
		{
			is Token.EndOfStream -> break
			is Token.StringLiteral -> {
				val data = tk.body.encodeToByteArray()
				println("tokenize.Token: ${tk.body}")
//				println("tokenize.Token: ${String(data, Charsets.UTF_8)}")
			}
			else -> {}
		}
	}
	println("Endzor")
}


