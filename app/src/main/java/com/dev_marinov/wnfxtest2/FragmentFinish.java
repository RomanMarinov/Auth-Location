package com.dev_marinov.wnfxtest2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FragmentFinish extends Fragment {

    View frag;
    List<ObjectTab> list = new ArrayList<>(); // массив для хранения объектов fragment
    ArrayList<Fragment> arrayList = new ArrayList<>(); // массив для хранения fragments
    LinearLayout ll_tab_1, ll_tab_2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.fragment_finish, container, false);

        Window window = getActivity().getWindow();
        // установка градиента анимации на toolbar
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        //Drawable background = getResources().getDrawable(R.drawable.gradient_1_2_3_list);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS Флаг, указывающий, что это Окно отвечает за отрисовку фона для системных полос.
        // Если установлено, системные панели отображаются с прозрачным фоном, а соответствующие области в этом окне заполняются цветами,
        // указанными в Window#getStatusBarColor()и Window#getNavigationBarColor().
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        window.setNavigationBarColor(getResources().getColor(android.R.color.black));
        //window.setBackgroundDrawable(background);

        TabLayout tabLayout = frag.findViewById(R.id.tablayout);

        ll_tab_1 = frag.findViewById(R.id.ll_tab_1);
        ll_tab_2 = frag.findViewById(R.id.ll_tab_2);

        list.add(new ObjectTab("1","карта")); // добавляем объет в массив
        list.add(new ObjectTab("2","список ресторанов"));  // добавляем объет в массив
        tabLayout.removeAllTabs();// удалить все закладки

// Таб карта
        TabLayout.Tab new_Tab_1 = tabLayout.newTab();
        new_Tab_1.setText("карта");
        new_Tab_1.setTag("0");
        tabLayout.addTab(new_Tab_1);
// Таб список ресторанов
        TabLayout.Tab new_Tab_2 = tabLayout.newTab();
        new_Tab_2.setText("список ресторанов");
        new_Tab_2.setTag("1");
        tabLayout.addTab(new_Tab_2);

        arrayList.add(new MapFragment());  // добавляем fragment в массив
        arrayList.add(new FragmentListFood()); // добавляем fragment в массив

        // устанавливаем слушателя для tabLayout
         tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() { //
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // если я кликаю на закладку я получаю из тега ноль или еденицу
                        String num_tab = tab.getTag().toString();
                        Log.e("frag_fin","-num_tab-" + num_tab);
                        if(num_tab.equals("0"))
                        {
                            ll_tab_1.setVisibility(View.VISIBLE);
                            ll_tab_2.setVisibility(View.GONE);
                        }
                        if(num_tab.equals("1"))
                        {
                            ll_tab_2.setVisibility(View.VISIBLE);
                            ll_tab_1.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }
                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });

        return frag;
    }
}