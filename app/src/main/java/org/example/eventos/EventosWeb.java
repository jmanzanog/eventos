package org.example.eventos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class EventosWeb extends AppCompatActivity {
    private String evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventos_web);
        Bundle extras = getIntent().getExtras();
        evento = extras.getString("evento");
    }
}
