import bean.Message;
import com.google.gson.Gson;
import controller.Controller;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;


public class Server {
    private static Controller instence;
    private HashMap<String, String> poll;

    /**多线程接收，这里只开了一个
     * @param args
     */
    public static void main(String[] args) {
        try {
            instence = Controller.getInstence();
            Thread get = new Thread(() -> {
                while (true) {
                    try {
                        get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            get.start();
            get.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**获得bute[]->json->message;
     * 再根据message的type区别对待
     * @throws IOException
     */
    private static void get() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        instence.getSocket().receive(packet);
        int len = Utils.returnActualLength(buf);
        buf = Utils.getArrayByte(buf, len);
        String json = new String(buf, 0, buf.length);
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);
        System.out.println(json);
        switch (message.getType()) {
            case ONLINE:
                System.out.println("{name: " + message.getAccount() + ", ip: " + packet.getAddress().getHostAddress() + " } 上线了");
                break;
            case OFFLINE:
                instence.toOffline(message, packet);
                break;
            case SEND:
                instence.routSend(message, packet);
                break;
            case SEND_SELF:
                instence.routSelf(message, packet);
                break;
            case LOGIN:
                instence.toLogin(message, packet);
                break;
            case SIGN:
                instence.toSign(message, packet);
                break;
            case GET_CONTACT:
                instence.sendContact(message, packet);
                break;
            case GET_USER:
                instence.getUser(message, packet);
                break;
            case ADD:
                instence.toAdd(message, packet);
                break;
            case ACCEPT:
                instence.toAccept(message, packet);
            case GET_RSA_PUBLICKEY:
                instence.sendRSAPublicKey(packet);
        }
    }


}
