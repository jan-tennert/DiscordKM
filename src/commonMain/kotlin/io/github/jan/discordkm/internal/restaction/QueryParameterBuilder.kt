package io.github.jan.discordkm.internal.restaction

class QueryParameterBuilder {

    private val map = hashMapOf<String, String>()

    fun build() : String {
        if(map.isEmpty()) return ""
        var isFirst: Boolean = true
        var index = 1
        val query = buildString {
            map.forEach { (key, value)  ->
                val prefix = if(isFirst) "?" else "&"
                append("$prefix$key=$value")
                index++
                isFirst = false
            }
        }
        return query
    }

    fun put(key: String, value: Any) {
        map[key] = value.toString()
    }

    fun putOptional(key: String, value: Any?) {
        value?.let {
            map[key] = value.toString()
        }
    }

}

fun buildQuery(builder: QueryParameterBuilder.() -> Unit) = QueryParameterBuilder().apply(builder).build()