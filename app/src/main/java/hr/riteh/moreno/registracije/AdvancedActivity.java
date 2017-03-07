package hr.riteh.moreno.registracije;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class AdvancedActivity extends AppCompatActivity implements MultiSelectSpinner.OnMultipleItemsSelectedListener, View.OnFocusChangeListener {

    private Requests requests = new Requests(AdvancedActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Spinner spinner = (Spinner) findViewById(R.id.advancedCity);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cities_array,
                R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final String[] numbers = getResources().getStringArray(R.array.numbers);
        String[] letters = getResources().getStringArray(R.array.letters);

        final MultiSelectSpinner num1 = (MultiSelectSpinner) findViewById(R.id.num1);
        num1.setItems(numbers);
        num1.setSelection(new int[]{});
        num1.setListener(this);

        final MultiSelectSpinner num2 = (MultiSelectSpinner) findViewById(R.id.num2);
        num2.setItems(numbers);
        num2.setSelection(new int[]{});
        num2.setListener(this);

        final MultiSelectSpinner num3 = (MultiSelectSpinner) findViewById(R.id.num3);
        num3.setItems(numbers);
        num3.setSelection(new int[]{});
        num3.setListener(this);

        final MultiSelectSpinner num4 = (MultiSelectSpinner) findViewById(R.id.num4);
        num4.setItems(numbers);
        num4.setSelection(new int[]{});
        num4.setListener(this);

        final MultiSelectSpinner let1 = (MultiSelectSpinner) findViewById(R.id.let1);
        let1.setItems(letters);
        let1.setSelection(new int[]{});
        let1.setListener(this);

        final MultiSelectSpinner let2 = (MultiSelectSpinner) findViewById(R.id.let2);
        let2.setItems(letters);
        let2.setSelection(new int[]{});
        let2.setListener(this);

        ImageView advancedCaptchaPic = (ImageView) findViewById(R.id.advancedCaptchaPic);
        final EditText advancedCaptcha = (EditText) findViewById(R.id.advancedCaptcha);

        try {
            requests.getImage("http://www.huo.hr/inc/captcha/CaptchaSecurityImages.php", advancedCaptchaPic);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button button = (Button) findViewById(R.id.advancedSubmit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(num1.getSelectedStrings().isEmpty() || num2.getSelectedStrings().isEmpty() ||
                        num3.getSelectedStrings().isEmpty() || let1.getSelectedStrings().isEmpty() || advancedCaptcha.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText(AdvancedActivity.this, "Unesite obavezna polja!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    final String city = spinner.getSelectedItem().toString();

                    final List<String> numbers1 = num1.getSelectedStrings();
                    final List<String> numbers2 = num2.getSelectedStrings();
                    final List<String> numbers3 = num3.getSelectedStrings();
                    final List<String> numbers4 = num4.getSelectedStrings();
                    final List<String> letters1 = let1.getSelectedStrings();
                    final List<String> letters2 = let2.getSelectedStrings();

                    if (numbers1.isEmpty()) numbers1.add("");
                    if (numbers2.isEmpty()) numbers2.add("");
                    if (numbers3.isEmpty()) numbers3.add("");
                    if (numbers4.isEmpty()) numbers4.add("");
                    if (letters1.isEmpty()) letters1.add("");
                    if (letters2.isEmpty()) letters2.add("");

                    final String captchaString = advancedCaptcha.getText().toString();

                    final Integer combinations = numbers1.size() * numbers2.size() * numbers3.size() *
                            numbers4.size() * letters1.size() * letters2.size();

                    if (combinations > 50){
                        Toast toast = Toast.makeText(AdvancedActivity.this, "Prevelik broj kombinacija (" + combinations.toString() +
                            "). Maksimalan broj kombinacija je 50. Smanjite broj znakova!", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(AdvancedActivity.this);
                        confirmBuilder.setMessage("Ukupan broj mogućih kombinacija iznosi " + combinations.toString() +
                                " Jeste li sigurni da želite nastaviti?")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final Intent intent = new Intent(AdvancedActivity.this, DisplayCars.class);
                                        final ProgressDialog dialog = new ProgressDialog(AdvancedActivity.this);
                                        dialog.setMessage("Molim pričekajte...");
                                        dialog.setCancelable(false);
                                        final Thread thread = new Thread(){
                                            @Override
                                            public void run(){
                                                if (dialog.isShowing()){
                                                    try {
                                                        requests.advancedSearch(city, numbers1, numbers2, numbers3, numbers4, letters1, letters2, captchaString, intent, dialog, combinations);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        };
                                        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Prekid", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                thread.interrupt();
                                            }
                                        });
                                        dialog.show();
                                        thread.start();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        AlertDialog confirmDialog = confirmBuilder.create();
                        confirmDialog.show();
                    }
                }
            }
        });

        advancedCaptcha.setOnFocusChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        //Toast.makeText(this, strings.toString(), Toast.LENGTH_LONG).show();
    }
}
