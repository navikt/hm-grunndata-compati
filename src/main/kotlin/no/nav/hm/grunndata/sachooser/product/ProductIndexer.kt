package no.nav.hm.grunndata.sachooser.product

import jakarta.inject.Singleton
import org.opensearch.client.opensearch.OpenSearchClient

@Singleton
class ProductIndexer(private val osClient: OpenSearchClient, private val registerClient: RegisterClient) {



    fun indexProducts() {
        val products = registerClient.fetchCatalogImport(size = 5000, page = 0).content
    }

}