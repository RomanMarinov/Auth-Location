package com.dev_marinov.wnfxtest2;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentSuccess extends Fragment {

    FirebaseAuth firebaseAuth;
    Button bt_out, bt_nextMap;
    TextView tv_out;
    View frag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        frag = inflater.inflate(R.layout.fragment_success, container, false);

        bt_out = frag.findViewById(R.id.bt_out);
        bt_nextMap = frag.findViewById(R.id.bt_nextMap);
        tv_out = frag.findViewById(R.id.tv_out);

        // анимация для фона
        ConstraintLayout constraintLayout = frag.findViewById(R.id.cl_main);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        // инициализация firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // получение доступа ко всем фрагментам по тегу и удаление их из стека
        // как только открывается FragmentSuccess
        for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments())
        {
            if (fragment.getTag() != null)
            {
                if (fragment.getTag().equals("enter_sms"))
                {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(fragment);
                }
                if (fragment.getTag().equals("success"))
                {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(fragment);
                }
            }
        }

        // кнопка разлогиниться
        bt_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseAuth.signOut();
                checkUserStatus();
            }
        });

        // кнопка перехода
        bt_nextMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentFinish fragmentFinish = new FragmentFinish();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.ll_frag_finish, fragmentFinish);
                fragmentTransaction.commit();


//                Fragment fragment = new MapFragment();
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.ll_frag_finish, fragment).commit();
            }
        });

        return frag;
    }

    private void checkUserStatus() {
            // получить текущего юзера
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null)
            {
            // юзер залогинился
            String phone = firebaseUser.getPhoneNumber();
            tv_out.setText("авторизованный пользователь \n" + phone);
            }
            else
            {
            // юзер не залогинился
               getActivity().finish();
            }

    }


}