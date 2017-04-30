package com.crakac.ofutodon.api

class Range(
        var maxId: Long? = null,
        var sinceId: Long? = null,
        var limit: Int = 40){

    var q = emptyMap<String, Long>()
        get(){
            return toQueryMap()
        }


    private fun toQueryMap(): Map<String, Long>{
        val map = HashMap<String, Long>()

        maxId?.let{
            map.put("max_id", it)
        }

        sinceId?.let{
            map.put("since_id", it)
        }

        map.put("limit", limit.toLong())
        return map
    }
}