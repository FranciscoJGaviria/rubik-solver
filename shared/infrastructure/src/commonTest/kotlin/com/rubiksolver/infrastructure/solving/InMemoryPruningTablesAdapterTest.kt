package com.rubiksolver.infrastructure.solving

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryPruningTablesAdapterTest {
    @Test
    fun `pruning tables warm up in memory`() {
        val adapter = InMemoryPruningTablesAdapter()
        adapter.isReady() shouldBe true
    }
}
