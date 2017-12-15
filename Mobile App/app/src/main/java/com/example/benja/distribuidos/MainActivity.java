package com.example.benja.distribuidos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] arraySpinner = new String[] {"Ruta 20", "Ruta 40"};
        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        s.setAdapter(adapter);
    }

    public void abreMapa(View view){
        EditText p = (EditText) findViewById(R.id.placas);
        Spinner s = (Spinner) findViewById(R.id.spinner);
        String placas = p.getText().toString();
        String ruta = s.getSelectedItem().toString().split(" ")[1];
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("placas", placas);
        intent.putExtra("ruta", ruta);
        startActivity(intent);
    }
}
