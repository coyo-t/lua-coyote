const val MAX_LOCALS = 200

internal class BlockCount
{
	var previous: BlockCount? = null
	var firstLabel = 0
	var firstGoto = 0
	var activeLocalCount = 0
	var anyUpvalue = false
	var insideToBeClosed = false
}






