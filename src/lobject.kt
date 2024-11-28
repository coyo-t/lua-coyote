import kotlin.math.ceil
import kotlin.math.log2

val log_2 = IntArray(256) { ceil(log2(it+1.0)).toInt() }

