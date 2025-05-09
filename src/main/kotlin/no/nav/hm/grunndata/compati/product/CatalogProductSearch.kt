package no.nav.hm.grunndata.compati.product


import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import java.net.ConnectException
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.generic.Request
import org.opensearch.client.opensearch.generic.Requests


import org.slf4j.LoggerFactory

@Singleton
class CatalogProductSearch(private val osClient: OpenSearchClient, private val objectMapper: ObjectMapper) {

    fun searchWithBodyResult(index: String, params: Map<String, String>, body: String): List<CompatibleProductResult> {
        return try {
            val request: Request = newRequest("POST", "/$index/_search", params, body)
            val response =  osClient.generic().execute(request)
            val jsonString = response.body.get().bodyAsString()
            val json =  objectMapper.readTree(jsonString)
            val hits = json.get("hits").get("hits")
            hits.map { CompatibleProductResult(
                title = it.get("_source").get("title").asText(),
                seriesTitle = it.get("_source").get("seriesTitle").asText(),
                seriesId = it.get("_source").get("seriesId").asText(),
                productId = it.get("_source").get("productId").asText(),
                hmsArtNr = it.get("_source").get("hmsArtNr").asText()
                )
            }
        } catch (e: ConnectException) {
            LOG.error("No connection to Opensearch", e)
            throw e
        }
    }

    fun lookupHmsNrWithQuery(index:String, params: Map<String, String>, hmsNr: String): CatalogProductDoc? {
        return try {
            val request: Request = newRequest("GET", "/$index/_doc/$hmsNr", params, null)
            val result =  osClient.generic().execute(request)
            if (result.status == 404) {
                LOG.warn("Not found for hmsNr: $hmsNr")
                return null
            }
            val jsonString = result.body.get().bodyAsString()
            val json = objectMapper.readTree(jsonString).get("_source")
            return objectMapper.treeToValue(json, CatalogProductDoc::class.java)
        } catch (e: Exception) {
            LOG.error("No connection to Opensearch", e)
            throw e
        }
    }



    private fun newRequest(method: String, endpoint: String, params: Map<String, String>, body: String?): Request {
        val requestBuilder = Requests.builder().method(method).endpoint(endpoint).query(params)
        if (body != null) {
            requestBuilder.json(body)
        }
        return requestBuilder.build()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CatalogProductSearch::class.java)
    }
}