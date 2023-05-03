package com.cst.vingcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

final class Utils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    static String pageBreak(String input) {
        if (input == null) return null;

        return input.replaceAll("(.{8})", "$1\n");
    }

    static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    static void showNfcSettingsDialog(final Activity app) {
        new AlertDialog.Builder(app)
                .setTitle("NFC is disabled")
                .setMessage("You must enable NFC to use this app.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        app.startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        app.finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //for write nfc card
    public static String[] splitString(String text, int length) {
        String regex = "(.{" + length + "})";
        String newStr = text.replaceAll(regex, "$1|");
        return newStr.split("\\|");
    }

    public static byte[] stringToHex(String str) {
        byte[] by = new byte[4]; // 4 because the size of each write is 4 bytes
        byte[] temp = str.getBytes();
        int length = str.length();
        for (int i = 0; i < length / 2; i++) {
            by[i] = string2Hex(temp[2 * i], temp[2 * i + 1]);
        }
        return by;
    }

    public static byte string2Hex(byte src0, byte src1) {
        byte _bo = Byte.decode("0x" + new String(new byte[]{src0}));
        _bo = (byte) (_bo << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}));
        return (byte) (_bo ^ _b1);
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    public static long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (byte aByte : bytes) {
            long value = aByte & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    public static long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

}
