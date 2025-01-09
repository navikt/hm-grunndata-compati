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
    val iso_2: String,
    val title: String,
    val supplierRef: String,
    val mainProduct: Boolean,
    val sparePart: Boolean,
    val accessory: Boolean,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

fun CatalogImport.toDoc() = CatalogProductDoc(
    id = this.hmsArtNr,
    orderRef = this.orderRef,
    hmsArtNr = this.hmsArtNr,
    iso = this.iso,
    iso_2 = this.iso.substring(0,4),
    title = this.title,
    supplierRef = this.supplierRef,
    mainProduct = this.mainProduct,
    sparePart = this.sparePart,
    accessory = this.accessory,
    created = this.created,
    updated = this.updated
)