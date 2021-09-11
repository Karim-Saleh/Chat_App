package summer.project.whatsappFinal.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import summer.project.whatsappFinal.App;
import summer.project.whatsappFinal.ChatActivity;
import summer.project.whatsappFinal.Database.Database;
import summer.project.whatsappFinal.Listener.OnMessageReceivedListener;
import summer.project.whatsappFinal.Listener.OnMessageStateChangeListener;
import summer.project.whatsappFinal.Message;
import summer.project.whatsappFinal.R;
import summer.project.whatsappFinal.Room;

public class ReceivingMessages extends Service
{
    private final String userPhoneNumber =
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    private final DatabaseReference reference_receiver = App.reference_toBeReceived.child(userPhoneNumber),
            reference_sender = App.reference_sentFrom.child(userPhoneNumber);
    private Database database;
    private String chatContactNumber = "";
    private static NotificationManager notificationManager;
    private static final String chatMessagesChannelId = "chatMessages"
                    ,servicesChannelId = "AllServices";
    private static final int RECEIVING_MESSAGE_SERVICE = 11;
    public static final int RECEIVING_MESSAGES = 1, NO_NOTIFICATION = -1;
    private static final ArrayList<Integer> notificationIds = new ArrayList<Integer>();
    private static OnMessageReceivedListener onMessageReceivedListener;
    private static OnMessageStateChangeListener messageStateChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, servicesChannelId)
                .setContentTitle("Receiving messages")
                .setContentText("Receiving messages service is running in the background")
                .setSilent(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(servicesChannelId,
                    "Running services", NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setShowBadge(false);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        startForeground(RECEIVING_MESSAGE_SERVICE, notification.build());
        reference_receiver.addChildEventListener(childListener_receiver);
        reference_sender.addChildEventListener(childListener_sender);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        chatContactNumber = intent.getExtras().getString("contactNumber");
        database = new Database(this);
        /*ArrayList<String> ids = database.getUndeliveredMessage();
        for(String id : ids)
        {
            reference_sender.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(id))
                    {
                        if (snapshot.child(id).hasChild("seen") &&
                                Boolean.parseBoolean(snapshot.child(id).child("seen").getValue().toString()))
                        {
                            database.update_state(id, false, true, true);
                            if (messageStateChangeListener != null)
                            {
                                messageStateChangeListener.messageStateChange(id,
                                        OnMessageStateChangeListener.seen);
                            }
                        } else if (snapshot.child(id).hasChild("delivered") &&
                                Boolean.parseBoolean(snapshot.child(id).child("delivered").getValue().toString()))
                        {
                            database.update_state(id, false, true, false);
                            if (messageStateChangeListener != null)
                            {
                                messageStateChangeListener.messageStateChange(id,
                                        OnMessageStateChangeListener.delivered);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }*/
        /*ids = database.getUnseenMessage();
        for(String id : ids)
        {
            reference_sender.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(id) && snapshot.child(id).hasChild("seen")
                            && Boolean.parseBoolean(snapshot.child(id).child("seen").getValue().toString()))
                    {
                        database.update_state(id, false, false, true);
                        if(messageStateChangeListener != null)
                            messageStateChangeListener.messageStateChange(id,
                                    OnMessageStateChangeListener.seen);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }*/
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final ChildEventListener childListener_receiver = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {
            if(!database.isMessageExist(snapshot.getKey()))
            {
                DatabaseReference reference_delivered_receiver = reference_receiver.child(snapshot.getKey()).child("delivered");
                Message receivedMessage = snapshot.getValue(Message.class);
                String contactName = database.getContactName(receivedMessage.getRoomNumber()).isEmpty() ?
                        receivedMessage.getRoomNumber() :
                        database.getContactName(receivedMessage.getRoomNumber());
                if (chatContactNumber.isEmpty() || !chatContactNumber.equals(receivedMessage.getRoomNumber()))
                {
                    int MessageCounter = database.getContactUnseenMessagesCounter(receivedMessage.getRoomNumber());
                    String messages = database.getUnseenContactMessages(receivedMessage.getRoomNumber());
                    messages = (messages.isEmpty() ? receivedMessage.getMsg() :
                            messages + "\n" + receivedMessage.getMsg());
                    sendNotification(receivedMessage.getRoomNumber(), contactName, messages, MessageCounter+1);
                }
                handleOnMessageReceiveListener(receivedMessage,
                        database.addChatRoom(receivedMessage.getRoomNumber(), contactName));
                database.addMessage(receivedMessage);
                reference_delivered_receiver.setValue(true);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {
            //Removing the message once it has been delivered and stored in the database
            DatabaseReference reference_changedMessage = reference_receiver.child(snapshot.getKey());
            if (Boolean.parseBoolean(snapshot.child("delivered").getValue().toString())) {
                //Change the delivered for the sender to true
                String senderNum = snapshot.child("sender").getValue().toString();
                DatabaseReference senderReference = App.reference_sentFrom.child(senderNum);
                DatabaseReference reference_delivered_Sender = senderReference.child(snapshot.getKey()).child("delivered");
                reference_delivered_Sender.setValue(true);
                /////////////
                reference_changedMessage.removeValue();
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    },
    childListener_sender = new ChildEventListener()
    {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {
            changeMessageState(snapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {
            changeMessageState(snapshot);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void sendNotification(String contactNumber, String contactName, String msg, int counter)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(chatMessagesChannelId,
                    "Chat Messages", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent notification = new Intent(ReceivingMessages.this, ChatActivity.class);
        notification.putExtra("contactNumber", contactNumber);
        notification.putExtra("contactName", contactName);
        notification.putExtra("class", RECEIVING_MESSAGES);
        notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ReceivingMessages.this,
                0, notification, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(ReceivingMessages.this, chatMessagesChannelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(contactName)
                .setContentText(counter + " New " + (counter == 1 ? "Message" : "Messages"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(msg.trim()))
                .setAutoCancel(true); //canceled when the user clicks it
        if(isNotificationExist(contactNumber) == NO_NOTIFICATION)
        {
            notificationIds.add(Integer.parseInt(contactNumber.substring(4)));
        }
        ///////////
        notificationManager.notify(Integer.parseInt(contactNumber.substring(4)), builder.build());
    }

    private static int isNotificationExist(String contactNumber)
    {
        final int contactId = Integer.parseInt(contactNumber.substring(4));
        for(int i = 0; i < notificationIds.size(); i++)
        {
            if(contactId == (notificationIds.get(i)))
            {
                return i;
            }
        }
        return NO_NOTIFICATION;
    }

    public static void removeNotification(String contactNumber)
    {
        int position = isNotificationExist(contactNumber);
        if(position != NO_NOTIFICATION)
        {
            notificationManager.cancel(notificationIds.get(position));
            notificationIds.remove(position);
        }
    }

    public static void addOnMessageReceivedListener(OnMessageReceivedListener listener)
    {
        onMessageReceivedListener = listener;
    }

    public static void addOnMessageStateChangeListener(OnMessageStateChangeListener listener)
    {
        ReceivingMessages.messageStateChangeListener = listener;
    }

    private void handleOnMessageReceiveListener(Message message, boolean newRoom)
    {
        if(onMessageReceivedListener != null) {
            if (newRoom) {
                onMessageReceivedListener.RoomAdded(new Room(
                        message.getRoomNumber(),
                        database.getContactName(message.getRoomNumber()),
                        message));
            } else {
                onMessageReceivedListener.roomChanged(message);
            }
            onMessageReceivedListener.MessageSaved(message);
        }
    }

    private void changeMessageState(DataSnapshot snapshot)
    {
        if(Boolean.parseBoolean(snapshot.child("seen").getValue().toString()))
        {
            DatabaseReference seenMessage = reference_sender.child(snapshot.getKey());
            database.updateMessageState(snapshot.getKey(), false, true, true);
            if(messageStateChangeListener != null)
                messageStateChangeListener.messageStateChange(snapshot.getKey(),
                        OnMessageStateChangeListener.seen);
            seenMessage.removeValue();
        }
        else if(Boolean.parseBoolean(snapshot.child("delivered").getValue().toString()))
        {
            database.updateMessageState(snapshot.getKey(), false, true, false);
            if(messageStateChangeListener != null)
                messageStateChangeListener.messageStateChange(snapshot.getKey(),
                        OnMessageStateChangeListener.delivered);
        }
    }
}
