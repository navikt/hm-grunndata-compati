package no.nav.hm.grunndata.compati.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/catalog/products")
class CompatibleCatalogProductController(private val catalogProductSearch: CatalogProductSearch,
                                         private val queryBuilder: QueryBuilder) {


    @Get("/compatibleWith")
    fun compatibleWidth(@QueryValue hmsNr: String, @QueryValue(defaultValue = "false") variant: Boolean? = false): List<CompatibleProductResult> {
        LOG.info("Lookup hmsNr: $hmsNr with variant: $variant")
        val doc = catalogProductSearch.lookupWithQuery(aliasName, null, hmsNr)
        if (doc!=null) {
            val jsonQuery = queryBuilder.buildJsonQueryForCompatibleWithSearch(
                doc.title,
                doc.postNr,
                doc.iso,
                doc.orderRef,
                !variant!!
            )
            return catalogProductSearch.searchWithBodyResult(aliasName, null, jsonQuery)
        }
        else {
            return emptyList()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CompatibleCatalogProductController::class.java)
    }

}
