package app.shevyakov.smartplay.db;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Andrey Shevakov on 19/04/2015.
 */
public class AddUpdSongRemDB extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {

        try {

            String link;
            String user = "u646815360_admin";
            String pass = "trustno1";
            String data;

            if (Integer.parseInt(params[0]) == 0) {
                link = "http://smartplay.16mb.com/add.php";
                data = URLEncoder.encode("user", "UTF-8")
                        + "=" + URLEncoder.encode(user, "UTF-8");

                data += "&" + URLEncoder.encode("pass", "UTF-8")
                        + "=" + URLEncoder.encode(pass, "UTF-8");
                data += "&" + URLEncoder.encode("song", "UTF-8")
                        + "=" + URLEncoder.encode(params[1], "UTF-8");
                data += "&" + URLEncoder.encode("rating", "UTF-8")
                        + "=" + URLEncoder.encode(params[2], "UTF-8");
                data += "&" + URLEncoder.encode("tf", "UTF-8")
                        + "=" + URLEncoder.encode(params[3], "UTF-8");
            }

            else {
                link = "http://smartplay.16mb.com/upd.php";

                data = URLEncoder.encode("user", "UTF-8")
                        + "=" + URLEncoder.encode(user, "UTF-8");
                data += "&" + URLEncoder.encode("pass", "UTF-8")
                        + "=" + URLEncoder.encode(pass, "UTF-8");
                data += "&" + URLEncoder.encode("song", "UTF-8")
                        + "=" + URLEncoder.encode(params[1], "UTF-8");
                data += "&" + URLEncoder.encode("rating", "UTF-8")
                        + "=" + URLEncoder.encode(params[2], "UTF-8");
                data += "&" + URLEncoder.encode("tf", "UTF-8")
                        + "=" + URLEncoder.encode(params[3], "UTF-8");
            }


            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter
                    (conn.getOutputStream());
            wr.write(data);
            wr.flush();

            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(conn.getInputStream()));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
