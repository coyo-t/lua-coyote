import kotlin.io.path.Path
import kotlin.io.path.readText


fun main ()
{
	val sr = LStringReader(Path(
		"./assets/testfiles",
//		"octalnumbers"
		"multiline"
	).readText(Charsets.ISO_8859_1))

	var tell = 0
	var line = 0
	while (true)
	{
		tell = sr.tell()
		line = sr.currentLineNumber()
		val tk = sr.lex()
		if (tk == null)
		{
			continue
		}

//		sr.addToken(tk)

		var ads = ""
		if (tk is Token.NumberLiteral || tk is Token.IntLiteral)
		{
			ads = "\t"
		}
		println("$ads${line..sr.currentLineNumber()} $tk")

		if (tk == Token.EndOfStream)
		{
			break
		}
	}
}


