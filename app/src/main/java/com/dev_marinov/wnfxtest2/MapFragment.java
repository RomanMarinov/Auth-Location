package com.dev_marinov.wnfxtest2;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    View frag;
    private GoogleMap mGoogleMap;
    private MapView mMapView; // представление для управления жизненным циклом объекта GoogleMap
    private DatabaseReference databaseReference; // firebase
    private LocationListener locationListener; // слушатель
    private LocationManager locationManager;
    private final long MIN_TIME = 1000; // милисекунды
    private final long MIN_DIST = 5; // метров
    TextView tv_lat_long_map;
    Button bt_map; // кнопка Обновить
    HashMap<Integer, ObjectMap> hashMapMap = new HashMap(); // массив для маркеров
    HashMap<Integer, ObjectListPhoto> hashMapListPhoto = new HashMap(); // массив для массивов фото реаторана
    RecyclerView rv_list_photo;
    AdapterListPhoto adapterListPhoto;
    String lat_lng_hashmap, lat_lng, lat_lng_new; // переменные для хранения широты и долготы
    ImageView img_my; // для картинки если нет фотографии ресторана (извините, картинки нет)
    int z = -1; // счетчик - индекс для заполнения hashMapMap
    String API_MAP_MARKER = "AIzaSyDbjAPSWXxoup3RRkEYWb1RupO1fhUBBlg";
    LatLng latLng; // класс для широты и долготы
    TextView tv_name, tv_phone_number, tv_address; // для отображения инфо о ресторане
    ConstraintLayout cl_info;
    String marker_name;
    String latLocListener; // хранение широты
    String lngLocListener; // хранение долготы
    String temp; // временная переменная
    int count_next_page_token = 0; // счетчик для ограничения запросов на nextpagetoken
    String my_next_page_token;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // Вызывается сразу после onCreateView()
        super.onViewCreated(view, savedInstanceState);
        Log.e("проверка_FRAG_MAP", "-onViewCreated зашел-");
        mMapView = frag.findViewById(R.id.google_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            // getMapAsync Устанавливает объект обратного вызова, который будет запускаться,
            // когда экземпляр GoogleMap будет готов к использованию
            mMapView.getMapAsync(this); //
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.fragment_map, container, false);

        tv_lat_long_map = frag.findViewById(R.id.tv_lat_long_map);
        bt_map = frag.findViewById(R.id.bt_map);
        rv_list_photo = frag.findViewById(R.id.rv_list_photo);
        img_my = frag.findViewById(R.id.img_my);
        tv_name = frag.findViewById(R.id.tv_name);
        tv_phone_number = frag.findViewById(R.id.tv_phone_number);
        tv_address = frag.findViewById(R.id.tv_address);
        cl_info = frag.findViewById(R.id.cl_info);

        adapterListPhoto = new AdapterListPhoto(getContext(), hashMapListPhoto);
        rv_list_photo.setLayoutManager(new LinearLayoutManager(frag.getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_list_photo.setAdapter(adapterListPhoto);

        // в databaseReference храниться ссылка на сайт firebase realtime
            databaseReference = FirebaseDatabase.getInstance().getReference("Location");
                databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Log.e("проверка_FRAG_MAP", "-databaseReference -onDataChange- зашел -");
                    // получение данных latitude
                    String databaseLatitudeString = dataSnapshot.child("latitude").getValue().toString()
                            .substring(1,dataSnapshot.child("latitude").getValue().toString().length()-1);
                        // получение данных longitude
                    String databaseLongitudeString = dataSnapshot.child("longitude").getValue().toString()
                            .substring(1,dataSnapshot.child("longitude").getValue().toString().length()-1);

                    // разделение данных строки и запись в массив string
                    String[] stringLat = databaseLatitudeString.split(", ");
                    Arrays.sort(stringLat); // сортировка данных массива
                    String latitude = stringLat[stringLat.length - 1].split("=")[1];

                    String[] stringLong = databaseLongitudeString.split(", ");
                    Arrays.sort(stringLong);
                    String longitude = stringLong[stringLong.length - 1].split("=")[1];

                    // установка точки местоположения на карте по строковым координатам latitude и longitude
                    latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
// "57.0395694,40.9945142"
                        Log.e("проверка_FRAG_MAP", "-databaseReference -onDataChange- latLng === " + latLng.toString());
                    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(latitude + " , " + longitude));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                        // программная установка mapbox
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 8);
                        mGoogleMap.animateCamera(cameraUpdate);
                        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
                        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
                        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                        mGoogleMap.setPadding(0,0,0,500);
                       // mGoogleMap.getUiSettings().setIndoorLevelPickerEnabled(true);

                    } catch (Exception e) {
                        Log.e("проверка_FRAG_MAP", "-try_catch_1-" + e);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("проверка_FRAG_MAP", "-onCancelled -error- " + error);
                }
            });

                // вроде не влияет не на что
        //myQueryMap(null); // метод для запроса ресторанов
//        addMarkers(); // метод для утсановки маркеров ресторанов из массива
//

                // ВКЛЮЧИТЬ КОГДА ВСЕ БУДЕТ ГОТОВО
        // кнопка обновления координат и запись в databaseReference, т.е. в firebase realtime
        bt_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("latitude").push().setValue(latLocListener);
                databaseReference.child("longitude").push().setValue(lngLocListener);

                // при каждом нажатии bt_map буду найден я на карте
                mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });

        return frag;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) { // Вызывается, когда карта готова к использованию.
            mGoogleMap = googleMap;
        Log.e("проверка_FRAG_MAP", "-onMapReady сработал- mGoogleMap = " + mGoogleMap);

            locationListener = new LocationListener() {
            // Метод onLocationChanged будет вызван при изменении текущего местоположения.
            // Этот метод вызывается по умолчанию, поскольку вы реализовали LocationListener.
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {

//                    edt_map_1.setText(Double.toString(location.getLatitude())); // запись в edittext широты
//                    edt_map_2.setText(Double.toString(location.getLongitude())); // запись в edittext долготы
                    Log.e("проверка_FRAG_MAP", "-onLocationChanged сработал-");
                    latLocListener = Double.toString(location.getLatitude());
                    lngLocListener = Double.toString(location.getLongitude());
                    temp = (location.getLatitude()) + "," + (location.getLongitude());
                    // отображение инфо о ресторане широта и долгота
                    tv_lat_long_map.setText(location.getLatitude() + "," + (location.getLongitude()));

                    // получение доступа к родительскому фрагменту
                    FragmentFinish fragmentFinish = (FragmentFinish) getActivity().getSupportFragmentManager().findFragmentById(R.id.ll_frag_finish);
                        Log.e("проверка_FRAG_MAP", "-fragmentFinish-" + fragmentFinish);
                    if(fragmentFinish != null)
                    {
//                        Log.e("FRAG_MAP", "-fragmentFinish ПРОШЕЛ-");
//                        if(fragmentFinish.getTag().equals("finish"))
//                        {
                        // получение доступа в детям фрагментам
                        FragmentListFood fragmentListFood = (FragmentListFood) fragmentFinish.getChildFragmentManager().findFragmentById(R.id.frag_list_food);
                        if(fragmentListFood != null)
                        {
                            Log.e("проверка_FRAG_MAP", "-fragmentListFood ПРОШЕЛ-");
                            // передача фрагменту fragmentListFood широты и долготы где нахожусь я
                            fragmentListFood.startRequestListFood(temp,null);
                            Log.e("проверка_FRAG_MAP", "-fragmentListFood ПРОШЕЛ 2 -" + temp);
                        }
                  //      }
                    }

                } catch (Exception e) {
                    Log.e("проверка_FRAG_MAP", "-try_catch_3-" + e);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e("проверка_FRAG_MAP", "-onStatusChanged- status- " + status + "-extras-" + extras);
            }
            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.e("проверка_FRAG_MAP", "-onProviderEnabled- provider- " + provider);
            }
            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.e("проверка_FRAG_MAP", "-onProviderDisabled- provider- " + provider);
            }
        };

        // установка LOCATION_SERVICE
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Log.e("проверка_FRAG_MAP", "-onMapReady- locationManager- " + locationManager);
        // если
        if (checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("проверка_FRAG_MAP", "-onMapReady  checkSelfPermission");
            // TODO: рассмотрите возможность звонка
            // ActivityCompat # requestPermissions
            // здесь, чтобы запросить недостающие разрешения, а затем переопределить
            // public void onRequestPermissionsResult (int requestCode, String [] permissions,
            // int [] grantResults)
            // для обработки случая, когда пользователь предоставляет разрешение. См. Документацию
            // для ActivityCompat # requestPermissions для более подробной информации.
            return;
        }
        try {
            Log.e("проверка_FRAG_MAP", "-onMapReady  onLocationChanged locationListener == " + locationListener);
            //locationManager класс обеспечивает доступ к системным службам определения местоположения.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } catch (Exception e) {
            Log.e("проверка_FRAG_MAP", "-try_catch_4-" + e);
        }
        // клик на маркет
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                hashMapListPhoto.clear(); // очистка массив фоток
                tv_phone_number.setText(""); // очистка номера телефона
                cl_info.setVisibility(View.VISIBLE); // открытие контерена с инфо о ресторане
                img_my.setVisibility(View.GONE); // закрытие фото (нет фото)
                rv_list_photo.setVisibility(View.VISIBLE); // показать recycler после нажатия mGoogleMap.setOnMapClickListener


                // исключаю скобки и пробелы
                lat_lng = marker.getPosition().toString().replaceAll("[\\[\\](){}]","").trim();
                // исключаю lat/lng: и пробелы
                lat_lng_new = lat_lng.replaceAll("lat/lng:", "").trim();
                String myPlace_id= "";
                // перебираем массив ресторанов и получаем place_id нажатого маркера
                for (int i = 0; i < hashMapMap.size(); i++) {
                    lat_lng_hashmap = (hashMapMap.get(i).lat + "," + hashMapMap.get(i).lng).trim();
                    if (lat_lng_new.equals(lat_lng_hashmap)) {
                        myPlace_id = hashMapMap.get(i).place_id;

                        tv_lat_long_map.setText(lat_lng_new); // и устанавливаем для отображения широту и долготу маркера
                        Log.e("проверка_FRAG_MAP","-tt-НАЙДЕНО myPlace_id =" + myPlace_id);
                    }
                }
                        // параметры для зарпоса инфо о ресторане по myPlace_id нажатого маркера
                        RequestParams requestParams = new RequestParams();
                        requestParams.put("place_id", myPlace_id);

                        requestParams.put("key", API_MAP_MARKER);

                        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                        asyncHttpClient.get("https://maps.googleapis.com/maps/api/place/details/json",
                                requestParams, new TextHttpResponseHandler() {
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        Log.e("проверка_FRAG_MAP","onFailure_te="+responseString);
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                        Log.e("проверка_FRAG_MAP","onSuccess_te myPlace_id ="+responseString);


                                            try {
                                                JSONObject jsonObject = new JSONObject(responseString);

                                                // чтобы небыло exeption сначала проверяем если ключ "photos" в jsonObject
                                                boolean myBoolPhotos = keyExists(jsonObject, "photos");

                                                Log.e("проверка_FRAG_MAP","-myBoolPhoto =" + myBoolPhotos);
                                                if(myBoolPhotos) // если есть (tru)
                                                {
                                                    // получение фоток
                                                    JSONArray jsonFoto = jsonObject.getJSONObject("result").getJSONArray("photos");

                                                    for (int i = 0; i < jsonFoto.length()-1; i++) {
                                                        JSONObject photo_obe_1 = jsonFoto.getJSONObject(i);
                                                        String photo_reference = photo_obe_1.getString("photo_reference");

                                                        hashMapListPhoto.put(i, new ObjectListPhoto(photo_reference));
                                                    }
                                                    Log.e("проверка_FRAG_MAP","-hashMapListPhoto="+hashMapListPhoto.size());
                                                }
                                                else
                                                {
                                                    hashMapListPhoto.clear(); // очистка массива фоток
                                                    img_my.setVisibility(View.VISIBLE); // открытие фотки (извините нет фото)
                                                    img_my.setImageResource(R.drawable.not_find); // фотка (извините нет фото)
                                                }

                                                // получение телефона
                                                // чтобы небыло exeption сначала проверяем если ключ "myBoolphone_number" в jsonObject
                                                boolean myBoolphone_number = keyExists(jsonObject, "formatted_phone_number");

                                                Log.e("проверка_FRAG_MAP","-myBoolphone_number =" + myBoolphone_number);
                                                if(myBoolphone_number) // если есть (tru)
                                                {
                                                    // получение phone_number
                                                    String phone_number = jsonObject.getJSONObject("result").getString("formatted_phone_number");
                                                    Log.e("проверка_FRAG_MAP","-phone_number="+phone_number);
                                                    tv_phone_number.setText("телефон: " + phone_number);
                                                }
                                                else
                                                {
                                                    tv_phone_number.setText("нет");
                                                }

                                                // получение адреса
                                                // чтобы небыло exeption сначала проверяем если ключ "formatted_address" в jsonObject
                                                boolean myBoolformatted_address = keyExists(jsonObject, "formatted_address");

                                                Log.e("проверка_FRAG_MAP","-myBoolformatted_address =" + myBoolformatted_address);
                                                if(myBoolformatted_address) // если есть (tru)
                                                {
                                                    // получение address
                                                    String address = jsonObject.getJSONObject("result").getString("formatted_address");
                                                    Log.e("проверка_FRAG_MAP","-phone_number="+address);
                                                    tv_address.setText("адрес: " + address);
                                                }
                                                else
                                                {
                                                    tv_address.setText("нет");
                                                }

                                                // получение названия
                                                // чтобы небыло exeption сначала проверяем если ключ "name" в jsonObject
                                                boolean myBoolName = keyExists(jsonObject, "name");

                                                Log.e("проверка_FRAG_MAP","-myBoolName =" + myBoolName);
                                                if(myBoolName) // если есть (tru)
                                                {
                                                    // получение названия ресторана
                                                    marker_name = jsonObject.getJSONObject("result").getString("name");
                                                    Log.e("проверка_FRAG_MAP","-name="+marker_name);

                                                    tv_name.setText(marker_name);
                                                }
                                                else
                                                {
                                                    tv_name.setText("нет");
                                                }
                                                // обновление адаптера
                                                postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        rv_list_photo.smoothScrollToPosition(0);
                                                        adapterListPhoto.notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                            catch (Exception e)
                                            {
                                                Log.e("проверка_FRAG_MAP", "-try_catch_5-" + e);
                                            }
                                    }
                                });
                return false;
            }
        });

        // нажатие на карту, кроме маркера чтобы отключить контейнер с инфо о реаторане
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                cl_info.setVisibility(View.GONE);
                rv_list_photo.setVisibility(View.GONE);
                // исключаю скобки и пробелы
                String latlngOnMapClick = latLng.toString().replaceAll("[\\[\\](){}]","").trim();
                // исключаю lat/lng: и пробелы
                String lat_lng_OnMapClick = latlngOnMapClick.replaceAll("lat/lng:", "").trim();
                Log.e("проверка_FRAG_MAP", "-latLng-setOnMapClickListener-" + latLng);
                tv_lat_long_map.setText(lat_lng_OnMapClick);
            }
        });

        // метод для заполнения массива hashMapMap списком ресторанов
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myQueryMap(null);
                            // addMarkers(); // метод для утсановки маркеров ресторанов из массива
                        }
                    });


                }
            },2000);


}

// метод для заполнения массива hashMapMap списком ресторанов
    public void myQueryMap(String next_page_token)
    {
        Log.e("проверка_FRAG_MAP","myQueryMap ВЫПОЛНИЛСЯ=");

        // параметры для запроса списка ресторанов
        RequestParams requestParams = new RequestParams();
        requestParams.put("location", latLocListener+","+lngLocListener);
        //requestParams.put("location", "57.0395167,40.994431");
        requestParams.put("radius", "10000");
        requestParams.put("type", "restaurant");
        if (next_page_token != null)
        {
            requestParams.put("pagetoken", next_page_token);
        }
        //requestParams.put("keyword", "");
        requestParams.put("key", API_MAP_MARKER);

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json",
                requestParams, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e("проверка_FRAG_MAP","onFailure_te="+responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.e("проверка_FRAG_MAP","onSuccess_first="+responseString);

                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                              Log.e("проверка_FRAG_MAP","results="+jsonObject.length());

                            JSONArray results = jsonObject.getJSONArray("results");
                            Log.e("проверка_FRAG_MAP","ar="+results.length());

                            // цикл для меток карты
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject myresults = results.getJSONObject(i);
                                String name = myresults.getString("name");
                                String lat = myresults.getJSONObject("geometry").getJSONObject("location").getString("lat");
                                String lng = myresults.getJSONObject("geometry").getJSONObject("location").getString("lng");
                                String place_id = myresults.getString("place_id");
                                z++;
                                // заполение массива списком ресторанов
                                hashMapMap.put(z, new ObjectMap(name, lat, lng, place_id));
                                Log.e("проверка_FRAG_MAP","-hashMapMap MAP_FRAG =" + hashMapMap.size());

                            }

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        try {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        addMarkers();
                                                    }
                                                    catch (Exception e)
                                                    {
                                                        Log.e("проверка_FRAG_MAP", "-etivity().runOnUiT try catch-" + e);
                                                    }
                                                }
                                            });
                                        }

                                        catch (Exception e)
                                        {
                                            Log.e("проверка_FRAG_MAP", "-ew Timer().schedu try catch-" + e);
                                        }
                                    }
                                },1000);

                                    // проверяем наличие ключа "next_page_token" в jsonObject
                                    boolean myBoolnext_page_token = keyExists(jsonObject, "next_page_token");
                                    Log.e("проверка_FRAG_MAP","-map_frag_myBoolnext_page_token =" + myBoolnext_page_token);

                                    count_next_page_token ++;
                                    Log.e("проверка_FRAG_MAP","count_next_page_token frag map" + count_next_page_token);

                                    if(myBoolnext_page_token) // если есть (tru)
                                    {

                                    // -next_page_token- для добавления еще 20 организаций
                                    my_next_page_token = jsonObject.getString("next_page_token");
                                        Log.e("проверка_FRAG_MAP", "-next_page_token_FRAG_map-" + my_next_page_token);
                                            new Timer().schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    if(count_next_page_token <= 3)
                                                    {
                                                            try {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                 myQueryMap(my_next_page_token);
                                                            }
                                                        });
                                                            }
                                                            catch (Exception e)
                                                            {
                                                            Log.e("проверка_FRAG_MAP", "-token try catch-" + e);
                                                            }
                                                    }
                                                    else
                                                    {
                                                        cancel();
                                                    }
                                                }
                                            },2000);
                                    }
                                    else
                                    {
                                        Log.e("проверка_FRAG_MAP", "-myBoolnext_page_token токена больше нет-" + myBoolnext_page_token);
                                    }

                        } catch (Exception e) {
                            Log.e("проверка_FRAG_MAP", "-try_catch_6-" + e);
                        }
                    }
                });
    }

    public void addMarkers() // метод для установки маркеров полученных (ресторанов с иконкой, зумом)
    {

        Log.e("проверка_FRAG_MAP","addMarkers ВЫЗВАЛСЯ");
        //mGoogleMap.clear();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < hashMapMap.size(); i++) {
                            String markers_latitude = hashMapMap.get(i).lat;
                            String markers_longitude = hashMapMap.get(i).lng;

                            Log.e("проверка_FRAG_MAP","hashMapMap addMarkers широта" + hashMapMap.get(i).lat
                                    + " долгота " + hashMapMap.get(i).lng);

                            // установка точки местоположения на карте по строковым координатам latitude и longitude
                            LatLng latLng = new LatLng(Double.parseDouble(markers_latitude), Double.parseDouble(markers_longitude));
                            mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                                    .icon(vectorToBitmap(R.drawable.ic_location, Color.parseColor("#A4C639"))));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            float zoomLevel = 12.0f; //This goes up to 21
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                        }
                    }
                });
            }
        },1000);
    }

// метод для преобразования файла drawable в BitmapDescriptor
// (для установки маркеров ресторанов)
    public BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    // метод для проверки налиичия ключа в json при закросе
    public boolean keyExists(JSONObject  object, String searchedKey) {
        boolean exists = object.has(searchedKey);
        if(!exists) {
            Iterator<?> keys = object.keys();
            while( keys.hasNext() ) {
                String key = (String)keys.next();
                try {
                    if ( object.get(key) instanceof JSONObject ) {
                        exists = keyExists((JSONObject) object.get(key), searchedKey);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("проверка_FRAG_MAP", "-try_catch_2-" + e);
                }
            }
        }
        return exists;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}