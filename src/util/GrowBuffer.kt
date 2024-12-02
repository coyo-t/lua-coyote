package util

import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GrowBuffer (initialSize:Int)
{
	companion object
	{
		private fun createBuffer (size:Int)
			= ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
	}


	private var data = createBuffer(initialSize)

	fun tell () = data.position()
	fun limit () = data.limit()
	fun flip () = apply { data.flip() }
	fun seek (to:Int) = apply { data.position(to) }
	fun clear () = apply { data.clear() }

	fun asMemorySegment () = MemorySegment.ofBuffer(data)

	private fun ensure (amount:Int)
	{
		val pos = data.position()
		val cap = data.capacity()
		val fin = pos + amount
		if (fin > cap)
		{
			data = createBuffer(fin + (fin ushr 1)).apply {
				put(data.flip())
				position(pos)
			}
		}
	}

	fun putByte (b:Byte) = apply {
		ensure(1)
		data.put(b)
	}

	fun putAsByte (i:Int) = putByte((i and 0xFF).toByte())

	fun putAsByte (ch:Char) = putAsByte(ch.code)

	fun putInt (i:Int) = apply {
		ensure(Int.SIZE_BYTES)
		data.putInt(i)
	}
}