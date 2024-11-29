const val LUA_ENV = "_ENV"


fun TK.eq (ch:Char) = rs == ch
fun Char.eq (ch:TK) = ch.eq(this)

fun TK.neq (ch:Char) = rs != ch
fun Char.neq (ch:TK) = ch.neq(this)


@JvmInline
value class TK(val rs: Char)
{

	constructor (i:Int): this(i.toChar())


	companion object
	{
		const val FIRST_RESERVED = 0xFF + 1
		const val NUM_RESERVED = 22


		private fun Int.astk(): TK
		{
			return TK((FIRST_RESERVED + this).toChar())
		}
		val EOZ = (-1).astk()

		/* terminal symbols denoted by reserved words */
		val AND = 0.astk()
		val BREAK = 1.astk()
		val DO = 2.astk()
		val ELSE = 3.astk()
		val ELSEIF = 4.astk()
		val END = 5.astk()
		val FALSE = 6.astk()
		val FOR = 7.astk()
		val FUNCTION = 8.astk()
		val GOTO = 9.astk()
		val IF = 10.astk()
		val IN = 11.astk()
		val LOCAL = 12.astk()
		val NIL = 13.astk()
		val NOT = 14.astk()
		val OR = 15.astk()
		val REPEAT = 16.astk()
		val RETURN = 17.astk()
		val THEN = 18.astk()
		val TRUE = 19.astk()
		val UNTIL = 20.astk()
		val WHILE = 21.astk()
		/* other terminal symbols */
		val IDIV = 22.astk()
		val CONCAT = 23.astk()
		val DOTS = 24.astk()
		val EQ = 25.astk()
		val GE = 26.astk()
		val LE = 27.astk()
		val NE = 28.astk()
		val SHL = 29.astk()
		val SHR = 30.astk()
		val DBCOLON = 31.astk()
		val EOS = 32.astk()
		val FLT = 33.astk()
		val INT = 34.astk()
		val NAME = 35.astk()
		val STRING = 36.astk()
	}
}

class FuncState
class lua_State
class Mbuffer
class ZIO
class Table
class Dyndata
class TString

class Token
{
	var token = TK(0)
	var seminfo = SemInfo()
}

class SemInfo
{
	var r = 0.0
	var i = 0
	var ts:TString?=null
}

class LuaSyntaxException: Exception()

// state of the lexer plus state of the parser when shared by all functions
class LexState
{
	/* current character (charint) */
	var current = TK(0)
	/* input line counter */
	var linenumber = 0
	/* line of last token 'consumed' */
	var lastline = 0
	/* current token */
	var t = Token()
	/* look ahead token */
	var lookahead = Token()
	/* current function (parser) */
	var fs: FuncState? = null

	lateinit var L: lua_State
	/* input stream */
	var z:ZIO? = null
	/* buffer for tokens */
	var buff:Mbuffer? = null
	/* to avoid collection/reuse strings */
	var h:Table?=null
	/* dynamic structures used by the parser */
	var dyd:Dyndata?=null
	/* current source name */
	lateinit var source:TString
	/* environment variable name */
	lateinit var envn:TString


	fun init (L:lua_State)
	{
		TODO()
	}

	fun setInput (
		L:lua_State,
		z:ZIO,
		source:String,
		firstChar:Int
	)
	{
		TODO()
	}

	fun newString (
		str:String,
		l:Long,
	): TString
	{
		TODO()
	}

	fun next ()
	{
		lastline = linenumber
		if (lookahead.token != TK.EOS)
		{
			/* is there a look-ahead token? */
			/* use this one and discharge it */
			t = lookahead
			lookahead.token = TK.EOS
		}
		else
		{
			t.token = llex(t.seminfo)
		}

		TODO()
	}

	fun lookahead (): TK
	{
		check(lookahead.token == TK.EOS)
		lookahead.token = llex(lookahead.seminfo)
		return lookahead.token
	}

	fun syntaxerror (s:String): Nothing
	{
		TODO()
	}

	fun token2str (token:Int):String
	{
		TODO()
	}

	private fun check_next1(c:Char)=check_next1(TK(c))

	private fun check_next1(c:TK):Boolean
	{
		if (current == c)
		{
			next()
			return true
		}
		return false
	}

	private fun lexerror (msg:String, token:TK): Nothing
	{
		val msg = luaG_addinfo(ls->L, msg, ls->source, ls->linenumber)
		if (token.rs.code != 0)
			luaO_pushfstring(ls->L, "%s near %s", msg, txtToken(ls, token))
		throw LuaSyntaxException()
	}

	private fun llex (seminfo:SemInfo): TK
	{
//		luaZ_resetbuffer(ls->buff);
		while (true)
		{
			when (current)
			{
				TK('\n'), TK('\r') -> {
					/* line breaks */
					inclinenumber()
					break;
				}
				TK(' '), TK(0xC), TK('\t'), TK(0xB) -> {
					/* spaces */
					next()
					break
				}
				TK('-') -> {
					/* '-' or '--' (comment) */
					next()
					if (current == TK('-'))
					{
						return TK('-')
					}
					/* else is a comment */
					next()
					if (current == TK('['))
					{
						/* long comment? */
						val sep = skip_sep()
						/* 'skip_sep' may dirty the buffer */
						luaZ_resetbuffer(buff)
						if (sep >= 2)
						{
							/* skip long comment */
							read_long_string(null, sep)
							/* previous call may dirty the buff. */
							luaZ_resetbuffer(buff)
							break;
						}
					}
					/* else short comment */
					while (!currIsNewline() && current != TK.EOZ)
					{
						/* skip until end of line (or end of file) */
						next()
					}
					break
				}
				TK('[')-> {
					/* long string or simply '[' */
					val sep = skip_sep()
					if (sep >= 2)
					{
						read_long_string(seminfo, sep)
						return TK.STRING
					}
					/* '[=...' missing second bracket? */
					if (sep == 0)
						lexerror("invalid long string delimiter", TK.STRING)
					return TK('[')
				}
				TK('=') -> {
					next()
					/* '==' */
					if (check_next1('='))
						return TK.EQ
					return TK('=')
				}
				TK('<') -> {
					next()
					if (check_next1('='))
						return TK.LE /* '<=' */
					if (check_next1('<'))
						return TK.SHL /* '<<' */
					return TK('<')
				}
				TK('>') -> {
					next()
					if (check_next1('=')) return TK.GE; /* '>=' */
					if (check_next1('>')) return TK.SHR; /* '>>' */
					return TK('>')
				}
				TK('/') -> {
					next()
					if (check_next1('/')) return TK.IDIV; /* '//' */
					return TK('/')
				}
				TK('~') -> {
					next()
					if (check_next1('=')) return TK.NE /* '~=' */
					return TK('~')
				}
				TK(':') -> {
					next()
					if (check_next1(':')) return TK.DBCOLON; /* '::' */
					return TK(':')
				}
				TK('"'), TK('\'') -> {
					/* short literal strings */
					read_string(current, seminfo)
					return TK.STRING
				}
				TK('.') -> {
					/* '.', '..', '...', or number */
					save_and_next()
					if (check_next1('.'))
					{
						if (check_next1('.'))
							return TK.DOTS /* '...' */
						return TK.CONCAT /* '..' */
					}
					if (!lisdigit(current)) return TK('.')
					return read_numeral(seminfo)
				}

				TK('0'),
				TK('1'),
				TK('2'),
				TK('3'),
				TK('4'),
				TK('5'),
				TK('6'),
				TK('7'),
				TK('8'),
				TK('9') -> {
					return read_numeral(seminfo)
				}
				TK.EOZ -> return TK.EOS
				else -> {
					if (lislalpha(current))
					{
						/* identifier or reserved word? */
						do
						{
							save_and_next();
						} while (lislalnum(current))
						val ts = luaX_newstring(
							luaZ_buffer(buff),
							luaZ_bufflen(buff)
						)
						seminfo.ts = ts
						if (isreserved(ts)) /* reserved word? */
							return TK(ts.extra - 1 + TK.FIRST_RESERVED)
						return TK.NAME
					}
					/* single-char tokens ('+', '*', '%', '{', '}', ...) */
					val c = current
					next()
					return c
				}
			}
		}
	}

}




fun main ()
{

}


