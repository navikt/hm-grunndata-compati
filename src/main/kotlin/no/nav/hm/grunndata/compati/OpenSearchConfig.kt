package no.nav.hm.grunndata.compati

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.opensearch.client.RestClient
import org.opensearch.client.json.jackson.JacksonJsonpMapper
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.transport.rest_client.RestClientTransport
import org.slf4j.LoggerFactory


@Factory
class OpenSearchConfig(private val openSearchEnv: OpenSearchEnv, private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpenSearchConfig::class.java)
    }

    @Singleton
    fun buildOpenSearchClient(restClient: RestClient): OpenSearchClient {
        val host = HttpHost.create(openSearchEnv.url)
        val credentialsProvider: BasicCredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope(host),
            UsernamePasswordCredentials(openSearchEnv.user, openSearchEnv.password)
        )
        val transport = RestClientTransport(restClient, JacksonJsonpMapper(objectMapper))
        val client = OpenSearchClient(transport)
        LOG.info("Opensearch client using ${openSearchEnv.user} and url ${openSearchEnv.url}")
        return client
    }

    @Singleton
    fun buildOpenSearchRestClient(): RestClient {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials(openSearchEnv.user, openSearchEnv.password)
        )
        val client = RestClient.builder(HttpHost.create(openSearchEnv.url))
            .setRequestConfigCallback { it
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(1000)
                .setSocketTimeout(20000)
            }
            .setHttpClientConfigCallback {
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                if ("https://localhost:9200" == openSearchEnv.url && "admin" == openSearchEnv.user) {
                    LOG.info("Using dev settings for ${openSearchEnv.url}")
                    devAndTestSettings(httpClientBuilder)
                }
                httpClientBuilder
                    .setMaxConnTotal(128)
                    .setMaxConnPerRoute(128)
            }.build()
        LOG.info("Opensearch client using ${openSearchEnv.user} and url ${openSearchEnv.url}")
        return client
    }

    private fun devAndTestSettings(httpClientBuilder: HttpAsyncClientBuilder) {
        httpClientBuilder.setSSLHostnameVerifier { _, _ -> true }
        val context = SSLContext.getInstance("SSL")
        context.init(null, arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

        }), SecureRandom())
        httpClientBuilder.setSSLContext(context)
    }
}

@ConfigurationProperties("opensearch")
open class OpenSearchEnv {
    var user: String = "admin"
    var password: String = "admin"
    var url: String = "https://localhost:9200"
}
