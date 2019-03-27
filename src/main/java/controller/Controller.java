package controller;

import bean.Message;
import bean.User;
import com.google.gson.Gson;
import dao.IController;
import utils.DBUtil;
import utils.Utils;
import utils.encryption.RSAUtil;

import java.io.IOException;
import java.net.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Controller implements IController {
    private static Controller instence;
    private DatagramSocket socket;
    private Sender sender;
    private static RSAPublicKey rsaPublicKey;
    private static RSAPrivateKey rsaPrivateKey;

    /** 创建指定端口的数据包socket；
     ** 创建发送器
     * @param port
     * @throws SocketException
     */
    private Controller(int port) throws SocketException {
        socket = new DatagramSocket(port);
        sender = new Sender(socket);
    }

    /**得到公钥和私钥；
     * 单例模式创建一个Controller
     * @return
     * @throws Exception
     */
    public static Controller getInstence() throws Exception {
        if (null == instence) {
            Map<String, Object> keyMap = RSAUtil.initKey();
            rsaPublicKey = RSAUtil.getpublicKey(keyMap);
            rsaPrivateKey = RSAUtil.getPrivateKey(keyMap);
            instence = new Controller(9898);
        }
        return instence;
    }

    /**发送RSA私钥
     * @param packet
     */
    public void sendRSAPublicKey(DatagramPacket packet) {
        try {
            Message message = Message.RSAPublicKey(rsaPublicKey);
            sender.sendPacket(message,packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**建立id1与id2的好友关系；
     * 通知双方添加成功
     * @param acceptMsg
     * @param packet
     */
    public void toAccept(Message acceptMsg, DatagramPacket packet) {
        System.out.println(acceptMsg.getTo());
        System.out.println(acceptMsg.getAccount());
        String id1 = getIdByName(acceptMsg.getTo());
        String id2 = getIdByName(acceptMsg.getAccount());
        boolean flag1 = buildRelation(id1, id2);
        boolean flag2 = buildRelation(id2, id1);
        if (flag1 && flag2) {
            String[] ipAndPortByName = getIpAndPortByName(acceptMsg.getTo());
            String[] ipAndPortByName2 = getIpAndPortByName(acceptMsg.getAccount());
            try {
                sender.sendPacket(Message.Accepted(), ipAndPortByName[0], Integer.parseInt(ipAndPortByName[1]));
                sender.sendPacket(Message.Accepted(), ipAndPortByName2[0], Integer.parseInt(ipAndPortByName2[1]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**建立id1与id2的好友关系
     * @param id1
     * @param id2
     * @return
     */
    public boolean buildRelation(String id1, String id2) {
        Connection con = null;
        PreparedStatement prst = null;
        String sql = "insert into relation values (?,?,?,?)";
        try {
            con = DBUtil.getConnection();
            prst = con.prepareStatement(sql);
            prst.setString(1, Utils.getSha1(id1 + id2));
            prst.setString(2, id1);
            prst.setString(3, id2);
            prst.setString(4, "默认");
            prst.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, null);
        }
        return true;
    }


    /**向目标用户发送添加好友请求
     * @param addMsg
     * @param fromPacket
     */
    public void toAdd(Message addMsg, DatagramPacket fromPacket) {
        //获得目标的ip，port
        String to = addMsg.getTo();
        String[] ipAndPort = getIpAndPortByName(to);
        String ip = ipAndPort[0];
        int port = Integer.parseInt(ipAndPort[1]);
        addMsg.setType(Message.TYPE.ADD);
        System.out.println(ip + "::" + port);
        if (null != ip && 0 != port) {
            try {
                sender.sendPacket(addMsg, ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送联系人hashmap
     * @param msg
     * @param fromPacket
     */
    public void sendContact(Message msg, DatagramPacket fromPacket) {
        //组名：{id:昵称}
        HashMap<String, LinkedList<String>> hashMap = new HashMap<>();
        Connection con = null;
        PreparedStatement prst = null;
        ResultSet set = null;
        String name, group;
        try {
            con = DBUtil.getConnection();
            String sql = "select user.name, relation._group from relation " +
                    "join user on relation.friend_id = user._id where relation.self_id = ?";
            prst = con.prepareStatement(sql);
            prst.setString(1, getIdByName(msg.getAccount()));

            set = prst.executeQuery();
            while (set.next()) {
                name = set.getString(1);
                group = set.getString(2);
                if (!hashMap.containsKey(group)) {
                    LinkedList<String> list = new LinkedList<>();
                    list.add(name);
                    hashMap.put(group, list);
                } else {
                    hashMap.get(group).add(name);
                }
            }
            //将hashmap转换为byte数组
            byte[] bytes = Utils.o2b(hashMap);
            Message message = new Message();
            message.setType(Message.TYPE.GOT_CONTACT);
            message.setBuf(bytes);
            sender.sendPacket(message, fromPacket);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, set);
        }
    }

    /**根据id查找name
     * @param name
     * @return
     */
    public String getIdByName(String name) {
        Connection con = null;
        PreparedStatement prst = null;
        ResultSet set = null;
        String id = null;
        try {
            con = DBUtil.getConnection();
            String sql = "select _id from user where name = ?";
            prst = con.prepareStatement(sql);
            prst.setString(1, name);
            set = prst.executeQuery();
            id = null;
            while (set.next())
                id = set.getString(1);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, set);
        }
        return id;
    }

    /**
     * 登录
     *
     * @param loginMsg
     * @param fromPacket
     */
    public void toLogin(Message loginMsg, DatagramPacket fromPacket) {
        Connection con = null;
        PreparedStatement prst = null;
        ResultSet set = null;
        String sql = "select * from user where name = ? and psw = ?";
        String name = loginMsg.getAccount();
        String ip = fromPacket.getAddress().getHostAddress();
        int port = fromPacket.getPort();

        try {
            byte[] buf = RSAUtil.decrypt(loginMsg.getPassword(), rsaPrivateKey);
            String psw = new String(buf, 0, buf.length);
            boolean flag = false;
            con = DBUtil.getConnection();
            prst = con.prepareStatement(sql);
            prst.setString(1, name);
            prst.setString(2, psw);
            set = prst.executeQuery();
            while (set.next()) {
                flag = true;
            }
            Message message = new Message();
            if (flag) {
                //登录成功
                message.setType(Message.TYPE.LOGIN_SUCCESS);
                String updateSql = "update user set ip = ?, port = ? where name = ?";
                prst = con.prepareStatement(updateSql);
                prst.setString(1, ip);
                prst.setInt(2, port);
                prst.setString(3, name);
                prst.executeUpdate();
            } else {
                //登录失败
                message.setType(Message.TYPE.LOGIN_FAILED);
            }
            sender.sendPacket(message, fromPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, set);
        }
    }

    /**转发消息
     * @param routMsg
     * @param fromPacket
     */
    public void routSend(Message routMsg, DatagramPacket fromPacket) {
        String to = routMsg.getTo();
        String[] ipAndPortByName = getIpAndPortByName(to);
        String ip = ipAndPortByName[0];
        int port = Integer.parseInt(ipAndPortByName[1]);
        routMsg.setType(Message.TYPE.RECEIVED);
        if (null != ip && 0 != port) {
            try {
                sender.sendPacket(routMsg, ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**自己跟自己聊天
     * @param routMsg
     * @param fromPacket
     */
    public void routSelf(Message routMsg, DatagramPacket fromPacket) {
        routMsg.setType(Message.TYPE.RECEIVED_SELF);
        try {
            sender.sendPacket(routMsg, fromPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**注册
     * @param signMsg
     * @param fromPacket
     */
    public void toSign(Message signMsg, DatagramPacket fromPacket) {
        Connection con = null;
        PreparedStatement prst = null;
        try {
            con = DBUtil.getConnection();
            String account = signMsg.getAccount();
            System.out.println(Arrays.toString(signMsg.getPassword()));
            byte[] buf = RSAUtil.decrypt(signMsg.getPassword(), rsaPrivateKey);
            String password = new String(buf, 0, buf.length);
            System.out.println(password);

            String sql = "insert into user(_id,name,psw,role) values(?,?,?,?)";
            prst = con.prepareStatement(sql);
            prst.setString(1, Utils.getSha1(account + password));
            prst.setString(2, account);
            prst.setString(3, password);
            prst.setInt(4,0);
            int i = prst.executeUpdate();
            //注册成功
            if (0 != i) {
                Message message = new Message()
                        .setType(Message.TYPE.SIGN_SUCCESS);
//                sender.sendLoginOrSign(message,fromPacket);
                sender.sendPacket(message, fromPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, null);

        }
    }

    /**根据名字获取user对象
     * @param getMessage
     * @param fromPacket
     */
    public void getUser(Message getMessage, DatagramPacket fromPacket) {
        Connection con = null;
        PreparedStatement prst = null;
        ResultSet set = null;
        String sql = "select * from user where name = ? ";
        User user = null;
        try {
            con = DBUtil.getConnection();
            prst = con.prepareStatement(sql);
            prst.setString(1, getMessage.getText());
            set = prst.executeQuery();
            while (set.next()) {
                String id = set.getString(1);
                String name = set.getString(2);
                user = new User(id, name);
            }
            Message message = new Message();
            Gson gson = new Gson();
            message.setText(gson.toJson(user));
            message.setType(Message.TYPE.GOT_USER);
            sender.sendPacket(message, fromPacket);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, set);
        }
    }

    /**下线
     * @param message
     * @param fromPacket
     */
    @Override
    public void toOffline(Message message, DatagramPacket fromPacket) {
        Connection con = null;
        PreparedStatement prst = null;
        ResultSet set = null;
        String sql = "update user set ip = null , port = null where name = ? ";
        try {
            con = DBUtil.getConnection();
            prst = con.prepareStatement(sql);
            prst.setString(1, message.getAccount());
            int i = prst.executeUpdate();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, set);
        }
    }


    /**根据名字查找ip和端口
     * @param name
     * @return
     */
    public String[] getIpAndPortByName(String name) {
        Connection con = null;
        PreparedStatement prst = null;
        ResultSet resultSet = null;
        String sql = "select ip,port from user where name = ?";
        String ip = null;
        int port = 0;
        try {

            con = DBUtil.getConnection();
            prst = con.prepareStatement(sql);
            prst.setString(1, name);
            resultSet = prst.executeQuery();
            while (resultSet.next()) {
                ip = resultSet.getString(1);
                port = resultSet.getInt(2);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(con, prst, null);
        }
        return new String[]{ip, port + ""};
    }


    public DatagramSocket getSocket() {
        return socket;
    }
}
