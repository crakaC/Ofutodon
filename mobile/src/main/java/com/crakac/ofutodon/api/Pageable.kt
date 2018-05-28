package com.crakac.ofutodon.api

/**
 * Created by Kosuke on 2017/04/30.
 */
class Pageable<T>(val part: List<T>, val link: Link?) {
    val DEFAULT_LIMIT = 40
    fun nextRange(limit: Int = DEFAULT_LIMIT): Range = Range(link?.maxId, limit = limit)
    fun prevRange(limit: Int = DEFAULT_LIMIT): Range = Range(sinceId = link?.sinceId, limit = limit)
    fun toRange(limit: Int = DEFAULT_LIMIT): Range = Range(link?.maxId, link?.sinceId, limit)
}