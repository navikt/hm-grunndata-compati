package no.nav.hm.grunndata.compati.product


import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.util.UUID

@Introspected
data class CatalogSeriesInfo(
    val hmsArtNr: String,
    val iso: String,
    val orderRef: String,
    val title: String,
    val supplierRef: String,
    val reference: String,
    val postNr: String,
    val mainProduct: Boolean,
    val sparePart: Boolean,
    val accessory: Boolean,
    val seriesTitle: String,
    val seriesId: UUID,
    val productId: UUID?,
    val created: LocalDateTime,
    val updated: LocalDateTime
)

