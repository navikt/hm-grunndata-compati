package no.nav.hm.grunndata.compati.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/catalog/product")
class CompatibleCatalogProductController(private val catalogProductSearch: CatalogProductSearch) {


    @Get("/compatibleWith")
    fun compatibleWidth(@QueryValue hmsNr: String): List<CompatibleProductResult> {
        LOG.info("Lookup hmsNr: $hmsNr")
        val doc = catalogProductSearch.lookupWithQuery("catalogproducts", null, hmsNr)
        val jsonQuery = buildJsonQuery(doc.title, doc.postNr, doc.iso, doc.orderRef)
        LOG.info("Query: $jsonQuery")
        return catalogProductSearch.searchWithBodyResult("catalogproducts", null, jsonQuery)
    }

    private fun buildJsonQuery(title:String, postNr: List<String>, iso: String, orderRef: String): String {
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
                        "term": {
                          "orderRef": {
                            "value": "$orderRef"
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
        private val LOG = LoggerFactory.getLogger(CompatibleCatalogProductController::class.java)
    }
}
