package com.egor_dubovik.testapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration_Activity extends AppCompatActivity {

    //наиболее частовстречающиеся домены
    private final String[] domains = {"com", "net", "uk", "fm", "ru", "by", "ua", "org", "run"};

    //Наиболее частовстречающиеся адреса электронной почты
    private final String[] popularDomains = {"aol.com", "att.net", "comcast.net", "facebook.com",
            "gmail.com", "gmx.com", "googlemail.com", "google.com", "hotmail.com", "mac.com",
            "me.com", "mail.com", "msn.com", "live.com",
            "sbcglobal.net", "verizon.net", "yahoo.com", "email.com", "games.com",
            "gmx.net", "hush.com", "hushmail.com", "inbox.com", "lavabit.com", "love.com",
            "pobox.com", "rocketmail.com", "wow.com", "ygm.com", "ymail.com",
            "zoho.com", "fastmail.fm", "mail.ru", "rambler.ru", "yandex.ru", "balinasoft.com"};

    //Поле ввода email
    private AppCompatMultiAutoCompleteTextView emailInput;

    //Поле ввода пароля
    private EditText passwordInput;

    private ScrollView scrollView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        passwordInput = findViewById(R.id.password_input_view);

        scrollView = findViewById(R.id.scroll_view);

        //TODO Прокрутка вниз при нажатии на поле с паролем
        //TODO Прокрутка вниз при нажатии на поле с email

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
                            passwordInput.setTransformationMethod(new PasswordTransformationMethod());
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
                    emailInput.showDropDown();
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
     * @return Кол-во символа "@"
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

        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);

        if (matcher.matches()) {
            Boolean flag = false;
            String domain = domainParser(email);
            for (String domain1 : domains) {
                if (domain.equals(domain1)) {
                    flag = true;
                }
            }
            if (!flag) {
                typeOfError = getResources().getString(R.string.error_of_email_domain) + ' ' + '"' + domain + '"';
            }

        } else {
            if (!email.equals("")) {
                typeOfError = getResources().getString(R.string.error_of_email_syntax);
            } else {
                typeOfError = getResources().getString(R.string.error_of_email_emptiness);
            }
        }
        return typeOfError;
    }

    /**
     * Принимает строку email и возвращает ее домен. Например, "example@example.com" возврашает
     * значение "com"
     *
     * @param s Email в виде строки
     * @return Домен в виде строки
     */

    private String domainParser(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) != '.') {
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

        if (password.length() < 5) {
            typeOfError = getResources().getString(R.string.error_of_password_minimum_length);
        }

        if (password.length() > 10) {
            typeOfError = getResources().getString(R.string.error_of_password_maximum_length);
        }

        if (password.length() == 0) {
            typeOfError = getResources().getString(R.string.error_of_password_emptiness);
        }

        return typeOfError;

    }

    /**
     * Вывод информации об успешной регистации, либо о виде ошибки в поле логина/пароля.
     * Активируется при нажатии на кнопку регистрации
     */
    public void onRegistrationButtonClick(View view) {

        String passwordInformation = passwordCheck(passwordInput.getText().toString());

        String emailInformation = emailCheck(emailInput.getText().toString());

        if (emailInformation.equals("") && passwordInformation.equals("")) {
            Snackbar.make(view, getResources().getString(R.string.successful_registration),
                    Snackbar.LENGTH_LONG).show();
        } else {
            String error = emailInformation + '\n' + passwordInformation;
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }

    }
}
