package ar.edu.unlam.soa.iotlocker.view.activities;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ar.edu.unlam.soa.iotlocker.R;
import ar.edu.unlam.soa.iotlocker.AdminActivity;

import static ar.edu.unlam.soa.iotlocker.helper.HttpHelper.get;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public int estado;
    private TextView textEstado;
    private Button buttonCambiarEstado, buttonDesbloquear;
    private boolean flag_voice;
    private SpeechRecognizer sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Se crea un timer para ejecutar la funcion de actualizar estado
        Timer timer = new Timer ();
        TimerTask timerTask = new TimerTask () {
            @Override
            public void run () {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            update(findViewById(R.id.buttonCambiarEstado));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        // Se agenda la tarea para correr cada 2 segundos
        timer.schedule (timerTask, 0l, 1500);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        textEstado = (TextView) findViewById(R.id.textEstado);
        buttonCambiarEstado = (Button) findViewById(R.id.buttonCambiarEstado);
        buttonDesbloquear = (Button) findViewById(R.id.buttonDesbloquear);

        Log.d("TAG", buttonDesbloquear.getText().toString());
        textEstado.setText("ABIERTO");
        textEstado.setTextColor(Color.GREEN);
        //buttonDesbloquear.setVisibility(View.INVISIBLE);
        buttonCambiarEstado.setVisibility(View.VISIBLE);
        buttonCambiarEstado.setText("CERRAR");

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        try {
            update(findViewById(R.id.buttonCambiarEstado));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.nav_status) {
            return true;
        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
        } else if (id == R.id.nav_about) {

            startActivity(new Intent(MainActivity.this, AdminActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        Log.d("settings", ""+R.id.action_settings);
        Log.d("status", ""+ R.id.nav_status);
        Log.d("manage", ""+R.id.nav_manage);
        Log.d("about", ""+ R.id.nav_about);
        Log.d("CLICK", "id");
        if (id == R.id.nav_manage) {

            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
            //break;
            //startActivity( new Intent(MainActivity.this, AdminActivity.class) );
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Actualiza el label que informa el estado de la caja (Abierto, Cerrado o Bloqueado).
     *
     * @param view
     * @throws JSONException
     * @throws IOException
     */
    public void update(View view) throws JSONException, IOException {
        Log.d("GET STATUS", "REQUEST");
        JSONObject mainObject = new JSONObject(get("/status_locker"));
        estado = mainObject.getInt("status");
        Log.d("GET STATUS", ""+estado);
        if (estado == 0) {
            textEstado.setText("ABIERTO");
            textEstado.setTextColor(Color.GREEN);
            buttonDesbloquear.setVisibility(View.INVISIBLE);
            buttonCambiarEstado.setVisibility(View.VISIBLE);
            buttonCambiarEstado.setText("CERRAR");
            Log.d("GET STATUS", "ABIERTO");
        } else if (estado == 1) {
            textEstado.setText("CERRADO");
            textEstado.setTextColor(Color.BLUE);
            buttonDesbloquear.setVisibility(View.INVISIBLE);
            buttonCambiarEstado.setVisibility(View.VISIBLE);
            buttonCambiarEstado.setText("ABRIR");
            Log.d("GET STATUS", "CERRADO");
        } else if (estado == 2) {
            textEstado.setText("BLOQUEADO");
            textEstado.setTextColor(Color.RED);
            buttonDesbloquear.setVisibility(View.VISIBLE);
            buttonCambiarEstado.setVisibility(View.INVISIBLE);
            Log.d("GET STATUS", "BLOQUEADO");
        }
    }

    /**
     * Metodo que cambia el estado de la caja, es decir:
     * Si el estado es 0 significa que la puerta esta abierta por lo que hay que cerrarla.
     * Si el estado es 1 significa que la puerta esta cerrada, y entonces la abre.
     *
     * @param view
     * @throws JSONException
     */
    public void cambiarEstado(View view) throws JSONException{
        JSONObject mainObject = null;
        Integer result = 0;
        if (estado == 0) { // cerrar
            try {
                mainObject = new JSONObject(get("/security/close"));
                result = mainObject.getInt("result");
                Log.d("GET CLOSE", ""+result);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (estado == 1) { // abrir
            try {
                mainObject = new JSONObject(get("/security/open"));
                result = mainObject.getInt("result");
                Log.d("GET OPEN", ""+result);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class listener implements RecognitionListener {
        String[] palabras = new String[5];
        private static final String TAG = "VOICE";

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.d(TAG, "error " + error);
            if (error == 7) {
                Toast.makeText(
                        getApplicationContext(),
                        "Por favor, intente de nuevo",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        public void onResults(Bundle results) {
            Integer result = 0;
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            palabras[0] = ""+data.get(0);
            Log.d(TAG, "result " + data.get(0));



            JSONObject mainObject = null;
            try {
                mainObject = new JSONObject(get("/security/unlock?palabra="+palabras[0]));
                result = mainObject.getInt("result");
                Log.d("GET PALABRA", ""+result);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result == 1) {
                // Se ha desbloqueado la caja
                Toast.makeText(
                        getApplicationContext(),
                        "OK",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "No hubo coincidencia. Intente de nuevo",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }

    }

    /**
     * Funcionalidad que inicializa la API de reonocimiento de voz para detectar la palabra clave
     * que sirve para desbloquear la caja (cuando se encuentra bloqueada por intentos fallidos).
     *
     * Si flag_voice es false significa que el servicio no está activo, por ende se activa y
     * comienza a escuchar.
     * Si flag_voice es true significa que estaba activo, entonces para el servicio, deja de escuchar.
     *
     */
    public void desbloquear(View view) {
        if (flag_voice == false) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            sr.startListening(intent);
            Log.i("VOICE", "START");
            flag_voice = true;
        } else {
            sr.stopListening();
            flag_voice = false;
        }
    }
}

