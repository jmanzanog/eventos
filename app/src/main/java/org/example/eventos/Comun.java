package org.example.eventos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Comun {
    static final String URL_SERVIDOR = "https://cursofirebase.000webhostapp.com/notificaciones/";
    static String ID_PROYECTO = "eventos-dcda4";
    String idRegistro = "";
    static StorageReference storageRef;

    static void mostrarDialogo(final Context context
            , final String mensaje) {
        Intent intent = new Intent(context, Dialogo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", mensaje);
        context.startActivity(intent);
    }


    public static class registrarDispositivoEnServidorWebTask extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea = "";

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("iddevice", idRegistroTarea).appendQueryParameter("idapp", ID_PROYECTO);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "registrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
        }
    }

    public static void guardarIdRegistro(Context context, String idRegistro) {
        registrarDispositivoEnServidorWebTask tarea = new registrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = idRegistro;
        tarea.execute();
    }

    public static void eliminarIdRegistro(Context context) {
        desregistrarDispositivoEnServidorWebTask tarea = new desregistrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = FirebaseInstanceId.getInstance().getToken();
        tarea.execute();
    }

    public static class desregistrarDispositivoEnServidorWebTask extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea;

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("iddevice", idRegistroTarea).appendQueryParameter("idapp", ID_PROYECTO);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "desregistrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
        }
    }

    public static class enviarMensaje extends AsyncTask<Void, Void, String> {
        String apiKey = "AAAA1ArfQ1I:APA91bEUemPCptnU78JSOd4N74m1ARPSPFMbNb7VMCE1Mswb4DSh8NjdXTYBh4lW5KqvKpGAjEMwtHH3vBpohhmWYwgF9dmQQGiGZsIjm6gVZBRw1CI515TlozwBhpC1nb65ctC0o9Tg";
        //String apiKey = "AIzaSyBiMlIwLcDusYr2mBp2JBbqAoe9hPrulyo";
        public String mensaje = "";

        @Override
        protected String doInBackground(Void... voids) {
            String response;

            try {
                Uri.Builder constructorParametros = new Uri.Builder()
                        .appendQueryParameter("apiKey", apiKey)
                        .appendQueryParameter("idapp", ID_PROYECTO)
                        .appendQueryParameter("mensaje", mensaje);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "notificar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }
    }

    public static StorageReference getStorageReference() {
        return storageRef;
    }
}



