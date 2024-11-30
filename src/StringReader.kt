import kotlin.properties.Delegates

open class StringReader(var text:String)
{
	var cursor by Delegates.observable(0) { _, _, _ ->
		_currDirty = true
	}

	protected var _lineNumber = 1
	protected var _mark = 0

	private var _currDirty = true
	private var _cur = EOS
	private var _pev = EOS-1

	fun get (i:Int):Char
	{
		return text.getOrElse(i) { EOS }
	}

	fun markSkip ():Int
	{
		return mark().also { skip() }
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

	fun mark (): Int
	{
		_mark = cursor
		return _mark
	}

	fun peek (): Char
	{
		if (_currDirty)
		{
			_pev = _cur
			_cur = get(cursor)
			_currDirty = false
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