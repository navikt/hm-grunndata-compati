package no.nav.hm.grunndata.compati.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.rapid.dto.CatalogFileRapidDTO
import no.nav.hm.grunndata.rapid.dto.CatalogFileStatus
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class CatalogFileRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val catalogProductIndexer: CatalogProductIndexer

) : River.PacketListener {

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredCatalogfileV1) }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("dtoVersion") }
            .validate { it.demandKey("createdTime") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val dtoVersion = packet["dtoVersion"].asLong()
        val createdTime = packet["createdTime"].asLocalDateTime()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], CatalogFileRapidDTO::class.java)
        LOG.info("Received catalog file registration for catalogId: ${dto.orderRef} with supplierId: ${dto.supplierId}  eventId $eventId eventTime: $createdTime")
        runBlocking {
            if (dto.status == CatalogFileStatus.DONE) {
                catalogProductIndexer.indexProducts(dto.orderRef)
            }
        }

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CatalogFileRiver::class.java)
    }
}