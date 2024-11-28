import kotlin.math.ceil
import kotlin.math.log2

private val log_2 = IntArray(256) { ceil(log2(it+1.0)).toInt() }

fun luaO_ceillog2 (x: UInt): Int
{
	var l = 0
	var x = x
	x--
	while (x >= 256u)
	{
		l += 8
		x = x shr 8
	}
	return l + log_2[x.toInt()]
}


