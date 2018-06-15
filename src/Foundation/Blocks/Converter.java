package Foundation.Blocks;

import Top.NKSql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Converter {

    public byte[] convertToBytes(Integer integer) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeInt(integer);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            System.out.println("Cannot convert some integer to bytes.");
            exception.printStackTrace();
        }
        return null;
    }

    public byte[] convertToBytes(Float floatNumber) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeFloat(floatNumber);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            System.out.println("Cannot convert some float to bytes.");
            exception.printStackTrace();
        }
        return null;
    }

    public byte[] convertToBytes(String string) {
        return extended(string).getBytes();
    }

    public Integer convertToInteger(byte[] bytes) {
        DataInputStream dataInputStream = createDataInputStream(bytes);
        try {
            return dataInputStream.readInt();
        } catch (Exception exception) {
            System.out.println("Cannot convert some bytes to integer.");
            exception.printStackTrace();
        }
        return -1;
    }

    public Float convertToFloat(byte[] bytes) {
        DataInputStream dataInputStream = createDataInputStream(bytes);
        try {
            return dataInputStream.readFloat();
        } catch (Exception exception) {
            System.out.println("Cannot convert some bytes to float.");
            exception.printStackTrace();
        }
        return (float)-1;
    }

    public String convertToString(byte[] bytes) {
        String string = new String(bytes);
        return trimmed(string);
    }

    private DataInputStream createDataInputStream(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        return new DataInputStream(byteArrayInputStream);
    }

    private String extended(String string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        while (stringBuilder.length() < NKSql.maxLengthOfString) {
            stringBuilder.append(" ");
        }
        string = stringBuilder.toString();
        return string;
    }

    private String trimmed(String string) {
        return string.replaceFirst("\\s++$", "");
    }

}
