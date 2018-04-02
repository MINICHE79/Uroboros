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
 * Created by Portillo,Alderete on 28/01/2018.
 */
/*Extrae del codigo fuente de la pagina todos los links de imagenes */
public class ExtractImages {
    ArrayList<String> Links; //Arreglo que almacena los url's
    private int cont = 1;
    private String url = null; //Prueba
    private Document doc; //elemento del Jsoup para guardar los links
    private String comparar = ""; //se usa para comparar si un elemento ya existe en el arreglo
    private String patron; //los tipos de links que se extraerán
    private Context context = null;
    String title = "";

    // inicia la clase DataGrabber
    public ExtractImages(Context context) throws IOException {
        this.context = context;

    }

    public String returnTitle(){
        return title;
    }

    public ArrayList<String> threads(String url) throws ExecutionException, InterruptedException {//el patrón que se usará para los links de los threads
        Links = new ArrayList<>();
        patron = ".*4cdn.*";
        this.url = url;
        new DataGrabber().execute().get();
        return Links;
    }

    //Transforma a string los datos

    private static String guardar(String msg, Object... args) {

        return (String.format(msg, args));
    }

    /*Clase para implementar un thread para la extraccion de los links*/
    private class DataGrabber extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {//Thread
            try {
                //Se conecta a la pagina de internet y extrae los links
                doc = Jsoup.connect(url).get();
                System.out.println(doc.title());
                title = doc.title();
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    Pattern pat = Pattern.compile(comparar);//compara si no es un link repetido
                    Matcher mat = pat.matcher(guardar(link.attr("abs:href")));
                    Pattern patE = Pattern.compile(patron);//compara que sea un link deseado
                    Matcher matE = patE.matcher(guardar(link.attr("abs:href")));
                    if (!mat.matches() && matE.matches()){
                        Links.add(guardar(link.attr("abs:href")));
                        comparar = guardar(link.attr("abs:href"));
                    }
                }
                System.out.println(Links.size());
                for (int i = 0; i<Links.size();i++){
                    System.out.println(Links.get(i));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        /*ProgressDialog progressBar = new ProgressDialog(context);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setCancelable(true);
            progressBar.setMessage("Cargando");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.dismiss();
        }*/
    }

}



