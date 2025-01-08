package no.nav.hm.grunndata.compati.product

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest

import java.util.UUID
import org.junit.jupiter.api.Test

import org.slf4j.LoggerFactory

@MicronautTest
class OpensearchIndexerTest(
    private val catalogProductIndexer: CatalogProductIndexer,
    private val osContainer: OSContainer

) {

    @Test
    fun testProductIndexer() {
        catalogProductIndexer.shouldNotBeNull()
        osContainer.shouldNotBeNull()
        val productDoc = CatalogProductDoc(
            id = UUID.randomUUID(),
            orderRef = "orderRef",
            title = "title",
            hmsArtNr = "123456",
            supplierRef = "supplierRef",
            iso = "12345678",
            mainProduct = true,
            accessory = false,
            sparePart = false
        )
        val response = catalogProductIndexer.index(listOf(productDoc))
        response.errors() shouldBe false
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OpensearchIndexerTest::class.java)
    }

}