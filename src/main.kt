import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.properties.Delegates.observable

class GrowBuffer (initialSize:Number)
{

	private var mem = MemorySegment.NULL
	private var f = mem.asbb()
	private var pretendSize = 0L

	private fun MemorySegment.asbb ()
		= this.asByteBuffer().order(ByteOrder.nativeOrder())

	init
	{
		pretendSize = initialSize.toLong()
		resize(pretendSize)
	}

	private fun resize (newSize:Long)
	{
		if (newSize <= mem.byteSize())
		{
			return
		}
		mem = Arena.ofAuto().allocate(newSize).copyFrom(mem)
		f = with(mem.asbb()) {
			position(f.position())
			limit(f.limit())
		}
	}

	private fun ensureCapacity (amount:Long)
	{
		val bs = mem.byteSize()
		val newSz = bs+amount
		if (newSz >= bs)
		{
			pretendSize = newSz
			resize(newSz + (newSz ushr 1))
		}
	}

	private inline fun
	ensureCapacity (amount:Long, bloc: ByteBuffer.()->Unit)
	{
		ensureCapacity(amount)
		f.apply(bloc)
	}

	fun writeU8 (char:Char)
	{
		writeU8(char.code)
	}

	fun writeU8 (value:Int)
	{
		ensureCapacity(1) {
			put(value.toByte())
		}
	}


}




fun String.escapeilize ():String
{
	val outs = StringBuilder()
	val hxch = "0123456789ABCDEF"
	for (ch in this)
	{
		when (ch)
		{
			'\u0007' -> outs.append("\\a")
			'\u0008' -> outs.append("\\b")
			'\u000c' -> outs.append("\\f")
			'\u000a' -> outs.append("\\n")
			'\u000d' -> outs.append("\\r")
			'\u0009' -> outs.append("\\t")
			'\u000b' -> outs.append("\\v")
			'\"' -> outs.append("\\\"")
			'\'' -> outs.append("\\\'")
			'\\' -> outs.append("\\\\")
			else -> {
				if (ch in ' '..'~')
				{
					outs.append(ch)
				}
				else
				{
					val cp = ch.code
					outs.append("\\x")
					outs.append(hxch[(cp shr 4) and 0b00001111])
					outs.append(hxch[cp and 0b00001111])
				}
			}
		}
	}
	return outs.toString()
}

fun Char.hexToInt (): Int
{
	return when (this)
	{
		in '0'..'9' -> this - '0'
		in 'a'..'f' -> this - 'W'
		in 'A'..'F' -> this - '7'
		else -> throw IllegalArgumentException("Expecting a hex digit")
	}
}

const val EOS = Char.MAX_VALUE

val CONSIDERED_WHITESPACE = setOf(
	'\u0009',
	'\u000a',
	'\u000b',
	'\u000c',
	'\u000d',
	'\u0020',
)

open class StringReader(var text:String)
{
	var cursor by observable(0) { _, _, _ ->
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

	fun skipWhile (eq:Char)
	{
		while (peek() == eq)
		{
			skip()
		}
	}

	inline fun skipWhile (fn:(Char)->Boolean)
	{
		while (fn(peek()))
		{
			skip()
		}
	}

}



class LStringReader(text: String): StringReader(text)
{


	fun peekIsNewline () = peek().let { it=='\r'||it=='\n' }

	fun voreNewline (doRewind:Boolean=false): Boolean
	{
		if (doRewind)
		{
			rewind()
		}
		if (!peekIsNewline())
		{
			return false
		}
		val pev = peek()
		skip()
		if (peekIsNewline() && peek() != pev)
		{
			skip()
		}
		if (lineNumber++ >= Int.MAX_VALUE)
		{
			throw IllegalStateException("Chunk has too many lines")
		}
		return true
	}

	fun readString (): String
	{
		val delimiter = read()
		val outs = StringBuilder()
		while (peek() != delimiter)
		{
			when (val ch = read())
			{
				EOS, '\r', '\n' -> throw RuntimeException("Unfinished String")
				'\\' -> {
					when (val escCh = read())
					{
						'a' -> outs.append('\u0007')
						'b' -> outs.append('\u0008')
						'f' -> outs.append('\u000c')
						'n' -> outs.append('\u000a')
						'r' -> outs.append('\u000d')
						't' -> outs.append('\u0009')
						'v' -> outs.append('\u000b')
						'x' ->
						{
							val hi = read().hexToInt()
							val lo = read().hexToInt()
							outs.append(((hi shl 4) or lo).toChar())
						}
						'u' -> TODO("i dont feel like adding the utf8 esc stuff rn")

						'\r', '\n' -> outs.append('\n').also { voreNewline(true) }

						'\"', '\'', '\\' ->  outs.append(escCh)

						'z' -> {
							while (peek() in CONSIDERED_WHITESPACE)
							{
								if (peekIsNewline())
								{
									voreNewline()
								}
								else
								{
									skip()
								}
							}
						}
						else -> {
							check(peek().isDigit()) { "Invalid escape sequence" }
							TODO("dont feel like adding decimal esc seqs")
						}
					}
				}
				else -> outs.append(ch)
			}
		}
		return outs.toString()
	}



}


const val TEST = (
	"'This is a\\x20test\\r\\n string!\\\r\nyeah'"+
	"\u00DE\u00AD\u00CA\u0075\\x32"
)

fun main ()
{
	val ub = Path("./assets/ubyte.txt").readText(Charsets.ISO_8859_1)
	println(ub.escapeilize())
}


