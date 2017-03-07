package hr.riteh.moreno.registracije;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    private Requests requests = new Requests(MainActivity.this);

    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create spinner
        final Spinner spinner = (Spinner) findViewById(R.id.spinner_city);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cities_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // get all fields of form
        final EditText regNumber = (EditText) findViewById(R.id.regNumber);
        final EditText regLetters = (EditText) findViewById(R.id.regLetters);
        ImageView captchaPic = (ImageView) findViewById(R.id.captchaPic);
        final EditText captchaText = (EditText) findViewById(R.id.captcha);

        try {
            requests.getImage("http://www.huo.hr/inc/captcha/CaptchaSecurityImages.php", captchaPic);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Button submitButton = (Button) findViewById(R.id.sumbitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Molim pričekajte...");
                dialog.setCancelable(false);

                final String city = spinner.getSelectedItem().toString();
                final String regNumberString = regNumber.getText().toString();
                final String regLettersString = regLetters.getText().toString();
                final String captchaString = captchaText.getText().toString();

                final Integer checkStringNum = regNumberString.indexOf('?');
                final Integer checkStringLet = regLettersString.indexOf('?');

                int questionNum = regNumberString.length() - regNumberString.replace("?", "").length();
                int questionLet = regLettersString.length() - regLettersString.replace("?", "").length();

                if(regNumberString.isEmpty() || regLettersString.isEmpty() || captchaString.isEmpty()){
                    dialog.dismiss();
                    Toast toast = Toast.makeText(MainActivity.this, "Unesite sva polja!", Toast.LENGTH_SHORT);
                    toast.show();
                }

                else if((checkStringNum >= 0 && checkStringLet >= 0) || questionNum >= 2 || questionLet >= 2){
                    dialog.dismiss();
                    Toast toast = Toast.makeText(MainActivity.this, "Moguće upisati samo jedan upitnik." +
                            " Korisitite napredno pretraživanje!", Toast.LENGTH_SHORT);
                    toast.show();
                }

                else if(checkStringNum < 0 && checkStringLet < 0) {
                    final Intent intent = new Intent(MainActivity.this, DisplayCars.class);
                    final Thread mThread = new Thread(){
                        @Override
                        public void run(){
                            if (dialog.isShowing()) {
                                try {
                                    requests.postForm(city, regNumberString, regLettersString, captchaString, intent, dialog);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    dialog.show();
                    mThread.start();
                } else {
                    final Intent intent = new Intent(MainActivity.this, DisplayCars.class);
                    final Thread mThread =  new Thread(){
                        @Override
                        public void run(){
                            if (dialog.isShowing()) {
                                try {
                                    requests.postMultipleForms(city, regNumberString, regLettersString, captchaString, checkStringNum, checkStringLet, intent, dialog);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Prekid", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mThread.interrupt();
                        }
                    });
                    dialog.show();
                    mThread.start();
                }
            }
        });

        Button advancedButton = (Button) findViewById(R.id.advancedButton);

        advancedButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AdvancedActivity.class);
                startActivity(intent);
            }
        });

        regNumber.setOnFocusChangeListener(this);
        regLetters.setOnFocusChangeListener(this);
        captchaText.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            hideKeyboard(v);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
