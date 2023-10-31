package org.oopscraft.fintics.collector;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MarketIndiceClientTest {

    @Disabled
    @Test
    void test() throws Exception {
        String wsUrl = "wss://ws.postman-echo.com/raw";
        wsUrl= "wss://streaming.forexpros.com/echo/245/p511pwid/websocket";
        URI uri = new URI(wsUrl);
        WebSocketClient webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("== onOpen: {}", handshakedata);
            }

            @Override
            public void onMessage(String message) {
                System.out.println("== onMessage: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("== onClose");
            }

            @Override
            public void onError(Exception ex) {
                log.info("== onError: {}", ex.getMessage());
            }
        };

        webSocketClient.connectBlocking();

        webSocketClient.send("[\"{\\\"_event\\\":\\\"bulk-subscribe\\\",\\\"tzID\\\":8,\\\"message\\\":\\\"pid-0:%%pid-1175152:%%isOpenExch-152:%%pid-1175153:%%pid-169:%%isOpenExch-1:%%pid-166:%%pid-14958:%%isOpenExch-2:%%pid-44336:%%isOpenExch-97:%%pid-8827:%%isOpenExch-1004:%%pid-8849:%%pid-8833:%%pid-8862:%%pid-8830:%%pid-8836:%%pid-8831:%%pid-8916:%%pid-6408:%%pid-6369:%%pid-13994:%%pid-6435:%%pid-26490:%%pid-6497:%%pid-941155:%%pid-8832:%%pid-20:%%pid-172:%%isOpenExch-4:%%pid-27:%%isOpenExch-3:%%pid-167:%%isOpenExch-9:%%pid-178:%%isOpenExch-20:%%pid-1:%%isOpenExch-1002:%%pid-2:%%pid-3:%%pid-5:%%pid-7:%%pid-9:%%pid-10:%%pid-23705:%%pid-23706:%%pid-23703:%%pid-23698:%%pid-8880:%%isOpenExch-118:%%pid-8895:%%pid-1141794:%%pid-13063:%%pid-243:%%pid-940817:%%isOpenExch-NaN:%%pid-7870:%%pid-21027:%%pid-13842:%%pid-247:%%pid-103925:%%pid-19696:%%pid-1198295:%%pid-16662:%%pid-15951:%%pid-525:%%pid-14211:%%pid-45279:%%pid-14175:%%pid-14174:%%pid-14220:%%pid-14210:%%pid-38165:%%pid-255:%%pid-8274:%%pid-7989:%%pid-1131597:%%pid-1202649:%%isOpenExch-47:%%pid-8839:%%pidExt-166:%%cmt-1-5-166:%%pid-942611:%%pid-19155:%%pid-179:\\\"}\"]");

        Thread.sleep(10_000);
//        for(int i = 0; i < 10; i ++) {
//            Thread.sleep(1_000);
//            webSocketClient.send("[\"{ \\\"_event\\\": \\\"heartbeat\\\", \\\"data\\\": \\\"h\\\"}\"]");
//            webSocketClient.send("[\"{\\\"_event\\\":\\\"bulk-subscribe\\\",\\\"tzID\\\":8,\\\"message\\\":\\\"pid-0:%%pid-1175152:%%isOpenExch-152:%%pid-1175153:%%pid-169:%%isOpenExch-1:%%pid-166:%%pid-14958:%%isOpenExch-2:%%pid-44336:%%isOpenExch-97:%%pid-8827:%%isOpenExch-1004:%%pid-8849:%%pid-8833:%%pid-8862:%%pid-8830:%%pid-8836:%%pid-8831:%%pid-8916:%%pid-6408:%%pid-6369:%%pid-13994:%%pid-6435:%%pid-26490:%%pid-6497:%%pid-941155:%%pid-8832:%%pid-20:%%pid-172:%%isOpenExch-4:%%pid-27:%%isOpenExch-3:%%pid-167:%%isOpenExch-9:%%pid-178:%%isOpenExch-20:%%pid-1:%%isOpenExch-1002:%%pid-2:%%pid-3:%%pid-5:%%pid-7:%%pid-9:%%pid-10:%%pid-23705:%%pid-23706:%%pid-23703:%%pid-23698:%%pid-8880:%%isOpenExch-118:%%pid-8895:%%pid-1141794:%%pid-13063:%%pid-243:%%pid-940817:%%isOpenExch-NaN:%%pid-7870:%%pid-21027:%%pid-13842:%%pid-247:%%pid-103925:%%pid-19696:%%pid-1198295:%%pid-16662:%%pid-15951:%%pid-525:%%pid-14211:%%pid-45279:%%pid-14175:%%pid-14174:%%pid-14220:%%pid-14210:%%pid-38165:%%pid-255:%%pid-8274:%%pid-7989:%%pid-1131597:%%pid-1202649:%%isOpenExch-47:%%pid-8839:%%pidExt-166:%%cmt-1-5-166:%%pid-942611:%%pid-19155:%%pid-179:\\\"}\"]");
//        }

        webSocketClient.closeBlocking();

    }

}