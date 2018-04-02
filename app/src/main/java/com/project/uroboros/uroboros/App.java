package com.project.uroboros.uroboros;

/*Uroboros es una aplicación que permite al usuario descargar Threads de imagenes de cualquier
 *board de la pagina "4chan.org" y almacenarlas en el dispositivo*/


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.jsoup.nodes.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Portillo,Alderete on 25/01/2018.
 */
public class App extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private GridView gridView = null;
    private itemAdapter adapter = null;
    String title = "";
    String board = "/a/";
    ArrayList<String> Threads;
    ArrayList<String> Images;
    ArrayList<item> lista;
    ArrayList<String> Text;
    Bitmap bitmap;
    private int ImgNum = 0;
    GridImages g = null;
    DownloadIm di = null;
    ArrayList<String> TImages;
    ExtractImages EI = null;
    ExtractThreads ET = null;
    boolean loading = true;
    ProgressDialog dialog;
    private SwipeRefreshLayout swipe;
    FirebaseAuth firebaseAuth;
    boolean flag = true;
    String photo;
    ImageView profile;
    TextView email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {//Metodo de inicio
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                int permissionCall = 1;
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionCall);
            }
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        firebaseAuth = FirebaseAuth.getInstance();
        System.out.println(firebaseAuth.getCurrentUser().getEmail());

        photo = firebaseAuth.getCurrentUser().getPhotoUrl().toString();
        profile = (ImageView)headerView.findViewById(R.id.profile);


        email = (TextView) headerView.findViewById(R.id.email);
        email.setText(firebaseAuth.getCurrentUser().getEmail());
        new ProfileInfo().execute();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.menu));


        /*prueba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    EI = new ExtractImages(App.this);
                    //Threads = EI.boards("an");
                    Images = EI.threads("https://boards.4chan.org/an/thread/2607983");
                    title = EI.returnTitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(App.this, "Descargando" + title, Toast.LENGTH_SHORT).show();
                new DownloadIm().execute();
            }
        });*/

        try {
            ET = new ExtractThreads(App.this);
            ET.boards(board);
            TImages = ET.returnImages();
            Threads = ET.returnThreads();
            Text = ET.returnText();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Threads.get(0));
        }

        g = new GridImages();
        di = new DownloadIm();

        gridView = (GridView) findViewById(R.id.gridView);
        lista = new ArrayList<>();
        fillImages();

        adapter = new itemAdapter(this, R.layout.griditems, lista);
        gridView.setAdapter(adapter);

        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if(ImgNum < TImages.size())
                    new GridImages().execute();
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v,
                                    final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(App.this);
                View mview = getLayoutInflater().inflate(R.layout.btndownload,null);
                Button DL = (Button) mview.findViewById(R.id.download);
                TextView TV = (TextView) mview.findViewById(R.id.TV);
                TV.setText("Download: " + lista.get(position).getName());
                builder.setView(mview);
                final AlertDialog dialog = builder.create();
                dialog.show();

                DL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        try {
                            EI = new ExtractImages(App.this);
                            Images = EI.threads(Threads.get(position + 1));
                            title = EI.returnTitle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(App.this, "Descargando" + title, Toast.LENGTH_SHORT).show();
                        new DownloadIm().execute();
                    }
                });

            }
        });
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onRefresh() {
                swipe.setColorSchemeColors(R.color.menu);
                reLoad();
            }


        });

    }

    private void fillImages() {
        try {
            g.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reLoad(){
        lista.clear();
        adapter.notifyDataSetChanged();
        ImgNum = 0;
        try {
            ET = new ExtractThreads(App.this);
            ET.boards(board);
            TImages = ET.returnImages();
            Threads = ET.returnThreads();
            Text = ET.returnText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Threads.get(0));
        }
        g = new GridImages();
        di = new DownloadIm();
        fillImages();
    }

    public void SignOut(MenuItem item) {
        firebaseAuth.signOut();
        startActivity(new Intent(this,Login.class));
        finish();
    }

    public void Settings(MenuItem item) {
    }


    class GridImages extends AsyncTask<String,Void,Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (loading) {
                dialog = new ProgressDialog(App.this,R.style.AppCompatAlertDialogStyle);
                // Set progress dialog title
                dialog.setTitle("Loading");
                // Set progress dialog message
                dialog.setMessage("Loading Threads");
                dialog.setIndeterminate(false);

                // Show progress dialog
                dialog.show();
                loading = false;
            }

        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;



                for (int i = 0; i <= 14; i++) {
                    if (i<TImages.size()) {
                        try {
                            bitmap = BitmapFactory.decodeStream((InputStream) new URL(TImages.get(ImgNum)).getContent());

                            lista.add((new item(bitmap,Text.get(ImgNum))));
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                    ImgNum++;
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            adapter.notifyDataSetChanged();
            if (dialog.isShowing())
                dialog.dismiss();
            if (swipe.isRefreshing())
                swipe.setRefreshing(false);
        }
    }

    class DownloadIm extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Download dl = new Download(App.this, title);
            for (int i = 0; i<Images.size();i++){
                dl.downloadFile(Images.get(i));
            }
            return null;
        }
    }

    class ProfileInfo extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            URL newurl = null;
            try {
                newurl = new URL(photo);
                bitmap = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
                profile.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String act = board;

        if(id == R.id.a)
            board = "/a/";
        else if (id == R.id.c)
            board = "/c/";
        else if (id == R.id.w)
            board = "/w/";
        else if (id == R.id.m)
            board = "/m/";
        else if (id == R.id.cgl)
            board = "/cgl/";
        else if (id == R.id.cm)
            board = "/cm/";
        else if (id == R.id.f)
            board = "/f/";
        else if (id == R.id.n)
            board = "/n/";
        else if (id == R.id.jp)
            board = "/jp/";
        else if (id == R.id.v)
            board = "/v/";
        else if (id == R.id.vg)
            board = "/vg/";
        else if (id == R.id.vp)
            board = "/vp/";
        else if (id == R.id.vr)
            board = "/vr/";
        else if (id == R.id.co)
            board = "/co/";
        else if (id == R.id.g)
            board = "/g/";
        else if (id == R.id.tv)
            board = "/tv/";
        else if (id == R.id.k)
            board = "/k/";
        else if (id == R.id.o)
            board = "/o/";
        else if (id == R.id.an)
            board = "/an/";
        else if (id == R.id.tg)
            board = "/tg/";
        else if (id == R.id.sp)
            board = "/sp/";
        else if (id == R.id.asp)
            board = "/asp/";
        else if (id == R.id.sci)
            board = "/sci/";
        else if (id == R.id.his)
            board = "/his/";
        else if (id == R.id.Int)
            board = "/int/";
        else if (id == R.id.out)
            board = "/out/";
        else if (id == R.id.toy)
            board = "/toy/";
        else if (id == R.id.i)
            board = "/i/";
        else if (id == R.id.po)
            board = "/po/";
        else if (id == R.id.p)
            board = "/p/";
        else if (id == R.id.ck)
            board = "/ck/";
        else if (id == R.id.ic)
            board = "/ic/";
        else if (id == R.id.wg)
            board = "/wg/";
        else if (id == R.id.lit)
            board = "/lit/";
        else if (id == R.id.mu)
            board = "/mu/";
        else if (id == R.id.fa)
            board = "/fa/";
        else if (id == R.id.three)
            board = "/3/";
        else if (id == R.id.gd)
            board = "/gd/";
        else if (id == R.id.diy)
            board = "/diy/";
        else if (id == R.id.wsg)
            board = "/wsg/";
        else if (id == R.id.qst)
            board = "/qst/";
        else if (id == R.id.biz)
            board = "/biz/";
        else if (id == R.id.trv)
            board = "/trv/";
        else if (id == R.id.fit)
            board = "/fit/";
        else if (id == R.id.x)
            board = "/x/";
        else if (id == R.id.adv)
            board = "/adv/";
        else if (id == R.id.lgbt)
            board = "/lgbt/";
        else if (id == R.id.mlp)
            board = "/mlp/";
        else if (id == R.id.news)
            board = "/news/";
        else if (id == R.id.wsr)
            board = "/wsr/";
        else if (id == R.id.vip)
            board = "/vip/";


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (!act.equals(board)) {
            loading = true;
            reLoad();
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//Menu del toolbar
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//Opciones de la toolbar
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
