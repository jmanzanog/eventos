package org.example.eventos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static org.example.eventos.Comun.mFirebaseRemoteConfig;
import static org.example.eventos.Comun.mostrarDialogo;
import static org.example.eventos.EventosFirestore.EVENTOS;
import static org.example.eventos.EventosFirestore.crearEventos;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class ActividadPrincipal extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private AdaptadorEventos adaptador;
    private static ActividadPrincipal current;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_INVITE = 0;
    private boolean acercaDe;
    private String colorFondo;
    private boolean pais;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = getApplicationContext().getSharedPreferences("crashlyticsPref", MODE_PRIVATE);

        boolean doquestion= pref.getBoolean("crashlyticsQ", true);  // getting boolean
        boolean activateCrashlytics = pref.getBoolean("activateCrashlytics", true);
        if (doquestion){
           doCrashlyticsQuestion(this);
       }else{
            if (activateCrashlytics){
                Fabric.with(this, new Crashlytics());
            }
       }

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppInvite.API).enableAutoManage(this, this).build();
        setContentView(R.layout.actividad_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // crearEventos();
        Query query = FirebaseFirestore.getInstance().collection(EVENTOS).limit(50);
        FirestoreRecyclerOptions<Evento> opciones = new FirestoreRecyclerOptions.Builder<Evento>().setQuery(query, Evento.class).build();
        adaptador = new AdaptadorEventos(opciones);
        final RecyclerView recyclerView = findViewById(R.id.reciclerViewEventos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);

        final SharedPreferences preferencias = getApplicationContext().getSharedPreferences("Temas", Context.MODE_PRIVATE);
        if (preferencias.getBoolean("Inicializado", false) == false) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences("Temas", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Inicializado", true);
            editor.commit();
            FirebaseMessaging.getInstance().subscribeToTopic("Todos");
        }

        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildAdapterPosition(view);
                Evento currentItem = (Evento) adaptador.getItem(position);
                String idEvento = adaptador.getSnapshots().getSnapshot(position).getId();
                Context context = getAppContext();
                Intent intent = new Intent(context, EventoDetalles.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("evento", idEvento);
                context.startActivity(intent);
                finish();
            }
        });

        Comun.storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://eventos-dcda4.appspot.com");
        String[] PERMISOS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.GET_ACCOUNTS, android.Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, PERMISOS, 1);

        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink).setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
            @Override
            public void onResult(AppInviteInvitationResult result) {
                if (result.getStatus().isSuccess()) {
                    Intent intent = result.getInvitationIntent();
                    String deepLink = AppInviteReferral.getDeepLink(intent);
                    String invitationId = AppInviteReferral.getInvitationId(intent);
                    android.net.Uri url = Uri.parse(deepLink);
                    String descuento = url.getQueryParameter("descuento");
                    mostrarDialogo(getApplicationContext(), "Tienes un descuento del " + descuento + "% gracias a la invitación: " + invitationId);
                }
            }
        });
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_default);
        long cacheExpiration = 3600;
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFirebaseRemoteConfig.activateFetched();
                pais = mFirebaseRemoteConfig.getBoolean("pais");
                Boolean performanceMonitoring = mFirebaseRemoteConfig.getBoolean("PerformanceMonitoring");
                if(null!=performanceMonitoring &&performanceMonitoring){
                    FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
                }else{
                    FirebasePerformance.getInstance().setPerformanceCollectionEnabled(false);
                }
                getColorFondo();
                getAcercaDe();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
                acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adaptador.startListening();
        current = this;
    }

    @Override
    public void onStop() {
        super.onStop();
        adaptador.stopListening();
    }

    public static ActividadPrincipal getCurrentContext() {
        return current;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.keySet().size() > 4) {
            String evento = "";
            evento = "Evento: " + extras.getString("evento") + "\n";
            evento = evento + "Día: " + extras.getString("dia") + "\n";
            evento = evento + "Ciudad: " + extras.getString("ciudad") + "\n";
            evento = evento + "Comentario: " + extras.getString("comentario");
            mostrarDialogo(getApplicationContext(), evento);
            for (String key : extras.keySet()) {
                getIntent().removeExtra(key);
            }
            extras = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actividad_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_error) {
            Crashlytics.getInstance().crash();
            return true;
        }
        if (id == R.id.action_temas) {
            Intent intent = new Intent(getBaseContext(), Temas.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.enviar_evento) {
            Intent intent = new Intent(getBaseContext(), ActividadEnviarEvento.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_invitar) {
            invitar();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Context getAppContext() {
        return ActividadPrincipal.getCurrentContext();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ActividadPrincipal.this, "Has denegado algún permiso de la aplicación.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
    }

    private void invitar() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            } else {
                Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
            }
        }
    }

    private void getColorFondo() {
        colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
    }

    private void getAcercaDe() {
        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
    }

    private void doCrashlyticsQuestion(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Con el fin de mejorar la aplicación, te pedimos que participes en el envío automático de errores a nuestros servidores. ¿Estás de acuerdo?")
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        Fabric.with(context, new Crashlytics());
                        editor = pref.edit();
                        editor.putBoolean("activateCrashlytics", true);
                        editor.putBoolean("crashlyticsQ", false);           // Saving boolean - true/false
                        editor.commit();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editor = pref.edit();
                        editor.putBoolean("activateCrashlytics", false);
                        editor.putBoolean("crashlyticsQ", false);           // Saving boolean - true/false
                        editor.commit();


                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();

    }
}
