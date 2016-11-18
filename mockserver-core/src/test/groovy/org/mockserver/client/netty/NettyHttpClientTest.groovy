package org.mockserver.client.netty

import io.netty.handler.codec.http.HttpHeaders
import org.mockserver.echo.http.EchoServer
import org.mockserver.model.HttpResponse
import org.mockserver.socket.PortFactory
import spock.lang.Specification

import static io.netty.handler.codec.http.HttpHeaders.Names.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.Is.is
import static org.mockserver.model.Cookie.cookie
import static org.mockserver.model.Header.header
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.mockserver.model.OutboundHttpRequest.outboundRequest
import static org.mockserver.model.StringBody.exact

public class NettyHttpClientTest extends Specification {

    private static EchoServer echoServer
    private static int freePort

    def setupSpec() {
        freePort = PortFactory.findFreePort()
        echoServer = new EchoServer(freePort, false)
    }

    def cleanupSpec() {
        echoServer.stop()
    }

    def "should send basic request"() {
        given:
        NettyHttpClient nettyHttpClient = new NettyHttpClient()

        when:
        HttpResponse httpResponse = nettyHttpClient.sendRequest(outboundRequest("0.0.0.0", freePort, "", request()))

        then:
        assertThat(httpResponse, is(
                response()
                        .withStatusCode(200)
                        .withHeader(header(HOST, "0.0.0.0:" + freePort))
                        .withHeader(header(CONTENT_LENGTH, 0))
                        .withHeader(header(ACCEPT_ENCODING, HttpHeaders.Values.GZIP + "," + HttpHeaders.Values.DEFLATE))
                        .withHeader(header(CONNECTION, HttpHeaders.Values.KEEP_ALIVE))
        ))
    }

    def "should send complex request"() {
        given:
        NettyHttpClient nettyHttpClient = new NettyHttpClient()

        when:
        HttpResponse httpResponse = nettyHttpClient.sendRequest(outboundRequest("0.0.0.0", freePort, "",
                request()
                        .withHeader(header("some_header_name", "some_header_value"))
                        .withHeader(header("another_header_name", "first_header_value", "second_header_value"))
                        .withCookie(cookie("some_cookie_name", "some_cookie_value"))
                        .withCookie(cookie("another_cookie_name", "another_cookie_value"))
                        .withBody(exact("this is an example body"))
        ))

        then:
        assertThat(httpResponse, is(
                response()
                        .withStatusCode(200)
                        .withHeader(header(HOST, "0.0.0.0:" + freePort))
                        .withHeader(header(CONTENT_LENGTH, "this is an example body".length()))
                        .withHeader(header(ACCEPT_ENCODING, HttpHeaders.Values.GZIP + "," + HttpHeaders.Values.DEFLATE))
                        .withHeader(header(CONNECTION, HttpHeaders.Values.KEEP_ALIVE))
                        .withHeader(header(COOKIE, "some_cookie_name=some_cookie_value; another_cookie_name=another_cookie_value"))
                        .withHeader(header("some_header_name", "some_header_value"))
                        .withHeader(header("another_header_name", "first_header_value", "second_header_value"))
                        .withCookie(cookie("some_cookie_name", "some_cookie_value"))
                        .withCookie(cookie("another_cookie_name", "another_cookie_value"))
                        .withBody(exact("this is an example body"))
        ))
    }

}