package pivot.security.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import pivot.security.android.Preferences;
import pivot.security.android.R;
import pivot.security.android.dialogfragment.LoadingDialogFragment;
import pivot.security.android.network.VPNService;
import pivot.security.android.network.responses.ServersResponse;
import pivot.security.android.utils.PrefUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends BaseActivity implements Validator.ValidationListener {

    public static final String PROCEED_TO_MAIN = "return", STATE_USERNAME = "state_username", STATE_PASSWORD = "state_password";

    private Validator mValidator;
    private boolean mDoNotSave = false;

    @NotEmpty
    @Bind(R.id.usernameEdit)
    EditText mUsername;
    @NotEmpty
    @Bind(R.id.passwordEdit)
    EditText mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_login);
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        if(savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsername.setText(PrefUtils.get(this, STATE_USERNAME, ""));
        mPassword.setText(PrefUtils.get(this, STATE_PASSWORD, ""));
        PrefUtils.remove(this, STATE_USERNAME);
        PrefUtils.remove(this, STATE_PASSWORD);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!mDoNotSave) {
            PrefUtils.save(this, STATE_USERNAME, mUsername.getText().toString());
            PrefUtils.save(this, STATE_PASSWORD, mPassword.getText().toString());
        }
    }

    @OnClick(R.id.loginButton)
    public void loginClick(View v) {
        mValidator.validate(true);
    }

    @OnClick(R.id.registerButton)
    public void registerClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.pivotsecurity.com/"));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        try {
            if (!getIntent().getBooleanExtra(PROCEED_TO_MAIN, true)) {
                setResult(RESULT_CANCELED);
            }
            super.onBackPressed();
        } catch (IllegalStateException e) {
            // catch activity close
        }
    }

    @Override
    public void onValidationSucceeded() {
        LoadingDialogFragment.show(getSupportFragmentManager());
        String result = VPNService.login(mUsername.getText().toString(), mPassword.getText().toString());
        
        if (result.equalsIgnoreCase("\"OK\"")) {
                PrefUtils.save(LoginActivity.this, Preferences.USERNAME, mUsername.getText().toString());
                PrefUtils.save(LoginActivity.this, Preferences.PASSWORD, mPassword.getText().toString());

                mDoNotSave = true;
                if (getIntent().getBooleanExtra(PROCEED_TO_MAIN, true)) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
        }else{
            Toast.makeText(LoginActivity.this, R.string.credentials_incorrect, Toast.LENGTH_SHORT).show();
        }
        
		LoadingDialogFragment.dismiss(getSupportFragmentManager());
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_USERNAME, mUsername.getText().toString());
        outState.putString(STATE_PASSWORD, mPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUsername.setText(savedInstanceState.getString(STATE_USERNAME, ""));
        mPassword.setText(savedInstanceState.getString(STATE_PASSWORD, ""));
    }
}
