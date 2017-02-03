package com.example.mymap;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_REQUEST_CODE = 1; //peticion de la ubicacion
    private static final int REQUEST_LOCATION = 2; //obtencion de latitud y longitud
    private GoogleMap mMap; //mapa de google con el que se trabaja
    private GoogleApiClient mGoogleApiClient; //API de cliente para recoger la ubicacion
    private Location myLocation; //localizacion donde se guardan las variables de latitud y longitud

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // construccion del cliente google asignando los metodos de las interfaces que lo complementan
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    /**
     * @param googleMap se pasa como parametro el mapa de google sobre cual se va a trabajar
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; //asignacion del mapa con el atributo de clase instanciado

        //comprobación del permiso para poder utilizar el GPS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //si se ha concedido permiso la localizacion se hace visible
            mMap.setMyLocationEnabled(true);
        } else {
            //si no se tiene acceso al GPS se pide el permiso
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        //configuracion del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true); //botones de zoom activados
        mMap.getUiSettings().setMapToolbarEnabled(false); //toolbar innecesario desactivado

        //markers
        LatLng treasure_latlng = new LatLng(42.236905, -8.712710);
        mMap.addMarker(new MarkerOptions().position(treasure_latlng).title("tesoro"));
    }

    /**
     * Después de haber hecho una petición de permisos se tratan en este metodo, confirmando
     * el codigo que pedia ser devuelto
     *
     * @param requestCode  comprueba que peticion se esta tratando
     * @param permissions  no se trata
     * @param grantResults devuelve una lista con los permisos grantizados
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //petición para hacer la localizacion disponible
        if (requestCode == LOCATION_REQUEST_CODE) {
            //comprobacion de permisos
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        //petición para obtener la ubicación del usuario
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /**
                 * se asigna al atributo de clase myLocation la ultima ubicacion conocida del GPS
                 * a través de la petición al cliente de Google
                 */
                myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (myLocation != null) {

                } else {
                    Toast.makeText(this, "Ubicación no encontrada", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Permisos no otorgados", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * cuando el cliente de google se conecta llama a este método para asignar la ubicación del usuario
     * con el atributo de clase myLocation
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //se comprueba si el permiso del GPS no esta autorizado
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //si al conectar el cliente no tiene permisos para acceder al GPS se vuelven a pedir
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (myLocation != null) {

            } else {
                Toast.makeText(this, "Ubicación no encontrada", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * cuando se lanza la aplicación la API del cliente de google se conecta al servidor
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * en caso de que se pare la aplicacion el cliente de google se desconecta
     */
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}