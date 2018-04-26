package ht.vpn.android.network;

import java.util.*;
import android.util.Base64;
import java.io.*;
import java.net.*;

public class VPNService {
    private static final String API_URL = "https://www.pivotsecurity.com/wp-json/api/v1/";

    private VPNService() {
    }

    public static String login(String username, String password){
        if (username != null && password != null) {
        	HashMap<String, String> data = new HashMap<String, String>();
        	data.put("user", username);
        	data.put("auth", password);
        	return callPostService(API_URL + "logintoserver", data);
        }

        return null;
    }
    
    public static String callPostService(String Url, HashMap<String, String> data) {
        URL url;
        String response = "";
        try {
            url = new URL(Url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(data));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == 200) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
