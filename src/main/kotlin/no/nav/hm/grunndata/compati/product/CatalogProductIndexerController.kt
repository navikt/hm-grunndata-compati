package no.nav.hm.grunndata.compati.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue

import org.slf4j.LoggerFactory

@Controller("/internal/catalog/products/index")
class CatalogProductIndexerController(private val productIndexer: CatalogProductIndexer){

    @Post("/")
    suspend fun index(@QueryValue orderRef: String?) {
        println("Indexing catalog product with orderRef: $orderRef")
        productIndexer.indexProducts(orderRef)

    }

    @Post("/all")
    suspend fun indexAll() {
        productIndexer.indexAll()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CatalogProductIndexerController::class.java)
    }
}