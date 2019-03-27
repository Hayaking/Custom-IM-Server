package bean;

import utils.Utils;
import utils.encryption.MD5Util;

import java.security.interfaces.RSAPublicKey;

public class Message {
    public static Message Accept(String account, String to) {
        return new Message()
                .setAccount(account)
                .setTo(to)
                .setType(TYPE.ACCEPT);
    }

    public static Message Accepted() {
        return new Message()
                .setType(TYPE.ACCEPTED);
    }

    public static Message Add(String account, String to) {
        return new Message()
                .setAccount(account)
                .setTo(to)
                .setType(TYPE.ADD);
    }

    public static Message Login(String account, String psw) {
        return new Message()
                .setAccount(account)
                .setPassword(MD5Util.MD5(psw).getBytes())
                .setType(TYPE.LOGIN);
    }

    public static Message Sign(String account, String psw) {
        return new Message()
                .setAccount(account)
                .setPassword(MD5Util.MD5(psw).getBytes())
                .setType(TYPE.SIGN);
    }

    public static Message Offline(String account) {
        return new Message()
                .setAccount(account)
                .setType(TYPE.OFFLINE);
    }

    public static Message Online(String account) {
        return new Message()
                .setAccount(account)
                .setType(TYPE.ONLINE);
    }

    public static Message QuerryUser(String target) {
        return new Message()
                .setText(target)
                .setType(TYPE.GET_USER);
    }

    public static Message RequireContact(String account) {
        return new Message()
                .setAccount(account)
                .setType(TYPE.GET_CONTACT);
    }

    public static Message Self(String account, String text) {
        return new Message()
                .setAccount(account)
                .setText(text)
                .setType(TYPE.SEND_SELF);
    }

    public static Message Send(String account, String text, String to) {
        return new Message()
                .setAccount(account)
                .setText(text)
                .setTo(to)
                .setType(TYPE.SEND);
    }

    public static Message RSAPublicKey(RSAPublicKey key) {
        return new Message()
                .setBuf(Utils.o2b(key))
                .setType(TYPE.GOT_RSA_PUBLICKEY);
    }

    public static Message GetRSAPublicKey() {
        return new Message()
                .setType(TYPE.GET_RSA_PUBLICKEY);
    }

    private String id;
    private String account;
    private byte[] password;
    //    private String password;
    private String to;
    private String text;
    private byte[] buf;
    private TYPE type;

    public Message() {
    }

    public Message(String contact, String type, String body) {
        this.account = contact;
        this.text = body;
        if ("SEND".equals(type)) {
            this.type = TYPE.SEND;
        } else {
            this.type = TYPE.RECEIVED;
        }
    }

    public byte[] getPassword() {
        return password;
    }

    public Message setPassword(byte[] password) {
        this.password = password;
        return this;
    }

    public String getAccount() {
        return account;
    }

    public Message setAccount(String account) {
        this.account = account;
        return this;
    }

    public byte[] getBuf() {
        return buf;
    }

    public Message setBuf(byte[] buf) {
        this.buf = buf;
        return this;
    }

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }


    public String getText() {
        return text;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Message setTo(String to) {
        this.to = to;
        return this;
    }

    public TYPE getType() {
        return type;
    }

    public Message setType(TYPE type) {
        this.type = type;
        return this;
    }

    public enum TYPE {
        RECEIVED, SEND,
        GET_USER, GOT_USER,
        LOGIN, LOGIN_SUCCESS, LOGIN_FAILED,
        SIGN, SIGN_SUCCESS,
        GET_CONTACT, GOT_CONTACT,
        ADD,
        ACCEPT, ACCEPTED,
        OFFLINE, ONLINE,
        SEND_SELF, RECEIVED_SELF,
        GET_RSA_PUBLICKEY, GOT_RSA_PUBLICKEY
    }
}
