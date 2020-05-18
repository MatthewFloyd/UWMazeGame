/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 * 
 * 5/13/2020
 * 
 *    This class uses static methods to convert float, integer, long integer, and hexadecimal values to byte arrays and vice-versa
 */
import java.nio.ByteBuffer;

public final class Converter {
    public Converter() {
        
    }
    
    // Float values 
    public static float convertToFloat(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return buffer.getFloat();
    }

    public static byte[] floatToByte(float value) {        
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    // Integer values
    public static int convertToInt(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return buffer.getInt();
    }

    public static byte[] intToByte(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    //Long Integer values
    public static long convertToLong(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return buffer.getLong();
    }

    public static byte[] longToByte(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }

    // Hexadecimal values
    // adapted from https://www.baeldung.com/java-byte-arrays-hex-strings
    public static String convertToHex(byte[] array) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for(int i = 0; i < array.length; i++) {
            hexStringBuffer.append(byteToHex(array[i]));
        }
        return hexStringBuffer.toString();
    }

    private static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        // shift right 4 bits
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public static byte[] decodeHexString(String hex) {
        if(hex.length() % 2 == 1) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal String supplied.");
        }

        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = hexToByte(hex.substring(i, i + 2));
        }
        return bytes;
    }

    public static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }

};
