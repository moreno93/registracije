package hr.riteh.moreno.registracije;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

public class DisplayCars extends AppCompatActivity {

    private static final String TAG = "DisplayCars";
    private ArrayList<Cars> cars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cars);

        Intent intent = getIntent();
        ArrayList<String> responses = Globals.data;

        new Parser(DisplayCars.this).execute(responses.toArray(new String[responses.size()]));

        Globals.data.clear();
    }
}
