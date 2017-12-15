package com.example.benja.distribuidos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marcador;
    double lat = 0.0;
    double lng = 0.0;
    String mensaje1 = "";
    String direccion = "";
    String stationsJSON = "";
    LinkedList<Station> stations =  new LinkedList<Station>();
    String camion = "";
    String ruta = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        camion = intent.getStringExtra("placas");
        ruta = intent.getStringExtra("ruta");
        new getStations().execute(ruta);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        miUbicacion();
    }

    //activar los servicios del gps cuando estén apagados
    public void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
    }

    //Obtener la dirección de la calle a partir de la latitud y longitud
    public void setLocation(Location loc) throws IOException {
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (!list.isEmpty()) {
                Address DirCalle = list.get(0);
                direccion = (DirCalle.getAddressLine(0));
            }
        }
    }

    public String setLocation(double lat, double lng) throws IOException {
        if (lat != 0.0 && lng != 0.0) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);
            if (!list.isEmpty()) {
                Address DirCalle = list.get(0);
                return (DirCalle.getAddressLine(0));
            }
        }
        return "Sin dirección";
    }

    //Agregar el marcador en el mapa
    private void AgregarMarcador(double lat, double lng) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate MiUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        if (marcador != null)
            marcador.remove();
        marcador = mMap.addMarker(new MarkerOptions().position(coordenadas).title("Camión " + camion).snippet(direccion).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.animateCamera(MiUbicacion);
    }

    //actualizar la ubicación
    private void ActualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            AgregarMarcador(lat, lng);
        }
    }

    //control del gps
    LocationListener locListener = new LocationListener() {
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        @Override
        public void onLocationChanged(Location location) {
            ActualizarUbicacion(location);
            try {
                setLocation(location);
                Location actual = new Location("Actual location");
                actual.setLatitude(lat);
                actual.setLongitude(lng);
                for(int i = 0; i < stations.size(); i++){
                    Station st = stations.get(i);
                    Location auxST = new Location("Station " + st.getId());
                    auxST.setLatitude(Double.parseDouble(st.getLatitude()));
                    auxST.setLongitude(Double.parseDouble(st.getLongitude()));
                    double distance = actual.distanceTo(auxST);
                    if(distance <= 20){
                        mensaje1 = ("Estoy en la Estación " + st.getId());
                        Mensaje();
                        System.out.println("ESTOY EN LA ESTACIÓN " + st.getId());
                        new operacionSoap().execute(camion, ruta, st.getId());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            mensaje1 = ("GPS Activado");
            Mensaje();
        }

        @Override
        public void onProviderDisabled(String provider) {
            mensaje1 = ("GPS Desactivado");
            locationStart();
            Mensaje();
        }
    };

    private static int PETICION_PERMISO_LOCALIZACION = 101;

    private void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PETICION_PERMISO_LOCALIZACION);
            return;
        }else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            ActualizarUbicacion(location);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1200, 0, locListener);
        }
    }

    public void Mensaje() {
        Toast toast = Toast.makeText(this, mensaje1, Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class getStations extends AsyncTask<String, String, String>{
        static final String NAMESPACE = "http://WS/";
        static final String METHODNAME = "getStations";
        static final String URL = "http://18.217.115.39:8080/DistribuidosWS/DistribuidosWS?wsdl";
        static final String SOAP_ACTION = NAMESPACE + METHODNAME;

        protected String doInBackground(String... params) {
            SoapObject request = new SoapObject(NAMESPACE, METHODNAME);
            request.addProperty("route", params[0]);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = false;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            try{
                transporte.call(SOAP_ACTION, envelope);
                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                System.out.println(response.toString());
                stationsJSON =  response.toString();
                JSONObject obj = new JSONObject(stationsJSON);
                JSONArray stationArray = obj.getJSONArray("estaciones");
                for (int i = 0; i < stationArray.length(); i++) {
                    JSONObject stationDetail = stationArray.getJSONObject(i);
                    Station auxST = new Station();
                    auxST.setId(stationDetail.getString("id"));
                    auxST.setLatitude(stationDetail.getString("latitud"));
                    auxST.setLongitude(stationDetail.getString("longitud"));
                    stations.add(auxST);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            return "HAY " + stations.size() + " ESTACIONES :D";
        }

        protected void onPostExecute(String result) {
            System.out.println(result);
            for(int i = 0; i < stations.size(); i++) {
                Station st = new Station();
                st = stations.get(i);
                LatLng coordenadas = new LatLng(Double.parseDouble(st.getLatitude()), Double.parseDouble(st.getLongitude()));
                try {
                    mMap.addMarker(new MarkerOptions().position(coordenadas).title("Estacion " + st.getId()).snippet(setLocation(Double.parseDouble(st.getLatitude()), Double.parseDouble(st.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.st)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class operacionSoap extends AsyncTask<String, String, String>{
        static final String NAMESPACE = "http://WS/";
        static final String METHODNAME = "checkIn";
        static final String URL = "http://18.217.115.39:8080/DistribuidosWS/DistribuidosWS?wsdl";
        static final String SOAP_ACTION = NAMESPACE + METHODNAME;

        @Override
        protected String doInBackground(String... params) {
            SoapObject request = new SoapObject(NAMESPACE, METHODNAME);
            request.addProperty("placas", params[0]);
            request.addProperty("ruta", params[1]);
            request.addProperty("estacion", params[2]);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = false;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            try{
                transporte.call(SOAP_ACTION, envelope);
                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                Log.d("response", response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
