package com.egor_dubovik.testapplication.ui;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.egor_dubovik.testapplication.R;
import com.egor_dubovik.testapplication.api.model.Data;
import com.egor_dubovik.testapplication.api.model.User;
import com.egor_dubovik.testapplication.api.model.Valid;
import com.egor_dubovik.testapplication.api.service.UserClient;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Registration_Activity extends AppCompatActivity {

    //Наиболее частовстречающиеся адреса электронной почты
    private final String[] popularDomains = {"aol.com", "att.net", "comcast.net", "facebook.com",
            "gmail.com", "gmx.com", "googlemail.com", "google.com", "hotmail.com", "mac.com",
            "me.com", "mail.com", "msn.com", "live.com",
            "sbcglobal.net", "verizon.net", "yahoo.com", "email.com", "games.com",
            "gmx.net", "hush.com", "hushmail.com", "inbox.com", "lavabit.com", "love.com",
            "pobox.com", "rocketmail.com", "wow.com", "ygm.com", "ymail.com",
            "zoho.com", "fastmail.fm", "mail.ru", "rambler.ru", "yandex.ru", "balinasoft.com"};
    private final String TAG = "Registration_Activity";
    //Поле ввода email
    private AppCompatMultiAutoCompleteTextView emailInput;
    //Поле ввода пароля
    private EditText passwordInput;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        passwordInput = findViewById(R.id.password_input_view);

        //Настройка слушателя определения нажатия на картинку для скрытия/показа пароля
        passwordInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    passwordInput.requestFocus();

                    if (event.getRawX() >= (passwordInput.getRight() - passwordInput.
                            getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        if (passwordInput.getTransformationMethod() != null) {
                            passwordInput.setTransformationMethod(null);
                            passwordInput.setSelection(passwordInput.getText().length());
                        } else {
                            passwordInput.
                                    setTransformationMethod(new PasswordTransformationMethod());
                            passwordInput.setSelection(passwordInput.getText().length());
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        emailInput = findViewById(R.id.email_input_view);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.simple_spinner, popularDomains);

        //Слушатель изменения состояния строки email, для предложения пользователю наиболее
        //популярных вариантов
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailInput.getText().toString().contains("@") &&
                        atCount(emailInput.getText().toString()) == 1) {
                    emailInput.setAdapter(adapter);
                    emailInput.setTokenizer(new EmailTokenizer());
                } else {
                    emailInput.setAdapter(null);
                    emailInput.dismissDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * Счетчик символа "@"
     *
     * @param s Строка с символом "@"
     * @return Кол-во символа "@" в строке
     */
    private int atCount(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '@') {
                count++;
            }
        }
        return count;
    }

    /**
     * Проверка email на ошибки, если все хорошо - возвращает пустое значение, если есть какая-то
     * ошибка - возвращает тип ошибки
     *
     * @param email Строка с email, который будет проверятся на валидность
     * @return Если все хорошо - возвращает пустое значение, если есть какая-то
     * ошибка - возвращает тип ошибки
     */
    private String emailCheck(String email) {

        Pattern pattern;
        Matcher matcher;

        String typeOfError = "";

        if (!email.equals("")) {
            final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            pattern = Pattern.compile(EMAIL_PATTERN);
            matcher = pattern.matcher(email);

            if (!matcher.matches()) {
                typeOfError = getResources().getString(R.string.error_of_email_syntax);
            }

        } else typeOfError = getResources().getString(R.string.error_of_email_emptiness);

        return typeOfError;
    }

    /**
     * Принимает строку email и возвращает ее домен. Например, "example@example.com" возврашает
     * значение "example.com"
     *
     * @param s Email в виде строки
     * @return Сайт в виде строки
     */

    private String domainParser(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) != '@') {
                stringBuilder.append(s.charAt(i));
            } else {
                break;
            }
        }
        return stringBuilder.reverse().toString();
    }

    /**
     * Проверка пароля на ошибки, если все хорошо - возвращает пустое значение, если есть какая-то
     * ошибка - возвращает тип ошибки
     *
     * @param password Строка с паролем, который будет проверятся на валидность
     * @return Если все хорошо - возвращает пустое значение, если есть какая-то
     * ошибка - возвращает тип ошибки
     */
    private String passwordCheck(String password) {

        String typeOfError = "";

        if (password.length() < 8) {
            typeOfError = getResources().getString(R.string.error_of_password_minimum_length);
        }

        if (password.length() > 500) {
            typeOfError = getResources().getString(R.string.error_of_password_maximum_length);
        }

        if (password.length() == 0) {
            typeOfError = getResources().getString(R.string.error_of_password_emptiness);
        }

        return typeOfError;

    }

    /**
     * Подключение к серверу, либо вывод ошибки в поле логина/пароля.
     * Активируется при нажатии на кнопку регистрации
     */
    public void onRegistrationButtonClick(View view) {

        String passwordInformation = passwordCheck(passwordInput.getText().toString());

        String emailInformation = emailCheck(emailInput.getText().toString());

        if (emailInformation.equals("") && passwordInformation.equals("")) {
            new EmailExistenceChecker().execute(domainParser(emailInput.getText().toString()));
        } else {
            String error = errorBuilder(emailInformation, passwordInformation);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    //Используется для более удобного построения сообщения об ошибке
    private String errorBuilder(String a, String b) {

        String error = "";

        if (a.equals("") && !b.equals("")) {
            error = b;
        }

        if (!a.equals("") && b.equals("")) {
            error = a;
        }

        if (!a.equals("") && !b.equals(""))
            error = a + '\n' + b;

        return error;
    }

    /**
     * Отправка запроса на регистрацию, получение подтверждения об ее успешности, либо вывод
     * сообщения об возникшей ошибке
     *
     * @param user объект класса User, который хранит значения логина и пароля
     */

    private void sendNetworkRequest(User user) {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://junior.balinasoft.com/api/account/signup/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        UserClient userClient = retrofit.create(UserClient.class);
        Call<User> call = userClient.signUpUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                //Если запрос был выполнен успешно
                if (response.isSuccessful()) {

                    Data data = response.body().getData();

                    String token = data.getToken();
                    String id = String.valueOf(data.getUserId());
                    String login = data.getLogin();
                    String status = String.valueOf(response.body().getStatus());

                    //Вывод сообщения об успешной регистрации и полученных данных
                    String message = getResources().getString(R.string.successful_registration) +
                            "\nstatus: " + status +
                            "\nuserId: " + id + "\nlogin: " + login + "\ntoken: " + token;

                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }

                //Если запрос был выполнен неуспешно
                if (!response.isSuccessful()) {

                    Gson gson = new Gson();

                    TypeAdapter<User> adapter = gson.getAdapter(User.class);

                    User user = new User(null, null);

                    try {
                        if (response.errorBody() != null) {
                            String string = response.errorBody().string();
                            user = adapter.fromJson(string);
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "Response wasn't successful exception", e);
                    }

                    String status = String.valueOf(user.getStatus());

                    String error = user.getError();

                    if (error.equals("validation-error")) {
                        Valid valid = user.getValid(0);

                        String field = valid.getField();
                        String fieldMessage = valid.getMessage();

                        if (field.equals("login") &&
                                fieldMessage.equals("size must be between 4 and 32")) {
                            String message = "Status: " + status + "\n" +
                                    getResources().getString(R.string.login_size_error) +
                                    "\n" + getResources().getString(R.string.login_description);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    if (error.equals("security.signup.login-already-use")) {
                        String message = "Status: " + status + "\n" +
                                getResources().getString(R.string.login_already_use) +
                                "\n" + getResources().getString(R.string.login_description);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                String message = getResources().getString(R.string.unexpected_server_error);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    //Получение логина из email
    private String getLoginFromEmail() {

        String email = emailInput.getText().toString();

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < email.length() - 1; i++) {
            if (email.charAt(i) != '@') {
                stringBuilder.append(email.charAt(i));
            } else {
                break;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * AsyncTask, для проверки email на существование
     */
    @SuppressLint("StaticFieldLeak")
    private class EmailExistenceChecker extends AsyncTask<String, Void, Boolean> {

        private String domain;

        @Override
        protected Boolean doInBackground(String... path) {
            domain = path[0];
            return isHostReachable(pathBuilder(path[0]));
        }

        private String pathBuilder(String path) {
            return "http://" + path;
        }

        //Если такой Email существует, то начинается выполнение метода sendNetworkRequest, иначе
        //выводится сообщение об несуществовании домена
        @Override
        protected void onPostExecute(Boolean result) {
            String error;

            if (!result) {
                error = getResources().getString(R.string.error_of_email_domain) + ' ' + '"' +
                        domain + '"';
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            }

            if (result) {
                User user = new User(
                        getLoginFromEmail(),
                        passwordInput.getText().toString()
                );
                sendNetworkRequest(user);
            }
        }

        /**
         * Проверка сайта на существование. Производится подключение к серверу, если сервер
         * возвращает такие коды ответа, как 200, 301, 302 -> он существует.
         *
         * @param address Адрес сайта, который должен быть проверен на существование
         * @return true(существует), false(не существует)
         */
        private boolean isHostReachable(String address) {

            try {

                URL url = new URL(address);

                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "userAgent");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000 * 7);
                urlc.connect();
                if (urlc.getResponseCode() == 200 || urlc.getResponseCode() == 301 ||
                        urlc.getResponseCode() == 302) {
                    urlc.disconnect();
                    return true;
                }
            } catch (MalformedURLException e) {
                Log.d(TAG, "isHostReachable method exception", e);
            } catch (IOException e) {
                Log.d(TAG, "isHostReachable method exception", e);
            }
            return false;
        }

    }
}
