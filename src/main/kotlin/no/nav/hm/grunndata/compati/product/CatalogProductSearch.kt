package no.nav.hm.grunndata.compati.product


import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import java.net.ConnectException
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.opensearch.client.Request
import org.opensearch.client.RestClient


import org.slf4j.LoggerFactory

@Singleton
class CatalogProductSearch(private val restClient: RestClient, private val objectMapper: ObjectMapper) {

    fun searchWithBodyResult(index: String, params: Map<String, String>?, body: String): List<CompatibleProductResult> {
        return try {
            val entity = StringEntity(body, ContentType.APPLICATION_JSON)
            val request: Request = newRequest("POST", "/$index/_search", params, entity)
            val httpEntity: HttpEntity = restClient.performRequest(request).entity
            val jsonString = EntityUtils.toString(httpEntity)
            val json =  objectMapper.readTree(jsonString)
            val hits = json.get("hits").get("hits")
            hits.map { CompatibleProductResult(
                score = it.get("_score").asDouble(),
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

    fun lookupHmsNrWithQuery(index:String, params: Map<String, String>?, hmsNr: String): CatalogProductDoc? {
        return try {
            val request: Request = newRequest("GET", "/$index/_doc/$hmsNr", params, null)
            val result =  restClient.performRequest(request)
            if (result.statusLine.statusCode == 404) {
                LOG.warn("Not found for hmsNr: $hmsNr")
                return null
            }
            val responseEntity: HttpEntity = restClient.performRequest(request).entity
            val jsonString = EntityUtils.toString(responseEntity)
            val json = objectMapper.readTree(jsonString).get("_source")
            return objectMapper.treeToValue(json, CatalogProductDoc::class.java)
        } catch (e: Exception) {
            LOG.error("No connection to Opensearch", e)
            throw e
        }
    }



    private fun newRequest(method: String, endpoint: String, params: Map<String, String>?, entity: StringEntity? ): Request {
        val request = Request(method, endpoint)
        params?.forEach(request::addParameter)
        request.entity = entity
        return request
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CatalogProductSearch::class.java)
    }
}