package diamondtaco.tcl.lib

class FuzzyMatcher(private val strings: Set<String>) {
    @Suppress("ControlFlowWithEmptyBody", "NAME_SHADOWING")
    private fun scoreString(input: String, target: String): Double {
        val split = target.split('-', '_', ':')
        val splitIndices = split.runningFold(0) { a, b -> a + b.length }.dropLast(1)
        val indexMap = splitIndices.zip(split).associate { (a, b) -> a to Pair(b.length, b.first()) }

        var simpleScore = 0.0
        var targetIndex = 0
        for (char in input) {
            while (char != (target.getOrNull(targetIndex++) ?: return Double.NaN));
            simpleScore += indexMap[targetIndex]?.first ?: 1
        }

        var score = 0.0
        var inputIndex = 0
        targetIndex = 0
        for ((char, data) in input.asIterable() zip indexMap.asIterable()) {
            val (index, data) = data
            val (length, start) = data
            if (char == start) {
                inputIndex += 1
                score += length
                targetIndex = index
            } else {
                break
            }
        }

        while (inputIndex < input.length) {
            val char = input[inputIndex]
            while (char != (target.getOrNull(targetIndex++) ?: return simpleScore));

            score += indexMap[targetIndex]?.first ?: 1
            inputIndex += 1
        }

        return score / target.length
    }


    fun getMatches(input: String): List<String> {
        return strings
            .mapNotNull { scoreString(input, it).takeUnless(Double::isNaN)?.to(it) }
            .sortedByDescending { it.first }
            .map { it.second }
    }
}