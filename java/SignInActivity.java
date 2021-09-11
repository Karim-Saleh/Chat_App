package summer.project.whatsappFinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private Spinner countrySpinner;
    private EditText phoneEditText;
    private TextView countryCode;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //Countries spinner
        countrySpinner = (Spinner)findViewById(R.id.spinner_countrySelector);
        countrySpinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, Countries.countryNames));
        countrySpinner.setSelection(51);
        //////////////////

        //phone
        phoneEditText = (EditText)findViewById(R.id.editText_phoneNumber);
        ///////

        //// countryCode
        countryCode = (TextView)findViewById(R.id.textView_countryCode);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String code = "+"+Countries.countryAreaCodes[position];
                countryCode.setText(code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //////

        //Confirm button
        findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countryCode = Countries.countryAreaCodes[
                        countrySpinner.getSelectedItemPosition()];
                String phoneNumber = phoneEditText.getText().toString();
                if(phoneNumber.isEmpty())
                {
                    phoneEditText.setError("Number is required!");
                    phoneEditText.requestFocus();
                    return;
                }
                phoneNumber = phoneNumber.replaceAll("\\s", "");
                if(countryCode.equals("20") && !App.isRightEgyptNumber(phoneNumber))
                {
                    phoneEditText.setError("Not valid number");
                    phoneEditText.requestFocus();
                    return;
                }
                Intent intent = new Intent(SignInActivity.this, VerificationActivity.class);
                intent.putExtra("phoneNumber", "+" + countryCode +phoneNumber);
                startActivity(intent);
            }
        });
        ////////////////

    }

    @Override
    protected void onStart() {
        super.onStart();

        //If the user already logged in before
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            Intent intent = new Intent(SignInActivity.this, RoomsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}