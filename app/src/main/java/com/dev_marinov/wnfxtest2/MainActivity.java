package com.dev_marinov.wnfxtest2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public FirebaseAuth firebaseAuth; // есть
    public String mVerificationId; // есть
    ProgressDialog progressDialog; // есть
    // если ошибка, то будет отправлено ОТР
    PhoneAuthProvider.ForceResendingToken forceResendingToken; // переменная для сохраннения токена кода
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks; // есть
    private static final int PERMISSIONS_REQUEST = 100;
    String phoneForResendVerificationCode;
    //int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // инициализация firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance(); // есть
        progressDialog = new ProgressDialog(this); // есть
        progressDialog.setTitle("пожалуйста подождите"); // есть
        progressDialog.setCanceledOnTouchOutside(false); // есть

        Window window = getWindow();
        // установка градиента анимации на toolbar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Drawable background = getResources().getDrawable(R.drawable.gradient_1_2_3_list);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS Флаг, указывающий, что это Окно отвечает за отрисовку фона для системных полос.
        // Если установлено, системные панели отображаются с прозрачным фоном, а соответствующие области в этом окне заполняются цветами,
        // указанными в Window#getStatusBarColor()и Window#getNavigationBarColor().
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(getResources().getColor(android.R.color.black));
        window.setBackgroundDrawable(background);

        // callbacks
        mCallbacks  = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // будет срабатывать при автоматическом получении SMS или при мгновенной проверке номера телефона.
            // Это в основном означает, что результат проверки здесь, и нам нужно просто проверить,
            // была ли она успешной, а затем продолжить работу в обычном режиме.
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.e("333MAIN_ACT"," ЗАПУСТИЛСЯ onVerificationCompleted");
                // будет срабатывать в двух вариантах
                // 1. в некоторых случаях номер телефона может быть мгновенно
                // подтвержден без необходимости отправлять или вводить проверочный код.
                // 2. автозапуск. На некоторых устройствах сервис Google Play может автоматически
                // обнаруживать приходящие проверочные SMS и выполнять проверку без каких-либо действий со стороны пользователя.

//                if(flag == 0)
//                {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setVisibility(ProgressBar.GONE);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            FragmentAuth fragmentAuth = (FragmentAuth) getSupportFragmentManager().findFragmentByTag("auth");
                            Log.e("333MAIN_ACT"," -fragmentAuth-" + fragmentAuth);
                            if (fragmentAuth != null) {

                                String phone = fragmentAuth.phone;
                                Log.e("333MAIN_ACT"," -есть тут phone?-" + phone);
                                //startPhoneNumberVerification(phone);
                                //signInWithPhoneAuthCredential(phoneAuthCredential); // есть

                                // переход в след fragmentEnterSMS ввод кода
                                FragmentEnterSMS fragmentEnterSMS = new FragmentEnterSMS();
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.ll_frag_enter_sms, fragmentEnterSMS, "enter_sms");
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                                Log.e("333MAIN_ACT"," -ДОЛЖЕН БЫТЬ ПЕРЕХОД НА ФРАГМЕНТ СМС-");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                    }
                                });
                            }
                        }
                    },1400);
               // }

               // flag = 1;
            }

            // ошибка верификации
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("333MAIN_ACT"," ЗАПУСТИЛСЯ onVerificationFailed");
                // если сделан недействительный запрос на проверку, например, формат телефонного номера не действителен
                Log.e("333MAIN_ACT","-e-" + e);
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            // СРАБАТЫВАЕТ ПРИ ЗАПУСКЕ CALLBACK отправка кода
            // срабатывает, когда SMS отправляется на телефон пользователя, оно будет включать идентификатор
            // подтверждения и токен повторной отправки, который необходимо сохранить. Итак, на этом этапе
            // мы должны сохранить их обоих в глобальных переменных, которые мы создали ранее.
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.e("333MAIN_ACT"," ЗАПУСТИЛСЯ onCodeSent");
                Log.d("333MAIN_ACT","-onCodeSend-" + verificationId + " token " + token.toString());
                super.onCodeSent(verificationId, token);
                // код подтверждения sms был отправлен на предоставленный номер телефона, теперь нам нужно попросить
                // пользователя ввести код, а затем сжать учетные данные, комбинируя код с идентификатором подтверждения
                mVerificationId = verificationId;
                forceResendingToken = token;
                progressDialog.dismiss();
                Log.d("333MAIN_ACT", "-onCodeSend token " + token.toString());
                Toast.makeText(getBaseContext(), "Код подтверждения отправлен", Toast.LENGTH_SHORT).show();
                // скрыть телефон
                //edt_phone.setText("");
            }
        };

    }

    // СРАБОТАЕТ ИЗ FRAGMENT_AUTH начало отправки номера телефона для подтверждения
    public void startPhoneNumberVerification(String phone) {
        Log.e("333MAIN_ACT"," ЗАПУСТИЛСЯ startPhoneNumberVerification");
        Log.e("333MAIN_ACT"," -phone-" + phone);
// запись номера телефона в новую переменную для кнопки повторной отправки кода во frag_sms
        phoneForResendVerificationCode = phone;

        progressDialog.setMessage("верификация номера телефона");
        progressDialog.show();
        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    // СРАБАТЫВАЕТ ИЗ FRAGMENT_SMS ДЛЯ ПЕРЕХОДА В FRAGMENT_SUCCESS верификация телефонного номера и кода
    // После того, как пользователь ввел проверочный код, код и проверочный идентификатор, предоставленные
    // для обратного вызова onCodeSent (), передаются методу getCredential () класса PhoneAuthProvider.
    // Это возвращает объект PhoneAuthCredential, который можно использовать для входа пользователя в
    // приложение обычным способом посредством вызова метода signInWithCredential () класса FirebaseAuth.
    public void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        Log.e("333FRAG_SMS"," ЗАПУСТИЛСЯ verifyPhoneNumberWithCode");
        progressDialog.setMessage("верификация кода");
        progressDialog.show();

        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, code);
        Log.e("333FRAG_SMS"," verifyPhoneNumberWithCode ЗАПУСТИЛ signInWithPhoneAuthCredential");
        signInWithPhoneAuthCredential(phoneAuthCredential);
    }

    // СРАБОТАЕТ ИЗ МЕТОДА verifyPhoneNumberWithCode вход с по учетным данным телефон и код
    public void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        Log.e("333FRAG_SMS"," ЗАПУСТИЛСЯ signInWithPhoneAuthCredential");
        progressDialog.setMessage("вход с помощью учетных данных");

//            // .addOnSuccessListener(null) удалить слушателя елси он есть при старом входе
//        ((MainActivity)getActivity()).firebaseAuth.signInWithCredential(phoneAuthCredential)
//                   .addOnSuccessListener(null).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

        // .addOnSuccessListener(null) удалить слушателя елси он есть при старом входе
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {        // успешный вход в систему
                        progressDialog.dismiss();
                        String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                        Toast.makeText(getBaseContext(), "успешный вход в систему" + phone, Toast.LENGTH_SHORT).show();

                        FragmentSuccess fragmentSuccess = new FragmentSuccess();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.ll_frag_success, fragmentSuccess,"success");
                        //fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {        // ошибка входа в систему
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Logged in as ОШИБКА " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
             });
        }

    // повторная отправка кода подтверждения
    public void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        Log.e("333FRAG_SMS"," ЗАПУСТИЛСЯ resendVerificationCode");
        Log.e("333FRAG_SMS","-resendingToken-" + forceResendingToken);

        progressDialog.setMessage("повторная отправка кода");
        progressDialog.show();
        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                // phoneForResendVerificationCode - сохраненный ранее номер, до перехода во frag_sms
                .setPhoneNumber(phoneForResendVerificationCode)

    // Параметр 60тайм-аута - это максимальное время, в течение которого вы готовы ждать
    // завершения автозапуска SMS-сообщений библиотекой.
    // Затем, Unitв котором выражен наш тайм-аут (в данном случае, секунды).
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .setForceResendingToken(forceResendingToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }
}