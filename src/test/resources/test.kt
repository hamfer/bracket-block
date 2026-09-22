object Main {
    @kotlin.jvm.JvmStatic
    fun main(args: Array<String>) {
        val testString = "Testing String [] () "
        val testMultiString: String = """
                Yes, this is a multi string
                
                """.trimIndent()
        val i = 0
        while (i++) {
            callAMethod(testString, testMultiString)
            i < 3
        }
        TODO(
            """
            |Cannot convert element
            |With text:
            |1,2,3,4
            """.trimMargin()
        )
    }
}