package no.nav.hm.grunndata.compati.product

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/catalog/product")
class CatalogProductSearchController(private val catalogProductSearch: CatalogProductSearch) {


    @Get("/search")
    fun search(@QueryValue hmsNr: String): List<CompatibleProductResult> {
        LOG.info("Searching for hmsNr: $hmsNr")
        val doc = catalogProductSearch.lookupWithQuery("catalogproducts", null, hmsNr)
        val jsonQuery = buildJsonQuery(doc.seriesTitle, doc.postNr, doc.iso)
        LOG.info("Query: $jsonQuery")
        return catalogProductSearch.searchWithBodyResult("catalogproducts", null, jsonQuery!!)
    }

    private fun buildJsonQuery(title:String, postNr: List<String>, iso: String): String {
        return """
        {
          "query": {
            "bool": {
              "should": [
                {
                  "bool": {
                    "must": [
                      {
                        "more_like_this": {
                          "fields": [
                            "seriesTitle"
                          ],
                          "like": "$title",
                          "min_term_freq": 1,
                          "max_query_terms": 25,
                          "min_doc_freq": 1
                        }
                      },
                      {
                        "prefix": {
                          "iso": {
                            "value": "${iso.substring(0, 4)}"
                          }
                        }
                      },
                      {
                        "term": {
                          "mainProduct": {
                            "value": true
                          }
                        }
                      },
                      {
                        "bool": {
                          "should": [
                            {
                              "terms": {
                                "postNr": [
                                    ${postNr.joinToString(",")}
                                ]
                              }
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              ]
            }
          },
          "collapse": {
            "field": "seriesId"
          },
          "size": 500
        }""".trimIndent()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CatalogProductSearchController::class.java)
    }
}

@Introspected
data class SearchRequest(
    val hmsArtNr: String,
)

