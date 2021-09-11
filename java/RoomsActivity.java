package summer.project.whatsappFinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.Manifest;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import summer.project.whatsappFinal.Database.Database;
import summer.project.whatsappFinal.Listener.OnContactLoadFinishListener;
import summer.project.whatsappFinal.Listener.OnMessageStateChangeListener;
import summer.project.whatsappFinal.Listener.OnRecyclerViewItemClickListener;
import summer.project.whatsappFinal.Listener.OnMessageReceivedListener;
import summer.project.whatsappFinal.Service.ReceivingMessages;
import summer.project.whatsappFinal.databinding.RoomsBinding;

public class RoomsActivity extends AppCompatActivity
{
    private RoomsBinding roomsBinding;
    private RecyclerView roomsList;
    private RoomsAdapter roomsAdapter;
    private ArrayList<Room> rooms;
    private Database database = new Database(this);
    public static final int ROOMS_ACTIVITY = 1002;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        roomsBinding = RoomsBinding.inflate(getLayoutInflater());

        //Related to design
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getSupportActionBar().setTitle("Chats");
        setContentView(roomsBinding.getRoot());
        ///////////////////

        //Related to RecyclerView
        rooms = database.getChatRooms();
        roomsAdapter = new RoomsAdapter(this, rooms, database.getUsrPhoneNumber());
        roomsList = findViewById(R.id.recyclerView_rooms);
        roomsList.setLayoutManager(new LinearLayoutManager(this));
        roomsList.setAdapter(roomsAdapter);
        ((SimpleItemAnimator) roomsBinding.recyclerViewRooms.getItemAnimator()).setSupportsChangeAnimations(false);
        ////////////////

        //Events
        roomsAdapter.setOnItemClickListener(RecyclerViewItemClickListener);
        roomsBinding.fabContacts.setOnClickListener(fabContactsOnClickListener);
        App.addOnLoadContactFinishListener(contactLoadFinishListener);
        ////////

        //Related to service
        Intent serviceIntent = new Intent(getBaseContext(), ReceivingMessages.class);
        serviceIntent.putExtra("contactNumber", "");
        ContextCompat.startForegroundService(getBaseContext() ,serviceIntent);
        ////////////////////


        App.sendUnsentMessages(database, roomsAdapter, rooms); //Should be in the service class
        checkPermission(); //should be in the Application class
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        ReceivingMessages.addOnMessageReceivedListener(messageReceivedListener);
        ReceivingMessages.addOnMessageStateChangeListener(messageStateListener);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        rooms.clear();
        rooms.addAll(database.getChatRooms());
        roomsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100 && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            App.getContactList(this, database);

        }
        else {
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_CONTACTS},100);

        }
        else {
            App.getContactList(this, database);
        }
    }

    private int findRoom(String contactNumber)
    {
        for(int i = 0; i < rooms.size(); i++)
        {
            if(contactNumber.equals(rooms.get(i).getContactNumber()))
            {
                return i;
            }
        }
        return -1;
    }

    private void changeLastMessageState(String id, int state)
    {
        for(int i = 0; i < rooms.size(); i++)
        {
            final int position = i;
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    if(rooms.get(position).getLastMessage().getId().equals(id))
                    {
                        if(state == OnMessageStateChangeListener.delivered)
                        {
                            rooms.get(position).getLastMessage().setDelivered(true);
                        }
                        else if (state == OnMessageStateChangeListener.seen)
                        {
                            rooms.get(position).getLastMessage().setSeen(true);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                roomsAdapter.notifyItemChanged(position);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public void changeRoom(Message newMessage)
    {
        int position = findRoom(newMessage.getRoomNumber());
        Room room = rooms.get(position);
        room.setLastMessage(newMessage);
        roomsAdapter.notifyItemChanged(position);
        if (position != 0)
        {
            rooms.remove(position);
            rooms.add(0, room);
            roomsAdapter.notifyItemMoved(position, 0);
        }
    }

    //Events
    private View.OnClickListener fabContactsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(RoomsActivity.this, ContactActivity.class);
            startActivity(intent);
        }
    };

    private OnRecyclerViewItemClickListener RecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Intent intent = new Intent(RoomsActivity.this, ChatActivity.class);
            intent.putExtra("contactNumber", rooms.get(position).getContactNumber());
            intent.putExtra("contactName", rooms.get(position).getContactName());
            intent.putExtra("class", ROOMS_ACTIVITY);
            startActivity(intent);
        }
    };

    private OnContactLoadFinishListener contactLoadFinishListener = new
            OnContactLoadFinishListener() {
                @Override
                public void onLoadFinish()
                {
                    rooms.clear();
                    rooms.addAll(database.getChatRooms());
                    roomsAdapter.notifyDataSetChanged();
                }
            };

    private OnMessageReceivedListener messageReceivedListener = new OnMessageReceivedListener() {
        @Override
        public void roomChanged(Message newMessage) {
            super.roomChanged(newMessage);
            changeRoom(newMessage);
        }

        @Override
        public void RoomAdded(Room room) {
            super.RoomAdded(room);
            rooms.add(0, room);
            roomsAdapter.notifyItemInserted(0);
        }
    };

    private OnMessageStateChangeListener messageStateListener = new
            OnMessageStateChangeListener() {
                @Override
                public void messageStateChange(String messageId, int state)
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            changeLastMessageState(messageId, state);
                        }
                    }).start();
                }
            };
    ////////
}
