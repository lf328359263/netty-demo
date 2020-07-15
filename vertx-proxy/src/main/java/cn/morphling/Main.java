package cn.morphling;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetClientImpl;
import io.vertx.core.streams.Pump;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class Main {

    static NetServer netServer = Vertx.vertx().createNetServer();
    static NetClient netClient = Vertx.vertx().createNetClient();

    public static boolean connect(NetSocket clientSocket, String remoteHost, int port) {
        AtomicBoolean flag = new AtomicBoolean(false);
        netClient.connect(port, remoteHost, result -> {
            NetSocket proxySocket = null;
            if (result.succeeded()) {
                proxySocket = result.result();
                log.info("代理连接成功 {}:{} ", remoteHost, port);
                clientSocket.handler(proxySocket::write);
                proxySocket.handler(clientSocket::write);
//                Pump.pump(clientSocket, proxySocket).start();
//                Pump.pump(proxySocket, clientSocket).start();
                proxySocket.closeHandler(event -> log.info("代理连接关闭 {}:{}", remoteHost, port));
                flag.set(true);
            } else {
                log.error("代理连接失败 {}:{}", remoteHost, port);
            }
            if (proxySocket != null) {
                if (!flag.get()) {
                    proxySocket.close();
                }
            }
        });
        return flag.get();
    }

    public static void main(String[] args) {

        String[] remoteHosts = {"114.67.234.213", "114.67.234.214"};
        netServer.connectHandler(clientSocket -> {
            log.info("客户端 {}:{} 创建连接", clientSocket.remoteAddress().host(), clientSocket.remoteAddress().port());
            for (String remoteHost : remoteHosts) {
                System.out.println(remoteHost);
                boolean connect = connect(clientSocket, remoteHost, 3306);
                if (connect) break;
            }
        });
        log.info("开始监听 8088");
        netServer.listen(8088);
    }

}
