package org.mockserver.client.netty

import io.netty.handler.codec.http.HttpHeaders
import org.mockserver.configuration.ConfigurationProperties
import org.mockserver.echo.http.EchoServer
import org.mockserver.model.HttpResponse
import org.mockserver.socket.PortFactory
import spock.lang.Ignore
import spock.lang.Specification

import static io.netty.handler.codec.http.HttpHeaders.Names.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.Is.is
import static org.mockserver.model.Header.header
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.mockserver.model.OutboundHttpRequest.outboundRequest
import static org.mockserver.model.StringBody.exact

class NettyHttpClientErrorHandlingTest extends Specification {

    private int freePort

    def setup() {
        freePort = PortFactory.findFreePort()
    }

    def "should throw SocketCommunicationException for ConnectException"() {
        when:
        new NettyHttpClient().sendRequest(outboundRequest("127.0.0.1", freePort, "", request()))

        then:
        SocketConnectionException e = thrown()
        e.message.contains("Unable to connect to socket /127.0.0.1:" + freePort)
    }

    @Ignore
    def "should handle connection closure"() {
        given:
        EchoServer echoServer = new EchoServer(freePort, true, EchoServer.Error.CLOSE_CONNECTION)

        when:
        new NettyHttpClient().sendRequest(outboundRequest("127.0.0.1", freePort, "", request()).withSsl(true))

        then:
        RuntimeException e = thrown()
        e.message.contains("Connection reset by peer")

        cleanup:
        echoServer.stop()
    }

    def "should handle LargerContentLengthHeader"() {
        given:
        EchoServer echoServer = new EchoServer(freePort, true, EchoServer.Error.LARGER_CONTENT_LENGTH)
        long originalMaxSocketTimeout = ConfigurationProperties.maxSocketTimeout()

        ConfigurationProperties.maxSocketTimeout(5)

        when:
        new NettyHttpClient().sendRequest(outboundRequest("127.0.0.1", freePort, "", request().withBody(exact("this is an example body"))).withSsl(true))

        then:
        SocketCommunicationException e = thrown()
        e.message.contains("Response was not received after 5 milliseconds, to make the proxy wait longer please use \"mockserver.maxSocketTimeout\" system property or ConfigurationProperties.maxSocketTimeout(long milliseconds)")

        cleanup:
        echoServer.stop()
        ConfigurationProperties.maxSocketTimeout(originalMaxSocketTimeout)
    }

    def "should handle SmallerContentLengthHeader"() {
        given:
        EchoServer echoServer = new EchoServer(freePort, true, EchoServer.Error.SMALLER_CONTENT_LENGTH)

        when:
        InetSocketAddress socket = new InetSocketAddress("127.0.0.1", freePort)
        HttpResponse httpResponse = new NettyHttpClient().sendRequest(outboundRequest(socket, "", request().withBody(exact("this is an example body"))).withSsl(true))


        then:
        assertThat(httpResponse, is(
                response()
                    .withStatusCode(200)
                    .withHeader(header(HOST, socket.getHostName() + ":" + freePort))
                    .withHeader(header(CONTENT_LENGTH, "this is an example body".length().intdiv(2)))
                    .withHeader(header(ACCEPT_ENCODING, HttpHeaders.Values.GZIP + "," + HttpHeaders.Values.DEFLATE))
                    .withHeader(header(CONNECTION, HttpHeaders.Values.KEEP_ALIVE))
                    .withBody(exact("this is an "))
                )
        )

        cleanup:
        echoServer.stop()
    }

}