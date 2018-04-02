package com.project.uroboros.uroboros;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by INSPIRON on 07/03/2018.
 */

public class ExtractThreads {

    ArrayList<String> Html;
    ArrayList<String> Threads;
    ArrayList<String> Images;
    ArrayList<String> Subs;
    ArrayList<String> Teaser;
    private String url = null; //Prueba
    private String fix;
    private int length = 0;
    private Document doc; //elemento del Jsoup para guardar los links
    private String board; //los tipos de links que se extraerán
    private String Title;
    private Context context = null;
    Pattern pat;
    Pattern patIm;
    Matcher mat;
    Matcher matIm;
    String d;

    public ExtractThreads(Context context) throws IOException {
        this.context = context;

    }

    public void boards(String board) throws ExecutionException, InterruptedException {//el patron que se usará para los links de los boards
        this.board = board;
        Html = new ArrayList<>();
        Threads = new ArrayList<>();
        Images = new ArrayList<>();
        Subs = new ArrayList<>();
        Teaser = new ArrayList<>();
        url = "https://boards.4chan.org"+board+"catalog";
        new DataGrabber().execute().get();



        String[] codes = d.split("\"");
        String[] codes2 =d.split(",");

        Html.clear();

        pat = Pattern.compile("([^,{}]*)");
        for (int i = 0; i<codes.length;i++){
            mat = pat.matcher(codes[i]);
            if (mat.matches()){
                Html.add(codes[i]);
            }
        }

        pat = Pattern.compile("^\"sub\".*");
        patIm = Pattern.compile("^\"teaser\".*");
        for (int i = 0; i<codes2.length;i++){
            mat = pat.matcher(codes2[i]);
            matIm = patIm.matcher(codes2[i]);
            if (mat.matches() || matIm.matches()){
                Subs.add(codes2[i].split(":")[1]);
            }
        }
        for (int i = 0; i<Subs.size();i=i+2){
            fix = (Subs.get(i) + "-" + Subs.get(i+1))
                    .replace("\"", "")
                    .replace("}", "")
                    .replace("&#039;","'")
                    .replace("&gt;",">");
            length = fix.length();
            if (length>50){
                length = 50;
            }

            Teaser.add(fix.substring(0,length));
        }


        Threads.add(Title);
        for (int i = 0; i < Html.size(); i++){


            pat = Pattern.compile("[0-9]{1,12}");
            mat = pat.matcher(Html.get(i));
            if (mat.matches() && (Threads.size()-1) == Images.size())
                Threads.add("https://boards.4chan.org" + board + "thread/" + Html.get(i));
            else if(mat.matches()){
                Images.add("https://s.4cdn.org/image/nofile.png");
                Threads.add("https://boards.4chan.org" + board + "thread/" + Html.get(i));
            }
            patIm = Pattern.compile("[0-9]{13,16}");
            mat = patIm.matcher(Html.get(i));
            if (mat.matches()) {
                Images.add("https://i.4cdn.org" + board + Html.get(i) + "s.jpg");
            }
        }
        //for (int i = 0; i < Teaser.size(); i++){
        //    System.out.println(Teaser.get(i));
        //}
    }


    public ArrayList<String> returnThreads(){
        return Threads;
    }
    public ArrayList<String> returnImages(){
        return Images;
    }
    public ArrayList<String> returnText(){
        return Teaser;
    }

    /*Clase para implementar un thread para la extraccion de los links*/
    private class DataGrabber extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {//Thread
            try {
                //Se conecta a la pagina de internet y extrae los links
                doc = Jsoup.connect(url).get();
                Title = doc.title().split("-")[1];
                Elements links = doc.select("script");

                d = links.toString();
                System.out.println(d);

                //for (Element link : links) {
                //    Html.add(link.data());
                //}

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
