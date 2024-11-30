import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GrowBuffer (initialSize:Number)
{
	var memory = MemorySegment.NULL
		private set

	private var f = memory.asbb()
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
		if (newSize <= memory.byteSize())
		{
			return
		}
		memory = Arena.ofAuto().allocate(newSize).copyFrom(memory)
		f = (memory.asbb()).apply {
			position(f.position())
			limit(f.limit())
		}
	}

	private fun ensureCapacity (amount:Long)
	{
		val bs = memory.byteSize()
		val newSz = tell()+amount
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

	fun writeInt (i:Int)
	{
		ensureCapacity(Int.SIZE_BYTES.toLong()) { putInt(i) }
	}

	fun writeInt (i:Char)
	{
		writeInt(i.code)
	}


	fun tell ():Int
	{
		return f.position()
	}


}