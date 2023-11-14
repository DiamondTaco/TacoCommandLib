package diamondtaco.tcl.commands

class FuzzyMatcher(private val strings: Set<String>) {
    private fun scoreString(input: String, target: String): Int {
        val starts: Map<Int, Char> = buildMap {
            var startIndex = 0
            for (word in target.split('-', ':')) {
                put(startIndex + 1, word[0])
                startIndex += word.length + 1
            }
        }

        var score = 0
        var targetInt = 0
        // initial jump fuzzy search
        for ((char, entry) in input.asSequence() zip starts.asSequence()) {
            val (idx, start) = entry
            if (char == start) {
                targetInt = idx
                score += 5
            } else {
                break
            }
        }

        outer@ for (ch in input) {
            do {
                val nextChar = target.getOrNull(targetInt++) ?: break@outer
            } while (ch != nextChar)
            score += starts[targetInt]?.let { 5 } ?: 1
        }

        return score
    }


    fun getMatches(input: String): List<String> {
        return strings.sortedByDescending { scoreString(input, it) }
    }
}