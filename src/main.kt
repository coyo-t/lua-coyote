import kotlin.io.path.Path
import kotlin.io.path.readText


fun main ()
{
	val sr = LStringReader(Path(
		"./assets/testfiles",
//		"octalnumbers"
		"multiline"
	).readText(Charsets.ISO_8859_1))
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
}


