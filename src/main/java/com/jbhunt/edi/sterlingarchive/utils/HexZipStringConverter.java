package com.jbhunt.edi.sterlingarchive.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

@Slf4j
public class HexZipStringConverter {
    public static String convert(String hexInput) throws Exception {
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(hexStringToByteArray(hexInput)));
            int l;
            byte[] unzipped = new byte[8192];
            StringBuilder builder = new StringBuilder();
            while ((l=gzip.read(unzipped))>0) {
                builder.append(new String(unzipped));
            }
            gzip.close();
            return builder.toString().replace("\0", "");
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
