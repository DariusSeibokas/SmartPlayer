package app.shevyakov.smartplay.db;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import app.shevyakov.smartplay.fragments.FragmentPlay;

/**
 * Created by Andrey Shevyakov on 18/04/2015.
 */
public class RetrieveRemDB extends AsyncTask<String, Void, String> {

    private FragmentPlay.FragmentCallback mFragmentCallback;

    public RetrieveRemDB(FragmentPlay.FragmentCallback fragmentCallback)
    {
        mFragmentCallback = fragmentCallback;
    }

    @Override
    protected String doInBackground(String ... params) {

        try {
            String link = "http://smartplay.16mb.com/rating.php";
            String user = "u646815360_admin";
            String pass = "trustno1";
            String data;

                data = URLEncoder.encode("user", "UTF-8")
                        + "=" + URLEncoder.encode(user, "UTF-8");
                data += "&" + URLEncoder.encode("pass", "UTF-8")
                        + "=" + URLEncoder.encode(pass, "UTF-8");
                data += "&" + URLEncoder.encode("song", "UTF-8")
                        + "=" + URLEncoder.encode(params[0], "UTF-8");


            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter
                    (conn.getOutputStream());
            wr.write(data);
            wr.flush();

            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                break;
            }

            String result = sb.toString();

            if (result.contains("PHP Error Message") || result == null || result.equals("")) result = "N/A";

            return result;

        }

        catch (Exception e){
            e.printStackTrace();
            return "N/A";
        }
    }

        @Override
    protected void onPostExecute(String result) {
        mFragmentCallback.onTaskDone(result);
    }
}
