package ar.edu.unlam.soa.iotlocker.view.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import ar.edu.unlam.soa.iotlocker.R;

import static ar.edu.unlam.soa.iotlocker.helper.HttpHelper.get;

public class LoginActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;
    private Boolean processingFlag = false;
    int i = 0;
    float[][] valores = new float[MUESTRAS_POR_SEGUNDO][3];
    boolean flag_patron;
    String patron, patronNew;
    TextView textPatron;

    static final int MUESTRAS_POR_SEGUNDO = 10;
    static final String FILENAME = "file_patron";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        FileInputStream fos;
        textPatron = (TextView)findViewById(R.id.textPatron);
        textPatron.setText("");
        //File dir = getFilesDir();
        //File file = new File(dir, FILENAME);
        //boolean deleted = file.delete();

        Log.d("GET STATUS", "REQUEST");

        try {
            JSONObject mainObject;
            mainObject = new JSONObject(get("/status_patron"));
            Integer result = mainObject.getInt("status");
            Log.d("RESULT PATRON", ""+result);
            if(mainObject.getInt("status") == 1){
                flag_patron = false;
                patronNew = "";
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
                            if(i == MUESTRAS_POR_SEGUNDO) {
                                i = 0;
                                flag_patron = true;
                            }
                            if(flag_patron && patronNew.length() <= 10){
                                j = i;
                                fin = j;
                                medio = j;
                                j++;
                                if(j == MUESTRAS_POR_SEGUNDO)
                                    j=0;
                                inicial = j;
                                j++;
                                if(j == MUESTRAS_POR_SEGUNDO)
                                    j=0;
                                while(flag_medio_x == false && flag_medio_y == false && j != fin){

                                    /**
                                     * If para detectar el movimiento:
                                     * I -> Izquierda
                                     * D -> Derecha
                                     * A -> Atras
                                     * F -> Frente
                                     */
                                    if(valores[j][0] - valores[inicial][0] > 10){
                                        flag_medio_x = true;
                                        dir = 'I';
                                        medio = j;
                                    }else if(valores[j][0] - valores[inicial][0] < -10){
                                        flag_medio_x = true;
                                        dir = 'D';
                                        medio = j;
                                    }else if(valores[j][2] - valores[inicial][2] < -10){
                                        flag_medio_y = true;
                                        dir = 'A';
                                        medio = j;
                                    }else if(valores[j][2] - valores[inicial][2] > 10){
                                        flag_medio_y = true;
                                        dir = 'F';
                                        medio = j;
                                    }else{
                                        j++;
                                        if(j == MUESTRAS_POR_SEGUNDO)
                                            j=0;
                                    }
                                }
                                if(flag_medio_x == true){

                                    while(flag_fin == false && j != fin){
                                        j++;
                                        if(j == MUESTRAS_POR_SEGUNDO)
                                            j=0;
                                        if((valores[j][0] - valores[medio][0] < -10 && dir == 'I') || (valores[j][0] - valores[medio][0] > 10 && dir == 'D')){
                                            flag_fin = true;
                                        }
                                    }
                                } else if(flag_medio_y == true){

                                    while(flag_fin == false && j != fin){
                                        j++;
                                        if(j == MUESTRAS_POR_SEGUNDO)
                                            j=0;
                                        if((valores[j][2] - valores[medio][2] < -10 && dir == 'F') || (valores[j][2] - valores[medio][2] > 10 && dir == 'A')){
                                            flag_fin = true;
                                        }
                                    }
                                }
                                if(flag_fin == true){
                                    flag_patron = false;
                                    i=0;
                                    valores = new float[MUESTRAS_POR_SEGUNDO][3];
                                    patronNew += dir;
                                    textPatron.setText(patronNew.replace("I", "<").replace("D", ">").replace("A", "˄").replace("F", "˅"));
                                }
                            }
                        }
                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

                mSensorManager.registerListener(_SensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000000);
                Log.d("PATRON ENCONTRADO", "-");
            } else {
                findViewById(R.id.titlePatron).setVisibility(View.GONE);
                textPatron.setVisibility(View.GONE);
                findViewById(R.id.buttonAceptarPatron).setVisibility(View.GONE);
                findViewById(R.id.buttonVaciar).setVisibility(View.GONE);
                Log.d("PATRON NO ENCONTRADO", "-");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    try {
                        attemptLogin();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptLogin();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() throws IOException {

        if (processingFlag) {
            return;
        }
        passwordView.setError(null);
        String password = passwordView.getText().toString();

        // Check for a valid password.
        int passwordLength = getResources().getInteger(R.integer.password_length);
        if (TextUtils.isEmpty(password)
                || password.length()!=passwordLength
                || !TextUtils.isDigitsOnly(password)) {

            passwordView.setError(getString(R.string.error_invalid_password));
            passwordView.requestFocus();

        } else {
            // Show progress and kick off background task to backend communication.
            processingFlag = true;

            processPasswordValidation(get("/security/input_password?password="+password));
        }
    }

    private void processPasswordValidation(String data) {
        showProgress(false);

        if (data.contains("true")) {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.correct_password),
                    Toast.LENGTH_SHORT
            ).show();

            startActivity( new Intent(LoginActivity.this, MainActivity.class) );
            finish();
        } else {
            processingFlag=false;
            passwordView.setError(getString(R.string.error_incorrect_password));
            passwordView.requestFocus();
        }
    }
    private void processPasswordValidationPatron(String data) {
        showProgress(false);

        if (data.contains("true")) {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.correct_password),
                    Toast.LENGTH_SHORT
            ).show();

            startActivity( new Intent(LoginActivity.this, MainActivity.class) );
            finish();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.error_incorrect_password),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void vaciarPatron(View view){
        patronNew = "";
        textPatron.setText(patronNew);
    }

    public void aceptarPatron(View view) throws IOException {
        JSONObject mainObject;
        String result = "false";

        Log.d("GET PATRON", "PATRON "+patronNew);



        try {
            mainObject = new JSONObject(get("/security/input_patron?patron="+patronNew));
            result = mainObject.getString("isValid");
            Log.d("GET PATRON", " Result: "+result);
            processPasswordValidationPatron(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

