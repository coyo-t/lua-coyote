




class Token
class FuncState
class lua_State
class Mbuffer
class ZIO
class Table
class Dyndata
class TString

// state of the lexer plus state of the parser when shared by all functions
class LexState
{
	/* current character (charint) */
	var current = 0
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

	var L:lua_State? = null
	/* input stream */
	var z:ZIO? = null
	/* buffer for tokens */
	var buff:Mbuffer? = null
	/* to avoid collection/reuse strings */
	var h:Table?=null
	/* dynamic structures used by the parser */
	var dyd:Dyndata?=null
	/* current source name */
	var source:String=""
	/* environment variable name */
	var envn:String=""
}




fun main ()
{

}


