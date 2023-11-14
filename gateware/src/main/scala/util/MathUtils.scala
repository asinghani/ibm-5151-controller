package util

object MathUtils {
    /**
     * Calculate exact integer log2 of a positive integer. Errors if input is not a power of 2
     */
    def intlog2(x: Int): Int = {
        assert(x > 0, "Input to intlog2 must be positive")
        val log = (Math.log(x) / Math.log(2.0)).round.toInt
        assert((1 << log) == x, "Input to intlog2 must be power of 2")
        log
    }

    /**
     * Calculate number of bits to store the number x
     */
    def clog2(x: Int): Int = {
        assert(x > 0, "Input to clog2 must be positive")
        val log = (Math.log(x+1) / Math.log(2.0)).ceil.toInt
        assert((1 << log) > x)
        log
    }

    /**
     * Custom string formatter: b"1010" -> (Int) 9
     */
    implicit class BinStrToInt(val sc: StringContext) extends AnyVal {
        def b(args: Any*): Int = {
            val strings = sc.parts.iterator
            val expressions = args.iterator
            val buf = new StringBuilder(strings.next())
            while(strings.hasNext) {
                buf.append(expressions.next())
                buf.append(strings.next())
            }

            Integer.parseInt("0" + buf.toString.replace("'", "").replace("_", ""), 2)
        }
    }
}
