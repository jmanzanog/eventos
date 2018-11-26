package org.example.eventos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

public class Dialogo extends AppCompatActivity {
    private String idEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (getIntent().hasExtra("mensaje")) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Mensaje:");
            alertDialog.setMessage(extras.getString("mensaje"));
            try {
                idEvent = extras.getString("mensaje").split(":")[1].split("\n")[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                idEvent =  StringUtils.deleteWhitespace(extras.getString("mensaje"));
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CERRAR", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    Context context = getAppContext();
                    if (!(idEvent == null) && !"".equals(idEvent)) {
                        Intent intent = new Intent(context, EventoDetalles.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("evento", StringUtils.deleteWhitespace(idEvent));
                        context.startActivity(intent);

                    }else{
                        Intent intent = new Intent(context, ActividadPrincipal.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });
            alertDialog.show();
            extras.remove("mensaje");
        }
    }

    private Context getAppContext() {
        return this.getApplicationContext();
    }
}
