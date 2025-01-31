package no.nav.hm.grunndata.compati.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class QueryBuilderTest(private val queryBuilder: QueryBuilder,
                       private val objectMapper: ObjectMapper) {



    @Test
    fun testQueryBuilder() {
        val title = "Senggrind 50 topputløser H seng Opus 90EW/120EW"
        val postNr = listOf("1", "2")
        val iso = "18121010"
        val orderRef = "3576215"
        val collapse = false
        val query = queryBuilder.buildJsonQueryForCompatibleWithSearch(title, postNr, iso, orderRef, collapse)
        val json = objectMapper.readTree(query)
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json))

        val title2 = "Senggrind 50 topputløser H seng Opus 90EW/120EW"
        val postNr2 = listOf("99")
        val iso2 = "18121010"
        val orderRef2 = "3576215"
        val collapse2 = true
        val query2 = queryBuilder.buildJsonQueryForCompatibleWithSearch(title2, postNr2, iso2, orderRef2, collapse2)

        val json2 = objectMapper.readTree(query2)
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json2))

    }
}