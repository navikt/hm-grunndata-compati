package no.nav.hm.grunndata.compati.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.compati.product.compatible.CompatibleAIFinder
import org.slf4j.LoggerFactory

@Controller("/catalog/products")
class CompatibleWithController(private val catalogProductSearch: CatalogProductSearch,
                                         private val queryBuilder: QueryBuilder,
                                         private val compatibleAIFinder: CompatibleAIFinder) {


    @Get("/compatibleWith")
    fun compatibleWith(@QueryValue hmsNr: String, @QueryValue(defaultValue = "false") variant: Boolean? = false): List<CompatibleProductResult> {
        LOG.info("Lookup hmsNr: $hmsNr with variant: $variant")
        val doc = catalogProductSearch.lookupHmsNrWithQuery(aliasName, mapOf("ignore" to "404"), hmsNr)
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
            LOG.info("Could not find compatible products for : $hmsNr")
            return emptyList()
        }
    }

    @Get("/compatibleWith/ai/series")
    fun compatibleWithAI(@QueryValue hmsNr: String): List<CompatibleProductResult> {
        LOG.info("Lookup hmsNr: $hmsNr with AI")
        val doc = catalogProductSearch.lookupHmsNrWithQuery(aliasName, mapOf("ignore" to "404"), hmsNr)
        if (doc!=null) {
            LOG.info("accessory with title: ${doc.title}")
            val query =  queryBuilder.buildJsonQueryLookupMainByOrderRef(doc.orderRef)
            LOG.debug(query)
            val mainProducts = catalogProductSearch.searchWithBodyResult(aliasName, null, query)
            LOG.info("Got ${mainProducts.size} main products for orderRef: ${doc.orderRef}")
            val hmsnrTitlePair: List<HmsNrTitlePair> = mainProducts.map { HmsNrTitlePair(it.hmsArtNr, it.seriesTitle.trim()) }
            val hmsNrs =  compatibleAIFinder.findCompatibleProducts(doc.title, hmsnrTitlePair)
            return hmsNrs.mapNotNull { mainProducts.find { m -> m.hmsArtNr == it.hmsnr } }
        }
        LOG.info("Could not find compatible products for : $hmsNr")
        return emptyList()
    }

    @Get("/compatibleWith/ai/variants")
    fun compatibleWithVariantsAI(@QueryValue hmsNr: String, @QueryValue seriesId: String): List<CompatibleProductResult> {
        LOG.info("Lookup hmsNr: $hmsNr with AI for variants")
        val doc = catalogProductSearch.lookupHmsNrWithQuery(aliasName, mapOf("ignore" to "404"), hmsNr)
        if (doc!=null) {
            LOG.info("Accessory with title: ${doc.title}")
            val query =  queryBuilder.buildJsonQueryLookupMainBySeriesId(seriesId)
            val mainVariants = catalogProductSearch.searchWithBodyResult(aliasName, null, query)
            LOG.info("Got ${mainVariants.size} main variants for seriesId: $seriesId")
            val hmsnrTitlePair: List<HmsNrTitlePair> = mainVariants.map { HmsNrTitlePair(it.hmsArtNr, it.title) }
            val hmsNrs =  compatibleAIFinder.findCompatibleProducts(doc.title, hmsnrTitlePair)
            return hmsNrs.mapNotNull { mainVariants.find { m -> m.hmsArtNr == it.hmsnr } }
        }
        LOG.info("Could not find compatible variants for : $hmsNr")
        return emptyList()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CompatibleWithController::class.java)
    }

}

data class HmsNrTitlePair(val hmsNr: String, val title: String)