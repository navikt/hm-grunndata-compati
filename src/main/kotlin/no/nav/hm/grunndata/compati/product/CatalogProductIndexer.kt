package no.nav.hm.grunndata.compati.product

import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import java.io.StringReader
import java.time.LocalDate
import no.nav.hm.grunndata.compati.product.CatalogProductIndexerController.Companion
import no.nav.hm.grunndata.rapid.dto.CatalogFileStatus
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Refresh
import org.opensearch.client.opensearch._types.mapping.TypeMapping
import org.opensearch.client.opensearch.core.BulkRequest
import org.opensearch.client.opensearch.core.BulkResponse
import org.opensearch.client.opensearch.core.bulk.BulkOperation
import org.opensearch.client.opensearch.core.bulk.IndexOperation
import org.opensearch.client.opensearch.indices.CreateIndexRequest
import org.opensearch.client.opensearch.indices.ExistsAliasRequest
import org.opensearch.client.opensearch.indices.GetAliasRequest
import org.opensearch.client.opensearch.indices.IndexSettings
import org.opensearch.client.opensearch.indices.UpdateAliasesRequest
import org.opensearch.client.opensearch.indices.update_aliases.ActionBuilders
import org.slf4j.LoggerFactory

@Singleton
class CatalogProductIndexer(private val client: OpenSearchClient,
                            private val registerClient: RegisterClient) {




    init {
        try {
            initAlias()
        } catch (e: Exception) {
            LOG.error("Trying to init alias ${aliasName}, failed! OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            initAlias()
        }
    }

    private fun initAlias() {
        if (!existsAlias()) {
            LOG.warn("alias $aliasName is not pointing any index")
            val indexName = "${aliasName}_${LocalDate.now()}"
            LOG.info("Creating index $indexName")
            createIndex(indexName,settings, mapping)
            updateAlias(indexName)
        }
        else {
            LOG.info("Aliases is pointing to ${getAlias().toJsonString()}")
        }
    }

    fun existsAlias()
            = client.indices().existsAlias(ExistsAliasRequest.Builder().name(aliasName).build()).value()

    fun getAlias()
            = client.indices().getAlias(GetAliasRequest.Builder().name(aliasName).build())

    fun createIndex(indexName: String, settings: String, mapping: String): Boolean {
        val mapper = client._transport().jsonpMapper()
        val createIndexRequest = CreateIndexRequest.Builder().index(indexName)
        val settingsParser = mapper.jsonProvider().createParser(StringReader(settings))
        val indexSettings = IndexSettings._DESERIALIZER.deserialize(settingsParser, mapper)
        createIndexRequest.settings(indexSettings)
        val mappingsParser = mapper.jsonProvider().createParser(StringReader(mapping))
        val typeMapping = TypeMapping._DESERIALIZER.deserialize(mappingsParser, mapper)
        createIndexRequest.mappings(typeMapping)
        val ack = client.indices().create(createIndexRequest.build()).acknowledged()!!
        LOG.info("Created $indexName with status: $ack")
        return ack
    }

    fun updateAlias(indexName: String): Boolean {
        val updateAliasesRequestBuilder = UpdateAliasesRequest.Builder()
        if (existsAlias()) {
            val aliasResponse = getAlias()
            val indices = aliasResponse.result().keys
            indices.forEach { index ->
                val removeAction = ActionBuilders.remove().index(index).alias(aliasName).build()
                updateAliasesRequestBuilder.actions { it.remove(removeAction) }
            }
        }
        val addAction = ActionBuilders.add().index(indexName).alias(aliasName).build()
        updateAliasesRequestBuilder.actions { it.add(addAction) }
        val updateAliasesRequest = updateAliasesRequestBuilder.build()
        val ack = client.indices().updateAliases(updateAliasesRequest).acknowledged()
        LOG.info("update for alias $aliasName and pointing to $indexName with status: $ack")
        return ack
    }
    suspend fun indexProducts(orderRef: String? = null) {
        val products = registerClient.fetchCatalogImport(orderRef = orderRef ).map { it.toDoc() }
        LOG.info("Indexing ${products.size} catalog products for orderRef: $orderRef")
        index(products)
    }

    suspend fun indexProductByHmsNr(hmsNr: String) {
        registerClient.fetchCatalogImportByHmsNr(hmsNr = hmsNr)?.let { product->
            LOG.info("Indexing $hmsNr catalog products for hmsNr: $hmsNr")
            index(listOf(product.toDoc()))
        }
    }

    suspend fun indexAll() {
        LOG.info("Indexing all catalog products")
        val catalogProducts = registerClient.fetchCatalogFilesByStatus(Pageable.from(0, 1000), CatalogFileStatus.DONE)
            .content.map { it.orderRef }.distinct()
        catalogProducts.forEach { indexProducts(it) }
    }


    fun index(docs: List<CatalogProductDoc>, indexName: String = aliasName): BulkResponse {
        val operations = docs.map { document ->
            BulkOperation.Builder().index(
                IndexOperation.of { it.index(indexName).id(document.id).document(document) }
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
        val settings = CatalogProductIndexer::class.java
            .getResource("/opensearch/catalogproducts_settings.json")!!.readText()
        val mapping = CatalogProductIndexer::class.java
            .getResource("/opensearch/catalogproducts_mapping.json")!!.readText()

    }

}

val aliasName: String = "catalogproducts"
