package com.crakac.ofutodon.model.api

import com.crakac.ofutodon.db.User

class MastodonApi(delegate: MastodonService, userAccount: User? = null) : MastodonService by delegate {
    var currentId = 0L
        private set

    init {
        currentId = userAccount?.userId ?: 0L
    }
}