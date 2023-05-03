package com.cst.vingcard;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

public class TagUtils {

    public static String detectTagSerialNumber(Tag tag) {
        byte[] id = tag.getId();
        return Utils.toReversedHex(id);
    }

    public static final String MIFARE_CLASSIC = "MIFARE_CLASSIC";
    public static final String MIFARE_CLASSIC_PLUS = "MIFARE_CLASSIC_PLUS";
    public static final String MIFARE_CLASSIC_PRO = "MIFARE_CLASSIC_PRO";
    public static final String MIFARE_CLASSIC_ERROR = "MIFARE_CLASSIC_ERROR";
    public static final String MIFARE_ULTRALIGHT = "MIFARE_ULTRALIGHT";
    public static final String MIFARE_ULTRALIGHT_C = "MIFARE_ULTRALIGHT_C";
    public static final String UNKNOWN = "UNKNOWN";

    public static String detectTagTech(Tag tag) {
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            return MIFARE_CLASSIC;
                        case MifareClassic.TYPE_PLUS:
                            return MIFARE_CLASSIC_PLUS;
                        case MifareClassic.TYPE_PRO:
                            return MIFARE_CLASSIC_PRO;
                    }
                } catch (Exception e) {
                    return MIFARE_CLASSIC_ERROR + ":" + e.getMessage();
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        return MIFARE_ULTRALIGHT;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        return MIFARE_ULTRALIGHT_C;
                }
            }
        }
        return UNKNOWN;
    }

    public static String detectTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(Utils.toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(Utils.toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(Utils.toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(Utils.toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        Log.v("test",sb.toString());
        return sb.toString();
    }

}
