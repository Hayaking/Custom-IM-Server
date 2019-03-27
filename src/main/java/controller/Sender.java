package controller;

import bean.Message;
import com.google.gson.Gson;
import dao.ISender;
import utils.encryption.RSAUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.interfaces.RSAPublicKey;

public class Sender implements ISender {
    private DatagramSocket socket;

    public Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void sendLoginOrSign(Message message, DatagramPacket fromPacket) throws Exception {
//        Gson gson = new Gson();
//        String json = gson.toJson(message);
//        byte[] rsaResult = RSAUtil.encrypt(json.getBytes(), rsaPublicKey);
//        sendBytes(rsaResult,fromPacket.getAddress().getHostAddress(),fromPacket.getPort());
    }

    /** 直接调用sendPacket(Message,ip,port)
     * @param message
     * @param fromPacket
     * @throws IOException
     */
    @Override
    public void sendPacket(Message message, DatagramPacket fromPacket) throws IOException {
        sendPacket(message, fromPacket.getAddress().getHostAddress(), fromPacket.getPort());
    }

    /**message->json->byte[],将得到的byte[]穿给sendBytes(byte[],ip,port)
     * @param message
     * @param ip
     * @param port
     * @throws IOException
     */
    @Override
    public void sendPacket(Message message, String ip, int port) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(message);
        //加密
        byte[] bytes = json.getBytes();
        sendBytes(bytes, ip, port);
    }

    /**发送byte[]到指定的ip::port
     * @param bytes
     * @param ip
     * @param port
     * @throws IOException
     */
    @Override
    public void sendBytes(byte[] bytes, String ip, int port) throws IOException {
        System.out.println(bytes);
        System.out.println(ip + "::" + port);
        DatagramPacket packet = new DatagramPacket(
                bytes,
                bytes.length,
                InetAddress.getByName(ip),
                port
        );
        socket.send(packet);
    }


    /** 发送RSA的私钥
     * @param key
     * @param fromPacket
     * @throws IOException
     */
    @Override
    public void sendRSAPublicKey(RSAPublicKey key, DatagramPacket fromPacket) throws IOException {
        Message message = Message.RSAPublicKey(key);
        Gson gson = new Gson();
        String json = gson.toJson(message);
        sendBytes(json.getBytes(), fromPacket.getAddress().getHostAddress(), fromPacket.getPort());
    }
}
