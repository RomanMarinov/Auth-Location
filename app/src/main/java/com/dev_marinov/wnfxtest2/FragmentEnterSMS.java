package com.dev_marinov.wnfxtest2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentEnterSMS extends Fragment {

    View frag;
    EditText edt_code;
    TextView tv_countDown, tv_blink_2;
    Button bt_next, bt_returnCode;
    String code;
    Animation anim;
    String phone;
    Boolean flag = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        frag = inflater.inflate(R.layout.fragment_enter_sms, container, false);

        tv_countDown = frag.findViewById(R.id.tv_countDown);
        edt_code = frag.findViewById(R.id.edt_code);
        bt_next = frag.findViewById(R.id.bt_next);
        bt_returnCode = frag.findViewById(R.id.bt_returnCode);
        tv_blink_2 = frag.findViewById(R.id.tv_blink_2);

        // анимация для фона
        ConstraintLayout constraintLayout = frag.findViewById(R.id.cl_main);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        // кнопка "продолжить" после набора кода
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("333FRAG_SMS"," НАЖАЛ ПЕРЕЙТИ ДАЛЬШЕ ПОСЛЕ ВВОДА КОДА");
                code = edt_code.getText().toString().trim();
                if(code.length() == 6)
                {
                    Log.e("333FRAG_SMS"," НАЖАЛ и запустил verifyPhoneNumberWithCode");
                    // верификация телефонного номера и кода
                    ((MainActivity)getActivity()).verifyPhoneNumberWithCode(((MainActivity)getActivity()).mVerificationId, code);
                }
                else {
                    Toast.makeText(getContext(), "пожалуйста введите код верификации", Toast.LENGTH_SHORT).show();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // анимация на неправльный ввод кода
                            tv_blink_2.setVisibility(View.VISIBLE);
                            anim = AnimationUtils.loadAnimation(getContext(), R.anim.blink);
                            tv_blink_2.startAnimation(anim);
                        }
                    });
                }

//               // использовал для отладки
//                FragmentSuccess fragmentSuccess = new FragmentSuccess();
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.ll_frag_success, fragmentSuccess);
//                fragmentTransaction.commit();
            }
        });

        // кнопка повторной отправки кода + обратный счетчик
        bt_returnCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new CountDownTimer(120000,1000)
                {
                    //Здесь обновляем текст счетчика обратного отсчета с каждой секундой
                    public void onTick(long millisUntilFinished) {
                        Log.e("333FRAG_SMS"," CountDownTimer ");

                        tv_countDown.setText("осталось: " + millisUntilFinished / 1000 + " секунд");
                        bt_returnCode.setEnabled(false);
                        bt_next.setEnabled(false);

                        if(flag == false)
                        {
                            Log.e("333FRAG_SMS"," CountDownTimer flag должен быть false" + flag);
                            // повторная отправка кода подтверждения
                            ((MainActivity)getActivity()).resendVerificationCode(phone,
                                    ((MainActivity)getActivity()).forceResendingToken);
                        }
                        flag = true;
                    }
                    //Задаем действия после завершения отсчета
                    public void onFinish() {
                        bt_returnCode.setEnabled(true);
                        tv_countDown.setText("");
                        bt_returnCode.setEnabled(true);
                        bt_next.setEnabled(true);
                        flag = false;
                    }
                }
               .start();
            }
        });

        return frag;
    }

}