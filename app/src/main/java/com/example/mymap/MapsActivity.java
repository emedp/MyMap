package com.example.mymap;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleMap mMap; //mapa de google con el que se trabaja
    private GoogleApiClient mGoogleApiClient; //API de cliente para recoger la ubicacion
    private static final int LOCATION_REQUEST_CODE = 1; //peticion de la ubicacion
    private static final int REQUEST_LOCATION = 2; //obtencion de latitud y longitud

    private Marker Marca; //marca cambiante
    private Circle Zona; //circulo limitador de zona cambiante
    private Location myLocation; // variable para guardar posicion del usuario
    private LatLng myposition; //variable donde se guarda Latitud y Longitud

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

        //relacionado boton con boton del xml
        Button distancia = (Button) findViewById(R.id.distancia);
        distancia.setOnClickListener(this);
    }

    /**
     * este metodo sobreescribe el mapa vacio que da la clase googleMap, a este mapa se le ha añadido
     * la posición del usuario
     * una marca invisible que indica el tesoro
     * un circulo que delimita la zona donde se puede encontrar la marca
     *
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
            //al no cumplirse el if significa que el permiso no esta concedido por lo que se pide
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        //configuracion del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true); //botones de zoom activados
        mMap.getUiSettings().setMapToolbarEnabled(false); //toolbar innecesario desactivado

        //adición de marca invisible en el mapa del lugar del tesoro
        LatLng treasure = new LatLng(42.236905, -8.712710); //lugar de la marca
        Marca = mMap.addMarker(new MarkerOptions().position(treasure).title("tesoro").visible(false));

        //adición del circulo donde se encuentra la marca
        LatLng center = new LatLng(42.237024, -8.713554); //centro del circulo
        Zona = mMap.addCircle(new CircleOptions().center(center).radius(150).strokeColor(Color.parseColor("#084B8A")));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //comprueba si el permiso ACCESS_FINE_LOCATION ha sido concedido
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //comprueba si la petición es la de hacer la ubicación disponible
            if (requestCode == LOCATION_REQUEST_CODE) {
                //realiza el mismo codigo que en el caso de que ya estuvieran concedidos
                mMap.setMyLocationEnabled(true);
            }
            //comprueba si la petición es la de recoger la ubicación del usuario
            if (requestCode == REQUEST_LOCATION) {
                //realiza el mismo codigo que en el caso de que ya estuvieran concedidos
                myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (myLocation != null) {
                    myposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    //una vez cargada mi localización se anima el mapa hasta el circulo donde se esconde el tesoro
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myposition, 17));
                }
            }
        }
    }

    /**
     * cuando el cliente de google se conecta llama a este método para asignar la ubicación del usuario
     * con el atributo de clase myLocation
     *
     * @param bundle recoge el bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //se comprueba si el permiso del GPS esta autorizado
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //en caso de ya tener los permisos ejecuta el codigo correspondiente
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (myLocation != null) {
                myposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                //una vez cargada mi localización se anima el mapa hasta el circulo donde se esconde el tesoro
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myposition, 17));
            }
        } else {
            //al no cumplirse el if significa que el permiso no esta concedido por lo que se pide
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onClick(View v) {
        //cada vez que el usuario pulse el boton la variable myposition se actualizará gracias al API de cliente de google
        //con la posicion actualizada se obtiene la distancia precisa
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //en caso de ya tener los permisos ejecuta el codigo correspondiente
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (myLocation != null) {
                myposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            }
        }
        int distancia = (int) SphericalUtil.computeDistanceBetween(myposition, Marca.getPosition());
        if (distancia > 200) {
            Toast.makeText(this, "BRRR esto esta CONGELAO'\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia > 150) {
            Toast.makeText(this, "Hace algo de frío...\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia > 100) {
            Toast.makeText(this, "Templado templado\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia > 50) {
            Toast.makeText(this, "Algo de calor hace\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia > 20) {
            Toast.makeText(this, "¡MUY CALIENTE!\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia <= 20) {
            Toast.makeText(this, "¡Lo encontraste!", Toast.LENGTH_LONG).show();
            Marca.setVisible(true);
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