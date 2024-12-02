package tokenize

import java.nio.ByteBuffer

class SemanticsInfo(
	val stringData:ByteBuffer=emptybb,
	val number:Double=0.0,
	val integer:Int=0,
	val stringName:String="",
)
{

	companion object
	{
		private val emptybb = ByteBuffer.allocate(0)
	}
}