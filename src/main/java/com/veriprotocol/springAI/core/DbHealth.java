package com.veriprotocol.springAI.core;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DbHealth {

    private final URI dbUri;

    public DbHealth(@Value("${spring.datasource.url}") String jdbcUrl) {
        // remove "jdbc:" so URI can parse it
        this.dbUri = URI.create(jdbcUrl.substring(5));
    }

    public boolean isDbUp() {
        try (Socket socket = new Socket()) {
            socket.connect(
                new InetSocketAddress(dbUri.getHost(), dbUri.getPort()),
                200
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
