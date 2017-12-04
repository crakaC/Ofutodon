package com.crakac.ofutodon.model.api

class MastodonApi(delegate: Mastodon, account: UserAccount? = null) : Mastodon by delegate {
    var currentId = 0L
        private set

    init {
        currentId = account?.id ?: 0L
    }
}