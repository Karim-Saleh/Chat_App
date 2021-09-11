package summer.project.whatsappFinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import summer.project.whatsappFinal.Database.Database;
import summer.project.whatsappFinal.Listener.OnRecyclerViewItemClickListener;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity
{
    private RecyclerView recyclerView ;
    private ArrayList<Contact> existContacts;
    private ContactAdapter adapter;
    private Database database;
    public final static int CONTACTS_ACTIVITY = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Contacts");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //Initialization
        database = new Database(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //RecyclerView
        existContacts = database.getContacts();
        recyclerView = findViewById(R.id.recyclerView_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager( (this)));
        adapter = new ContactAdapter(existContacts);
        recyclerView.setAdapter(adapter);
        //////////////

        /////////////////
        adapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                String contactName = existContacts.get(position).getName(),
                        contactNumber = existContacts.get(position).getNumber();
                intent.putExtra("contactName", contactName);
                intent.putExtra("contactNumber", contactNumber);
                intent.putExtra("class", CONTACTS_ACTIVITY);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}