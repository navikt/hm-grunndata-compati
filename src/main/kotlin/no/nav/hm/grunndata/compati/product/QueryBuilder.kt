package no.nav.hm.grunndata.compati.product

import jakarta.inject.Singleton

@Singleton
class QueryBuilder {

    fun buildJsonQueryLookupMainByOrderRef(orderRef: String): String {

        return """
{
  "query": {
          "bool": {
            "must": [
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

    fun buildJsonQueryLookupMainBySeriesId(seriesId: String): String {

        return """
{
  "query": {
          "bool": {
            "must": [
              {
                "term": {
                  "mainProduct": {
                    "value": true
                  }
                }
              },
              {
                "term": {
                  "seriesId": {
                    "value": "$seriesId"
                  }
                }
              }
            ]
          }
  },
  "size": 500
}""".trimIndent()
    }

    fun buildJsonQueryForCompatibleWithSearch(
        title: String,
        postNr: List<String>,
        iso: String,
        orderRef: String,
        collapse: Boolean = true
    ): String {
        val postNrFilter = postNr.filterNot { it == "99" }
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
                            "title"
                          ],
                          "like": "$cleanTitle",
                          "min_term_freq": 1,
                          "max_query_terms": 25,
                          "min_doc_freq": 1,
                          "max_doc_freq": 300
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
                      }                      
                     ${
            if (postNrFilter.isNotEmpty()) ",             {\n" +
                    "                        \"bool\": {\n" +
                    "                          \"should\": [\n" +
                    "                            {\n" +
                    "                              \"terms\": {\n" +
                    "                                \"postNr\": [\n" +
                    "                                    ${postNrFilter.joinToString(",")}\n" +
                    "                                ]\n" +
                    "                              }\n" +
                    "                            }\n" +
                    "                          ]\n" +
                    "                        }\n" +
                    "                      }" else ""
        }
        
                    ]
                  }
                }
              ]
            }
          }
          ${
            if (collapse) ",\"collapse\": {\n" +
                    "            \"field\": \"seriesId\"\n" +
                    "          }" else ""
        }      
          ,"size": 500
        }""".trimIndent()
    }
}