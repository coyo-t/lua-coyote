import java.lang.foreign.MemorySegment


class Mbuffer
{
	var buffer = MemorySegment.NULL
	var n = 0L
	var buffsize = 0L
}

class ZIO
