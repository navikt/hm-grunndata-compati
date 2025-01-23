package no.nav.hm.grunndata.compati.product

import jakarta.inject.Singleton

@Singleton
class QueryBuilder() {

    fun buildJsonQueryForCompatibleWithSearch(title:String, postNr: List<String>, iso: String, orderRef: String, collapse: Boolean = true): String {
        val cleanTitle = title.replace("\"", "").replace("'", "")
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
                          "like": "$cleanTitle",
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
          ${ if (collapse) "\"collapse\": {\n" +
                "            \"field\": \"seriesId\"\n" +
                "          }," else ""}
        
          "size": 500
        }""".trimIndent()
    }
}