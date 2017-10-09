package ar.edu.unlam.soa.iotlocker.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpHelper {

    static final String NUKEURL = "http://192.168.10.141:8080";

    public static String get (String url) throws IOException {
        System.setProperty("http.keepAlive", "false");
        URL requestUrl = new URL(NUKEURL+url);
        URLConnection con;


        con  = requestUrl.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        int cp;
        try {
            while ((cp = in.read()) != -1) {
                sb.append((char) cp);
            }
        }catch(Exception e){
        }
        String json = sb.toString();
        return json;
    }
}
