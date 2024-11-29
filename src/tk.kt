import java.lang.foreign.MemorySegment

const val LUA_ENV = "_ENV"

sealed class Token

class ReservedToken(val name:String): Token()

class FloatToken (val n:Double): Token()
class IntToken (val n:Int): Token()
class StringToken (val string:String): Token()
class IdentifierToken (val name:String): Token()
data object EndOfStreamToken: Token()




// and
// break
// do
// else
// elseif
// end
// false
// for
// function
// goto
// if
// in
// local
// nil
// not
// or
// repeat
// return
// then
// true
// until
// while
// //
// ..
// ...
// ==
// >=
// <=
// ~=
// <<
// >>
// ::




fun interface lua_Reader
{
	operator fun invoke (L: lua_State, userData:Any?): MemorySegment
}

class lua_State
