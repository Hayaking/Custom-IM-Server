package dao;

import bean.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.security.interfaces.RSAPublicKey;

public interface ISender {
    void sendLoginOrSign(Message message, DatagramPacket fromPacket) throws Exception;

    void sendPacket(Message message, DatagramPacket fromPacket) throws IOException;

    void sendPacket(Message message, String ip, int port) throws IOException;

    void sendBytes(byte[] bytes, String ip, int port) throws IOException;

    void sendRSAPublicKey(RSAPublicKey key, DatagramPacket fromPacket) throws IOException;
}
