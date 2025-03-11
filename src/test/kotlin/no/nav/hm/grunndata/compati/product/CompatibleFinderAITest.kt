package no.nav.hm.grunndata.compati.product

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.compati.product.compatible.CompatibleAIFinder
import org.junit.jupiter.api.Test

@MicronautTest
class CompatibleFinderAITest(private val compatibleAIFinder: CompatibleAIFinder) {

    @Test
    fun compatibleFinderTest() {
        val partsTitle = "Vippestuss mrs Nitrum/Argon2/Simba/Youngster 3 hø/ve"
        val mainProducts: List<HmsNrTitlePair> = listOf(
            HmsNrTitlePair("142352", "Mrs bak allr Emineo sb33 sd34-40 sh40-48 sete vreg rygg vreg"),
            HmsNrTitlePair("343351", "Mrs bak allr Youngster3 sb22-24 sd24-28 sh39-41 sete vreg rygg vreg"),
            HmsNrTitlePair("234152", "Mrs bak stå Levo Summit El sb35 sd35-43 sh48-54 rygg vreg"),
            HmsNrTitlePair("043253", "Mrs bak akt Argon 2 sb30 sd32 sh46-49 sete vreg rygg vreg"),
            HmsNrTitlePair("332146", "Mrs bak akt Nitrum sb32 sd34 sh44-47 sete vreg rygg vreg"),
            HmsNrTitlePair("541254", "Mrs bak akt QS5 X sb34 sd34 sh43-46 sete vreg rygg vreg"),
            HmsNrTitlePair("442341", "Mrs bak akt Simba sb22-24 sd24-28 sh37-40 sete vreg rygg vreg"),
            HmsNrTitlePair("341260", "Mrs bak allr Exigo 30 sb36 sd33-39 sh40-48 sete vreg rygg vreg"),
            HmsNrTitlePair("813561", "Mrs bak komf Cirrus G5 sb39 sd39-55 sh48 sete vreg rygg vreg 2024")
        )
        println(compatibleAIFinder.generatePrompt(partsTitle, mainProducts))
    }
}