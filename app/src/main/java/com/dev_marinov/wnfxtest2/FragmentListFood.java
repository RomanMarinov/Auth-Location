package com.dev_marinov.wnfxtest2;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class FragmentListFood extends Fragment {

    View frag;
    RecyclerView recyclerView;  // для списка ресторанов
    AdapterList adapterList; // для отображения списка ресторанов
    HashMap<Integer, ObjectList> hashMapList = new HashMap<>(); // массив для хранения списка ресторанов
    int z = -1; // счетчик-индекс для записи в массив hashMapList
    String save_my_lat_long; // переменная для хранения широты и долготы переданных от frag_map, которую получаю при загрузки карты
    int count_next_page_token = 0;
    boolean myBoolnext_page_token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.fragment_list_food, container, false);

        recyclerView = frag.findViewById(R.id.rv_list);
        adapterList = new AdapterList(getContext(), hashMapList);
        recyclerView.setLayoutManager(new LinearLayoutManager(frag.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapterList);

        hashMapList.clear();

        return frag;
    }

    // метод запроса у google map списка реаторанов
    // my_lat_long - широта и долга это мое гео, пришло от frag_map
    // token_nextpage - который я получаю из ответа google map для увеличения списка ресторанов
    // т.к. один запрос это 20 адресов, чтобы получить больше нужен токен
    // первый запрос будет token_nextpage - null
    public void startRequestListFood(String my_lat_long,String token_nextpage)
    {
            Log.e("test","-token_nextpage-ПРОШЕЛ="+token_nextpage);
        // сохраняю мое гео в отдельную переменную только для этого
        save_my_lat_long = my_lat_long;

        // параметры для запроса в google map
        RequestParams requestParams = new RequestParams();
        requestParams.put("location", save_my_lat_long); // широта, долгота
        requestParams.put("radius", "50000"); // радиус
        requestParams.put("type", "restaurant"); // категория поиска (рестораны)
        //requestParams.put("request_count", count_next_page_token); // костыль

        //requestParams.put("keyword", "");
        if (token_nextpage != null) // проверка на null (первый раз будет null)
        {
            // token_nextpage - который я получаю из ответа google map для увеличения списка ресторанов
            requestParams.put("pagetoken", token_nextpage);
        }
        // key - который я взял с сайта google map cloud для доступа к карте(есть key еще для markers)
        requestParams.put("key", "AIzaSyDbjAPSWXxoup3RRkEYWb1RupO1fhUBBlg");

        // Библиотека asyncHttpClient позволяет асинхронно делать запросы к серверу и скачивать и загружать файлы, картинки, JSON, XML,
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        // строка запроса + параметры requestParams
        asyncHttpClient.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json",
                requestParams, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e("test","onFailure_te="+responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.e("test","onSuccess_te="+responseString);

                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            Log.e("results","results="+jsonObject.length());

                            JSONArray results = jsonObject.getJSONArray("results");
                            Log.e("results","ar list food ="+results.length());

                            int show_count =0;
                            int count_all = 0;

                            // цикл для списка ресторанов, Перебирем все элементы чтобы было 4 шт в ряд
                            for (int i = 0; i < results.length(); i++) {

                                show_count++;
                                if (show_count == 4) //есть 4 элемента.
                                {count_all = count_all+4;
                                    JSONObject myresults_1 = results.getJSONObject(i);
                                    String name_1 = myresults_1.getString("name");
                                    JSONObject myresults_2 = results.getJSONObject(i-1);
                                    String name_2 = myresults_2.getString("name");
                                    JSONObject myresults_3 = results.getJSONObject(i-2);
                                    String name_3 = myresults_3.getString("name");
                                    JSONObject myresults_4 = results.getJSONObject(i-3);
                                    String name_4 = myresults_4.getString("name");
                                    show_count = 0;
                                    z++;
                                    //заполняем 4 элемента
                                    hashMapList.put(z, new ObjectList(name_1, name_2, name_3, name_4));
                                    Log.e("проверка","-name_1 =" + name_1 +
                                            "-name_2 =" + name_2 +
                                            "-name_3 =" + name_3 +
                                            "-name_4 =" + name_4);
                                }
                            }

//                            // если было например 6 элементов, 4 отобразили, 2 от осталось.
                            // ТОГДА КОГДА ОТ ГУГЛА ПРИДЕТ НЕ 20 МЕТОК, А 23 НАПРИМЕР
//                            if (count_all < results.length()) //проверям сколько осталось? Всего элементов-уже на экране.
//                            {
//                                for (int i = count_all+1; i < results.length(); i++) {
//                                    if ( (results.length()-count_all) == 1) {
//                                        JSONObject myresults_1 = results.getJSONObject(i);
//                                        String name_1 = myresults_1.getString("name");
//                                        z++;
//                                        hashMapList.put(z, new ObjectList(name_1, "", "", ""));
//                                    }
//
//                                    if ( (results.length()-count_all) == 2) {
//                                        JSONObject myresults_1 = results.getJSONObject(i);
//                                        String name_1 = myresults_1.getString("name");
//                                        JSONObject myresults_2 = results.getJSONObject(i-1);
//                                        String name_2 = myresults_2.getString("name");
//                                        z++;
//                                        hashMapList.put(z, new ObjectList(name_1,name_2, "", ""));
//                                    }
//
//                                    if ( (results.length()-count_all) == 3) {
//                                        JSONObject myresults_1 = results.getJSONObject(i);
//                                        String name_1 = myresults_1.getString("name");
//                                        JSONObject myresults_2 = results.getJSONObject(i-1);
//                                        String name_2 = myresults_2.getString("name");
//                                        JSONObject myresults_3 = results.getJSONObject(i-2);
//                                        String name_3 = myresults_3.getString("name");
//                                        z++;
//                                        hashMapList.put(z, new ObjectList(name_1,name_2, name_3, ""));
//                                    }
//                                }
//                            }


                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                // чтобы небыло Exception сначала проверяем если ключ "next_page_token" в jsonObject
                                                // -next_page_token- для добавления еще 20 организаций
                                                myBoolnext_page_token = keyExists(jsonObject, "next_page_token");

                                                Log.e("проверка","-myBoolnext_page_token =" + myBoolnext_page_token);
                                                if(myBoolnext_page_token) // если есть (tru)
                                                {
                                                    count_next_page_token ++; // счетчик для дальнейшего ограничения
                                                    // получаю токен
                                                    String my_next_page_token = jsonObject.getString("next_page_token");
                                                    Log.e("next_page_token", "-next_page_token-" + my_next_page_token);

                                                    if(count_next_page_token <= 5) // больше 5 запросов на токен нельзя, чтобы не перегружать
                                                    {
                                                        Log.e("next_page_token1", "-count_next_page_token-" + count_next_page_token
                                                                + "-save_my_lat_long-" + save_my_lat_long + "-my_next_page_token-" + my_next_page_token);
                                                        // запуск startRequestListFood для заполнения массива hashMapList + новыми ресторанами
                                                        startRequestListFood(save_my_lat_long, my_next_page_token);
                                                    }
                                                }
                                                else
                                                {
                                                    Log.e("проверка","-myBoolnext_page_token БОЛЬШЕ НЕТ=" + myBoolnext_page_token);
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                Log.e("проверка","-myBoolnext_page_token TRY CATCH =" + e);
                                            }

                                        }
                                    },4000);





                            // обновление адаптера
                            postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    adapterList.notifyDataSetChanged();
                                    Log.e("hashMapList","-hashMapList="+hashMapList.size());
                                }
                            });

                        } catch (Exception e) {
                            Log.e("results","проходка results try catch="+e);
                        }
                    }
                });
    }

    // метод для проверки налиичия ключа в json при закросе
    public boolean keyExists(JSONObject object, String searchedKey) {
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
                    Log.e("results","e.printStackTrace() try catch="+e);
                }
            }
        }
        return exists;
    }

}