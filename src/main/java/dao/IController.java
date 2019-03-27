package dao;

import bean.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public interface IController {

    void toAccept(Message acceptMsg, DatagramPacket packet);

    void toAdd(Message addMsg, DatagramPacket fromPacket);

    void sendContact(Message msg, DatagramPacket fromPacket);

    void toLogin(Message loginMsg, DatagramPacket fromPacket);

    void routSend(Message routMsg, DatagramPacket fromPacket);

    void toSign(Message signMsg, DatagramPacket fromPacket);

    void getUser(Message getMessage, DatagramPacket fromPacket);

    void toOffline(Message message, DatagramPacket fromPacket);

    String[] getIpAndPortByName(String name);

    String getIdByName(String name);

    boolean buildRelation(String id1, String id2);

    DatagramSocket getSocket();
}
