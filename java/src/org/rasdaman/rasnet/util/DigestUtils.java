package org.rasdaman.rasnet.util;

import org.rasdaman.rasnet.exception.RasnetException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

    public static String MD5(String message) {

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RasnetException("MD5 encryption not available.", e);
        }
        byte[] messageBytes;
        try {
            messageBytes = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RasnetException("UTF-8 encoding is not supported.", e);
        }
        byte[] hash = messageDigest.digest(messageBytes);

        StringBuilder stringHash = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            stringHash.append(String.format("%02x", b));
        }

        return stringHash.toString();
    }
}
