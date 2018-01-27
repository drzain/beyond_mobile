package absen.youngdev.com.beyonddev;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            Logout();
        }
        getSupportActionBar().hide();
        Button btnabsen = (Button) findViewById(R.id.btnPayment);
        Button btntask = (Button) findViewById(R.id.btnInvest);
        Button btnlogout = (Button) findViewById(R.id.btnShop);
        Button btnprofile = (Button) findViewById(R.id.btnProfile);

        btnabsen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }});

        btntask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Under Construction",Toast.LENGTH_SHORT).show();
            }});
        btnprofile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Under Construction",Toast.LENGTH_SHORT).show();
            }});
        btnlogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Logout();
            }});
    }

    public void Logout(){
        session.setLogin(false);
        Intent intent = new Intent(HomeActivity.this, NewLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
