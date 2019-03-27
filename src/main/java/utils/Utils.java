package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;

public class Utils {
    public static int returnActualLength(byte[] data) {
        int i = 0;
        for (; i < data.length; i++) {
            if (data[i] == '\0')
                break;
        }
        return i;
    }

    public static byte[] getArrayByte(byte[] buf, int len) {
        byte[] bytes = new byte[len];
        System.arraycopy(buf, 0, bytes, 0, len);
        return bytes;
    }

    public static String getSha1(String str) {

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));
            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

//    public static Object b2o(byte[] buffer) {
//        Object obj = null;
//        try {
//            ByteArrayInputStream buffers = new ByteArrayInputStream(buffer);
//            ObjectInputStream in = new ObjectInputStream(buffers);
//            obj = in.readObject();
//            in.close();
//        } catch (Exception e) {
//            System.out.println("error");
//        }
//        return obj;
//    }

    public static byte[] o2b(Object s) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bos.toByteArray();
    }
}