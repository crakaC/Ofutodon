package com.crakac.ofutodon.model.api

class Range(
        var maxId: Long? = null,
        var sinceId: Long? = null,
        var limit: Int? = 40) {

    var q = emptyMap<String, String>()
        get() {
            return toQueryMap()
        }


    private fun toQueryMap(): Map<String, String> {
        val map = HashMap<String, String>()

        maxId?.let {
            map.put("max_id", it.toString())
        }

        sinceId?.let {
            map.put("since_id", it.toString())
        }

        limit?.let {
            map.put("limit", limit.toString())
        }

        return map
    }
}