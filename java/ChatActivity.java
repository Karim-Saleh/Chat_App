package summer.project.whatsappFinal;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;

import summer.project.whatsappFinal.Database.Database;
import summer.project.whatsappFinal.Listener.OnMessageReceivedListener;
import summer.project.whatsappFinal.Listener.OnMessageStateChangeListener;
import summer.project.whatsappFinal.Service.ReceivingMessages;
import summer.project.whatsappFinal.databinding.ChatBinding;

public class ChatActivity extends AppCompatActivity
{
    private ChatBinding chatBinding;
    private String contactNumber, contactName;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    private final ArrayList<Message> messages = new ArrayList<Message>();
    private final Database database = new Database(this);
    private Intent serviceIntent;
    private int previousClass;
    private LinearSmoothScroller linearSmoothScroller;
    private static final float MILLISECONDS_PER_INCH = 0.00001f;
    private Cursor messagesCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatBinding = ChatBinding.inflate(getLayoutInflater());


        //Related to Intent
        Intent intent = getIntent();
        contactNumber = intent.getExtras().getString("contactNumber");
        contactName = intent.getExtras().getString("contactName");
        previousClass = intent.getExtras().getInt("class");
        /////////////////////

        //Related to Design
        getSupportActionBar().setTitle(contactName.isEmpty() ? contactNumber : contactName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(chatBinding.getRoot());
        ////////////////////

        //Related to database
        messagesCursor = database.getMessagesCursor(contactNumber);
        //messages.addAll(database.getFirstMessages(messagesCursor));
        database.updateUnseenContactMessages(contactNumber); //For displaying notifications
        ///////////////////

        //Related to RecyclerView layout manager
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        linearSmoothScroller = new LinearSmoothScroller(chatBinding.messagesList.getContext())
        {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics)
            {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
        ////////////////////////////////////////

        //Related to RecyclerView
        ((SimpleItemAnimator) chatBinding.messagesList.getItemAnimator()).setSupportsChangeAnimations(false);
        chatAdapter = new ChatAdapter(messages,database.getUsrPhoneNumber(), this);
        chatBinding.messagesList.setAdapter(chatAdapter);
        chatBinding.messagesList.setLayoutManager(layoutManager);
        //////////////

        new RetrievingMessages().execute(database.getMessagesCount(contactNumber)); //Displaying the rest of the chat

        //Events
        chatBinding.buttonSend.setOnClickListener(buttonSendListener); //Clicking send button
        ReceivingMessages.addOnMessageReceivedListener(messageReceivedListener); //Receiving message
        ReceivingMessages.addOnMessageStateChangeListener(messageStateChangeListener); //Message state change
        getOnBackPressedDispatcher().addCallback(this, callback); //Clicking back button in the navigation bar
        KeyboardUtils.addKeyboardToggleListener(this, keyboardToggleListener); //Opening Keyboard
        /////////
    }

    @Override
    protected void onStart() {
        super.onStart();

        //removing the notifications of this contact as the user is already in its chat
        ReceivingMessages.removeNotification(contactNumber);

        //Starting the service again with the contact
        //number to avoid the notifications of this contact
        serviceIntent = new Intent(this, ReceivingMessages.class);
        serviceIntent.putExtra("contactNumber", contactNumber);
        ContextCompat.startForegroundService(getBaseContext(), serviceIntent);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        //Setting the contact number in the service class to empty when the chat
        //activity pauses to make the notifications of this contact appear again
        serviceIntent = new Intent(this, ReceivingMessages.class);
        serviceIntent.putExtra("contactNumber", "");
        ContextCompat.startForegroundService(getBaseContext(), serviceIntent);

        //If the user came from the contact activity and it's the first
        //messaging to this contact then we need to add it in the database
        if(previousClass == ContactActivity.CONTACTS_ACTIVITY && messages.size() > 0)
        {
            database.addChatRoom(contactNumber, contactName);
        }
    }

    //Events
    private final View.OnClickListener buttonSendListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            String msg = chatBinding.editTextMessage.getText().toString().trim();
            if(!msg.isEmpty())
            {
                chatBinding.editTextMessage.setText("");
                new Thread(() -> {
                    Message message = App.sendMessage(contactNumber, msg,
                            ChatActivity.this, database, chatAdapter, messages);
                    messages.add(0, message);
                    runOnUiThread(() -> {
                        chatAdapter.notifyItemInserted(0);
                        //Scroll down
                        linearSmoothScroller.setTargetPosition(0);
                        layoutManager.startSmoothScroll(linearSmoothScroller);
                    });
                }).start();
            }
        }
    };

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true)
    {
        @Override
        public void handleOnBackPressed()
        {
            backFromChat();
        }
    };

    @Override
    public boolean onSupportNavigateUp()
    {
        backFromChat();
        return true;
    }

    private final KeyboardUtils.SoftKeyboardToggleListener keyboardToggleListener = new
            KeyboardUtils.SoftKeyboardToggleListener()
            {
                @Override
                public void onToggleSoftKeyboard(boolean isVisible)
                {
                    linearSmoothScroller.setTargetPosition(0);
                    layoutManager.startSmoothScroll(linearSmoothScroller);
                }
            };

    private final OnMessageReceivedListener messageReceivedListener = new OnMessageReceivedListener()
    {
        @Override
        public void MessageSaved(Message message)
        {
            super.MessageSaved(message);
            messages.add(0, message);
            chatAdapter.notifyItemInserted(0);
            linearSmoothScroller.setTargetPosition(0);
            layoutManager.startSmoothScroll(linearSmoothScroller);
        }
    };

    private final OnMessageStateChangeListener messageStateChangeListener =
            new OnMessageStateChangeListener() {
                @Override
                public void messageStateChange(String messageId, int state) {
                    int position = App.findMessage(messages, messageId);
                    if(position >= 0 && position < messages.size()) {
                        if (state == delivered) {
                            messages.get(position).setDelivered(true);
                        } else if (state == seen) {
                            messages.get(position).setSeen(true);
                        }
                        chatAdapter.notifyItemChanged(position);
                    }
                }
            };
    ////////

    private void backFromChat()
    {
        if(previousClass == ReceivingMessages.RECEIVING_MESSAGES)
        {
            Intent intent = new Intent(ChatActivity.this, RoomsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else
            finish();
    }

    class RetrievingMessages extends AsyncTask<Integer, Integer, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }

        @Override
        protected void onProgressUpdate(Integer... integers) {
            super.onProgressUpdate(integers);
            messages.add(database.getOneMessageAtATime(messagesCursor));
            chatAdapter.notifyItemInserted(integers[0]-1);
        }

        @Override
        protected Void doInBackground(Integer... integers)
        {
            for(int i = 1; i <= integers[0]; i++)
            {
                if(i == 15)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                publishProgress(i);
            }
            return null;
        }
    }
}