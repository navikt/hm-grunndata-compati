package no.nav.hm.grunndata.compati.product

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class QueryBuilderTest(private val queryBuilder: QueryBuilder) {

    @Test
    fun testQueryBuilder() {
        val title = "title"
        val postNr = listOf("1", "2")
        val iso = "12345678"
        val orderRef = "orderRef"
        val collapse = false
        val query = queryBuilder.buildJsonQueryForCompatibleWithSearch(title, postNr, iso, orderRef, collapse)
        println(query)
    }
}