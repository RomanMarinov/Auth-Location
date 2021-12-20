package com.dev_marinov.wnfxtest2;

import android.app.ProgressDialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;

import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class FragmentAuth extends Fragment {
    View frag;
    Button bt_getCode;
    EditText edt_phone;
    String phone;
    ProgressBar progressBar;
    TextView tv_blink;
    Animation anim;
    TextWatcher textWatcher;
    Boolean changed = false; // для срабатывания textWatcher один раз
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.fragment_auth, container, false);

        tv_blink = frag.findViewById(R.id.tv_blink);
        bt_getCode = frag.findViewById(R.id.bt_getCode);
        edt_phone = frag.findViewById(R.id.edt_phone);
        progressBar = frag.findViewById(R.id.progressBar);
        edt_phone.setText("+7("); // чтобы курсор встал после скобки
        edt_phone.setSelection(3);

        // анимация для фона
        ConstraintLayout constraintLayout = frag.findViewById(R.id.cl_main);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        // textWatcher только ради прекрашения анимации tv_blink, если весь номер заполнен
        textWatcher = new TextWatcher() {
            boolean considerChange = false;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { // до изменения текста
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)  { // при изменении текста
                    changed = true;
            }
            @Override
            public void afterTextChanged(Editable editable) { // после изменения текста
                if (considerChange)
                {
                    if(editable.length() == 17)
                    {
                        tv_blink.setVisibility(View.INVISIBLE);
                        tv_blink.clearAnimation();
                    }
                }
                considerChange = !considerChange; // меняю на false
            }
        };
        edt_phone.addTextChangedListener(textWatcher);

        // кнопка "получить код"
        bt_getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("333MAIN_AUTH"," НАЖАЛ ПОЛУЧИТЬ КОД");
                phone = edt_phone.getText().toString().trim();
                if(phone.length() != 17)
                {
                    //Toast.makeText(getContext(), "пожалуйста введите номер телефона", Toast.LENGTH_SHORT).show();
                    tv_blink.setVisibility(View.VISIBLE); // анимация на не правильный ввод номера телефона
                    anim = AnimationUtils.loadAnimation(getContext(), R.anim.blink);
                    tv_blink.startAnimation(anim);
                }
                else
                {
                    ProgressBar progressBar = (ProgressBar) frag.findViewById(R.id.progressBar);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    Log.e("333MAIN_AUTH"," НАЖАЛ и вызвал startPhoneNumberVerification");
                    // начало подтверждения телефонного номера
                    ((MainActivity)getActivity()).startPhoneNumberVerification(phone); // есть
                }

//               // использовал для отладки
//                FragmentEnterSMS fragmentEnterSMS = new FragmentEnterSMS();
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.ll_frag_enter_sms, fragmentEnterSMS);
//                fragmentTransaction.commit();

            }
        });

        return frag;
    }
}