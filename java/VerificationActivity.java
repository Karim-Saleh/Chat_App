package summer.project.whatsappFinal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import summer.project.whatsappFinal.Database.Database;


public class VerificationActivity extends AppCompatActivity
{
    private EditText editText_verificationCoed;
    Button button_resend;
    private static int BUTTON_RESEND_DISABLED;
    private Database database = new Database(this);
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        String phoneNumber = getIntent().getExtras().getString("phoneNumber");
        editText_verificationCoed = (EditText) findViewById(R.id.editText_verificationCode);
        button_resend = (Button) findViewById(R.id.button_resend);
        BUTTON_RESEND_DISABLED =  getResources().getColor(R.color.button_resend_disabled);
        App.phoneAuthentication(this, this, phoneNumber, database);
        findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = editText_verificationCoed.getText().toString().trim();
                if(code.isEmpty() || code.length() < 6)
                {
                    editText_verificationCoed.setError("Code not valid");
                }
                else
                {
                    App.manualVerify(code, VerificationActivity.this, database);
                }
            }
        });

        button_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                App.resendVerificationCode(phoneNumber, VerificationActivity.this);
                button_resend.setTextColor(BUTTON_RESEND_DISABLED);
                button_resend.setEnabled(false);
            }
        });
    }
}