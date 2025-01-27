package no.nav.hm.grunndata.compati.product

import io.micronaut.context.annotation.Factory
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import no.nav.hm.rapids_rivers.micronaut.RapidPushService

@Factory
class MockFactory {

    @MockBean(RapidPushService::class)
    fun rapidPushService(): RapidPushService = mockk(relaxed = true)

}