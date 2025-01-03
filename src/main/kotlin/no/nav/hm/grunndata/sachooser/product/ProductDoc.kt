package no.nav.hm.grunndata.sachooser.product

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Introspected
data class ProductDoc (
    val id: UUID = UUID.randomUUID(),
    val orderRef: String,
    val hmsArtNr: String,
    val iso: String,
    val title: String,
    val supplierRef: String,
    val articleType: String,
    val mainProduct: Boolean,
    val sparePart: Boolean,
    val accessory: Boolean,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)