package no.nav.hm.grunndata.compati

import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.compati")
            .mainClass(Application.javaClass)
            .start()
    }
}