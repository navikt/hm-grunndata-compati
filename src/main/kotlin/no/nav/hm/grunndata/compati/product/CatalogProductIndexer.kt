package no.nav.hm.grunndata.compati.product

import jakarta.inject.Singleton
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Refresh
import org.opensearch.client.opensearch.core.BulkRequest
import org.opensearch.client.opensearch.core.BulkResponse
import org.opensearch.client.opensearch.core.bulk.BulkOperation
import org.opensearch.client.opensearch.core.bulk.IndexOperation
import org.slf4j.LoggerFactory

@Singleton
class CatalogProductIndexer(private val client: OpenSearchClient, private val registerClient: RegisterClient) {



    fun indexProducts() {
        val products = registerClient.fetchCatalogImport(size = 5000, page = 0).content.map { it.toDoc() }
        index(products, "catalogproducts")
    }

    fun index(docs: List<CatalogProductDoc>, indexName: String="catalogproducts"): BulkResponse {
        val operations = docs.map { document ->
            BulkOperation.Builder().index(
                IndexOperation.of { it.index(indexName).id(document.id.toString()).document(document) }
            ).build()
        }
        val bulkRequest = BulkRequest.Builder()
            .index(indexName)
            .operations(operations)
            .refresh(Refresh.WaitFor)
            .build()
        return try {
            client.bulk(bulkRequest)
        }
        catch (e: Exception) {
            LOG.error("Failed to index $docs to $indexName", e)
            throw e
        }
    }
    companion object {
        private val LOG = LoggerFactory.getLogger(CatalogProductIndexer::class.java)
    }

}