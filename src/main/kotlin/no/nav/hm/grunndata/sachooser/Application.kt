package no.nav.hm.grunndata.sachooser

import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.sachooser")
            .mainClass(Application.javaClass)
            .start()
    }
}