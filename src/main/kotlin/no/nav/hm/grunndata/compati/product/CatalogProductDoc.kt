package no.nav.hm.grunndata.compati.product

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.util.UUID

@Introspected
data class CatalogProductDoc (
    val id: String,
    val orderRef: String,
    val hmsArtNr: String,
    val iso: String,
    val title: String,
    val seriesTitle: String,
    val seriesId: UUID,
    val productId: UUID?,
    val postNr: List<String> = emptyList(),
    val supplierRef: String,
    val mainProduct: Boolean,
    val sparePart: Boolean,
    val accessory: Boolean,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

fun CatalogSeriesInfo.toDoc() = CatalogProductDoc(
    id = this.hmsArtNr,
    orderRef = this.orderRef,
    hmsArtNr = this.hmsArtNr,
    iso = this.iso,
    postNr = parsedelkontraktNr(this.postNr!!).map { it.first},
    title = this.title,
    seriesTitle = this.seriesTitle,
    seriesId = this.seriesId,
    productId = this.productId,
    supplierRef = this.supplierRef,
    mainProduct = this.mainProduct,
    sparePart = this.sparePart,
    accessory = this.accessory,
    created = this.created,
    updated = this.updated
)
val delKontraktRegex = Regex("d(\\d+)([A-Q-STU-Z]*)r*(\\d*),*")

fun parsedelkontraktNr(subContractNr: String): List<Pair<String, Int>> {
    try {
        val cleanSubContractNr = subContractNr.replace("\\s".toRegex(), "")

        var matchResult = delKontraktRegex.find(cleanSubContractNr)
        val mutableList: MutableList<Pair<String, Int>> = mutableListOf()
        if (matchResult != null) {
            while (matchResult != null) {
                val groupValues = matchResult.groupValues
                val post = groupValues[1] + groupValues[2].uppercase()
                val rank1 = groupValues[3].toIntOrNull() ?: 99
                mutableList.add(Pair(post, rank1))
                matchResult = matchResult.next()
            }
        } else {
            throw Exception("Klarte ikke å lese delkontrakt nr. $subContractNr")
        }
        return mutableList
    } catch (e: Exception) {
        throw Exception("Klarte ikke å lese post og rangering fra delkontrakt nr. $subContractNr")
    }
}
