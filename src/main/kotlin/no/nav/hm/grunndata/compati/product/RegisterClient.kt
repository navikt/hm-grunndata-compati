package no.nav.hm.grunndata.compati.product

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${grunndata.register.url}")
interface RegisterClient {

    @Get(uri ="/internal/catalog/import", consumes = [APPLICATION_JSON])
    suspend fun fetchCatalogImport(
        @QueryValue("orderRef") orderRef: String? = null,
        @QueryValue("size") size: Int? = null,
        @QueryValue("page") page: Int? = null,
    ): Page<CatalogImport>

}

