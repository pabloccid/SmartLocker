package ar.edu.unlam.soa.iotlocker;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static ar.edu.unlam.soa.iotlocker.helper.HttpHelper.get;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {
    private SensorManager mSensorManager;
    String palabra = new String();
    static final int MUESTRAS_POR_SEGUNDO = 10;
    int i = 0;
    float[][] valores = new float[MUESTRAS_POR_SEGUNDO][3];
    TextView textPatron;
    boolean flag_patron;
    String patron = "";
    static final String TAG = "VOICE";
    boolean flag_voice;


    private SpeechRecognizer sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        textPatron = (TextView) findViewById(R.id.textView3);

        flag_patron = false;
        Button speakButton = (Button) findViewById(R.id.buttonListen);
        speakButton.setOnClickListener(this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        SensorEventListener _SensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                Sensor mySensor = sensorEvent.sensor;

                if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    int j;
                    int inicial;
                    int medio;
                    int fin;
                    char dir = Character.MIN_VALUE;
                    boolean flag_medio_x = false;
                    boolean flag_medio_y = false;
                    boolean flag_fin = false;
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];
                    valores[i][0] = x;
                    valores[i][1] = y;
                    valores[i][2] = z;

                    i++;
                    if (i == MUESTRAS_POR_SEGUNDO) {
                        i = 0;
                        flag_patron = true;
                    }
                    if (flag_patron && patron.length() <= 10) {
                        j = i;
                        fin = j;
                        medio = j;
                        j++;
                        if (j == MUESTRAS_POR_SEGUNDO)
                            j = 0;
                        inicial = j;
                        j++;
                        if (j == MUESTRAS_POR_SEGUNDO)
                            j = 0;
                        while (flag_medio_x == false && flag_medio_y == false && j != fin) {

                            /**
                             * If para detectar el movimiento:
                             * I -> Izquierda
                             * D -> Derecha
                             * A -> Atras
                             * F -> Frente
                             */
                            if (valores[j][0] - valores[inicial][0] > 10) {
                                flag_medio_x = true;
                                dir = 'I';
                                medio = j;
                            } else if (valores[j][0] - valores[inicial][0] < -10) {
                                flag_medio_x = true;
                                dir = 'D';
                                medio = j;
                            } else if (valores[j][2] - valores[inicial][2] < -10) {
                                flag_medio_y = true;
                                dir = 'A';
                                medio = j;
                            } else if (valores[j][2] - valores[inicial][2] > 10) {
                                flag_medio_y = true;
                                dir = 'F';
                                medio = j;
                            } else {
                                j++;
                                if (j == MUESTRAS_POR_SEGUNDO)
                                    j = 0;
                            }
                        }
                        if (flag_medio_x == true) {

                            while (flag_fin == false && j != fin) {
                                j++;
                                if (j == MUESTRAS_POR_SEGUNDO)
                                    j = 0;
                                if ((valores[j][0] - valores[medio][0] < -10 && dir == 'I') || (valores[j][0] - valores[medio][0] > 10 && dir == 'D')) {
                                    flag_fin = true;
                                }
                            }
                        } else if (flag_medio_y == true) {

                            while (flag_fin == false && j != fin) {
                                j++;
                                if (j == MUESTRAS_POR_SEGUNDO)
                                    j = 0;
                                if ((valores[j][2] - valores[medio][2] < -10 && dir == 'F') || (valores[j][2] - valores[medio][2] > 10 && dir == 'A')) {
                                    flag_fin = true;
                                }
                            }
                        }
                        if (flag_fin == true) {
                            flag_patron = false;
                            i = 0;
                            valores = new float[MUESTRAS_POR_SEGUNDO][3];
                            patron += dir;
                            textPatron.setText(patron.replace("I", "<").replace("D", ">").replace("A", "˄").replace("F", "˅"));
                        }

                    }

                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.registerListener(_SensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000000);
    }

    public void vaciarPatron(View view) {
        patron = "";
        textPatron.setText(patron);
    }

    /**
     * Se invoca al servicio para agregar al patron de ingreso.
     *
     * @param view
     * @throws IOException
     */
    public void aceptarPatron(View view) throws IOException {
        JSONObject mainObject = null;
        Integer result = 0;
        try {
            mainObject = new JSONObject(get("/set/patron?patron=" + patron));
            result = mainObject.getInt("result");
            Log.d("GET PATRON", "" + result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(result == 1){
            finish();
        }


    }

    /**
     * Se invoca al servicio para agregar la palabra clave.
     *
     * @param view
     */
    public void aceptarPalabra(View view) {
        JSONObject mainObject = null;
        Integer result = 0;
        try {
            mainObject = new JSONObject(get("/set/palabra?palabra=" + palabra));
            result = mainObject.getInt("result");
            Log.d("GET PALABRA", "" + result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result == 1)
            finish();
    }

    class listener implements RecognitionListener {
        TextView textVoice = (TextView) findViewById(R.id.textPalabra);

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
//            textVoice.setText("error " + error);
        }

        public void onResults(Bundle results) {
            palabra = "";
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.d(TAG, "result " + data.get(0));
            palabra += data.get(0);
            textVoice.setText(palabra);
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonListen) {
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
}


