package tokenize

import kotlin.properties.Delegates

open class StringReader(var text: CharSequence)
{
	var cursor by Delegates.observable(0) { _, _, _ ->
		_currDirty = true
	}

	protected var _lineNumber = 1

	private var _currDirty = true
	private var _cur = EOS
	private var _pev = EOS-1

	fun get (i:Int):Char
	{
		return text.getOrElse(i) { EOS }
	}

	fun seek (to:Int)
	{
		cursor = to
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

	fun currentLineNumber () = _lineNumber

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

	protected fun skipIf (condition: Boolean): Boolean
	{
		if (condition)
		{
			return true.also { skip() }
		}
		return false
	}

	fun vore (expect:Char): Boolean
	{
		return skipIf(peek() == expect)
	}

	fun voreIn (s:String): Boolean
	{
		return skipIf(peek() in s)
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

	companion object
	{
		@JvmStatic
		protected val EOS = Char.MAX_VALUE
	}

}