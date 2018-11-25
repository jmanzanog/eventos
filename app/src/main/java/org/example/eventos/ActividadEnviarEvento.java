package org.example.eventos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;

public class ActividadEnviarEvento extends AppCompatActivity {

    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enviar_evento);
        editText = findViewById(R.id.editText2);
        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comun.enviarMensaje tarea = new Comun.enviarMensaje();
                tarea.mensaje = editText.getText().toString();
                tarea.execute();
                finish();
            }
        });

    }

}
