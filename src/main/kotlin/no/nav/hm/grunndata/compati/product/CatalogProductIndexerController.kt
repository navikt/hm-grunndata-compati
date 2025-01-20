package no.nav.hm.grunndata.compati.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue

@Controller("/catalog/product/index")
class CatalogProductIndexerController(private val productIndexer: CatalogProductIndexer) {

    @Post("/")
    suspend fun index(@QueryValue orderRef: String?) {
        println("Indexing catalog product with orderRef: $orderRef")
        productIndexer.indexProducts(orderRef)
    }
}