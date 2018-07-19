package com.still.Utils;

public class MessageDigest {
    public static String Md5(String s){
        byte[] bs=null;
        try {
            bs = java.security.MessageDigest.getInstance("MD5").digest(s.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
        StringBuilder buf = new StringBuilder(bs.length * 2);
        for(byte b : bs) {
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }
}
