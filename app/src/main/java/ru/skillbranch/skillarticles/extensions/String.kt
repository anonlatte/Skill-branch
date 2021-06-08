package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    if (isNullOrEmpty()) return emptyList()
    val indices = mutableListOf<Int>()
    var lastIndex = 0
    while (lastIndex > -1 && lastIndex < length) {
        val indexOf = indexOf(substr, lastIndex, ignoreCase)
        lastIndex = indexOf
        if (indexOf > -1) {
            indices.add(lastIndex)
            lastIndex++
        }
    }
    return indices
}