import kotlin.properties.Delegates

open class StringReader(var text:String)
{
	var cursor by Delegates.observable(0) { _, _, _ ->
		currDirty = true
	}

	var lineNumber = 1

	private var currDirty = true
	private var _cur = EOS
	private var _pev = EOS-1

	fun get (i:Int):Char
	{
		return text.getOrElse(i) { EOS }
	}

	fun skip () = cursor++
	fun skip (amount:Int)
	{
		cursor += amount
	}
	fun rewind () = cursor--

	fun pev (): Char
	{
		return _pev
	}

	fun peek (): Char
	{
		if (currDirty)
		{
			_pev = _cur
			_cur = get(cursor)
			currDirty = false
		}
		return _cur
	}

	fun read () = get(cursor++)

	fun vore (expect:Char): Boolean
	{
		if (peek() == expect)
		{
			return true.also { skip() }
		}
		return false
	}

	fun tell () = cursor

	fun skipWhile (eq:Char):Int
	{
		val start = tell()
		while (peek() == eq)
		{
			skip()
		}
		return tell()-start
	}

	inline fun skipWhile (fn:(Char)->Boolean):Int
	{
		val start = tell()
		while (fn(peek()))
		{
			skip()
		}
		return tell()-start
	}

}