package no.nav.hm.grunndata.compati.product.compatible

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.api.Schema
import com.google.cloud.vertexai.api.Type
import com.google.cloud.vertexai.generativeai.ContentMaker
import com.google.cloud.vertexai.generativeai.GenerativeModel
import com.google.cloud.vertexai.generativeai.ResponseHandler
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import no.nav.hm.grunndata.compati.product.HmsNrTitlePair
import org.slf4j.LoggerFactory

@Singleton
class CompatibleAIFinder(private val config: VertexAIConfig, private val objectMapper: ObjectMapper ) {
    val instruction: String = """
        Du jobber i en NAV hjelpemiddelsentral, og bruker finnhjelpemiddel.no for informasjon om hjelpemidler.
    """.trimIndent()


    fun findCompatibleProducts(partsTitle: String, mainTitles: List<HmsNrTitlePair>): List<HmsNr> {
        val prompt = generatePrompt(partsTitle, mainTitles)
        LOG.info("Generated prompt: $prompt")
        return modelGenerateContent(prompt)
    }

    fun generatePrompt(accessory: String, mainProducts: List<HmsNrTitlePair>): String {
        val mainProductsString = mainProducts.joinToString("\n") { "hmsnr=${it.hmsNr} '${it.title.replace("'"," ")}'" }
        return "For følgende tilbehør:\n'${accessory.replace("'", " ")}'\nFinn ut hvilket hjelpemiddel som passer best blant disse hoved hjelpemiddel:\n$mainProductsString"
            .trimIndent().trim()
    }

    @Throws(Exception::class)
    private fun modelGenerateContent(prompt: String): List<HmsNr> {
        VertexAI(config.project, config.location).use { vertexAI ->
            val generationConfig: GenerationConfig = GenerationConfig.newBuilder()
                .setResponseMimeType("application/json")
                .setTemperature(config.temperature)
                .setResponseSchema(
                    Schema.newBuilder()
                        .setType(Type.ARRAY)
                        .setItems(
                            Schema.newBuilder()
                                .setType(Type.OBJECT)
                                .putProperties("hmsnr", Schema.newBuilder().setType(Type.STRING).build())
                                .addAllRequired(listOf("hmsnr"))
                                .build()
                        )
                        .build()
                )
                .build()
            val model = GenerativeModel(config.model, vertexAI)
                .withGenerationConfig(generationConfig)
                .withSystemInstruction(ContentMaker.fromString(instruction))
            val response = model.generateContent(prompt)
            val output: String = ResponseHandler.getText(response)
            LOG.debug("Got response: $output")
            return objectMapper.readValue(output,object : TypeReference<List<HmsNr>>() {} )
        }
    }
    companion object {
        private val LOG = LoggerFactory.getLogger(CompatibleAIFinder::class.java)
    }
}

@ConfigurationProperties("vertexai")
open class VertexAIConfig {
    var model: String = "gemini-2.0-flash-001"
    var location: String = "europe-north1"
    var project: String = "teamdigihot-dev-9705"
    var temperature: Float = 0.0f
}

@Introspected
data class HmsNr(
    val hmsnr: String
)