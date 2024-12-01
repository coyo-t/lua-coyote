import kotlin.io.path.Path
import kotlin.io.path.readText


fun main ()
{
	val sr = LStringReader(Path(
		"./assets/testfiles",
//		"octalnumbers"
		"multiline"
	).readText(Charsets.ISO_8859_1))

	while (true)
	{
		val tell = sr.tell()
		val line = sr.currentLineNumber()
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


