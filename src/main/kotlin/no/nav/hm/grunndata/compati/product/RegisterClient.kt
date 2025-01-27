package no.nav.hm.grunndata.compati.product

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.rapid.dto.CatalogFileStatus

@Client("\${grunndata.register.url}/internal/catalog/import")
interface RegisterClient {

    @Get(uri ="/", consumes = [APPLICATION_JSON])
    suspend fun fetchCatalogImport(
        @QueryValue("orderRef") orderRef: String? = null,
    ): List<CatalogSeriesInfo>

    @Get("/files/status/{status}", consumes = [APPLICATION_JSON])
    suspend fun fetchCatalogFilesByStatus(
        pageable: Pageable,
        status: CatalogFileStatus,
    ): Slice<CatalogFileDTO>

    @Get("/hmsnr/{hmsNr}", consumes = [APPLICATION_JSON])
    suspend fun fetchCatalogImportByHmsNr(hmsNr: String): CatalogSeriesInfo?

}

@Introspected
data class CatalogFileDTO(
    val id: UUID,
    val fileName: String,
    val fileSize: Long,
    val orderRef: String,
    val supplierId: UUID,
    val updatedByUser: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val status: CatalogFileStatus,
)

