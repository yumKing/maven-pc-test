package org.jin.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileTools {

	public static String readKeyFile(String keyFile) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(keyFile)));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) != '-') {
                sb.append(readLine);
                sb.append(System.getProperty("line.separator"));
            }
        }
        br.close();
        return sb.toString();
    }
	
}
