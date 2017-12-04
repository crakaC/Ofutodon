package com.crakac.ofutodon.model.api

class UserAccount(
        val host: String,
        val id: Long,
        val name: String,
        val accessToken: String
) {
    val acct: String get() = "$host @$name"
}
