package summer.project.whatsappFinal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import summer.project.whatsappFinal.Database.Database;
import summer.project.whatsappFinal.Listener.OnContactLoadFinishListener;

public class App
{
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private static final DatabaseReference reference_users = root.getReference("users");
    private static final DatabaseReference reference_messages = root.getReference("Messages");
    public static final DatabaseReference reference_sentFrom = reference_messages.child("Sent from"),
            reference_toBeReceived = reference_messages.child("To be received");
    private static String mVerificationId;
    private static PhoneAuthProvider.ForceResendingToken mResendToken;
    private static PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private static long maxCounter = 60000, diff = 1000;
    private static int BUTTON_RESEND_ENABLED;
    private static OnContactLoadFinishListener contactLoadFinishListener;

    // Phone Authentication
    public static void phoneAuthentication(Context context, Activity verificationActivity, String phoneNumber, Database database) {
        initializeAutoVerification(context, verificationActivity, database);
        sendVerificationCode(verificationActivity, phoneNumber);
    }

    private static void initializeAutoVerification(Context context, Activity verificationActivity,
                                                   Database database) {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                //Auto verification completed
                //Signing the user in
                signIn(credential, verificationActivity, database);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //Auto verification error
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                BUTTON_RESEND_ENABLED = verificationActivity.getResources().getColor(R.color.five_dark_kashmir);
                TextView counter = verificationActivity.findViewById(R.id.textView_counter);
                Button button_resend = (Button) verificationActivity.findViewById(R.id.button_resend);
                new CountDownTimer(maxCounter, diff) {
                    public void onTick(long millisUntilFinished) {
                        counter.setText("" + (millisUntilFinished / 1000));
                        //here you can have your logic to set text to edittext
                    }

                    public void onFinish() {
                        counter.setText("");
                        button_resend.setTextColor(BUTTON_RESEND_ENABLED);
                        button_resend.setEnabled(true);
                    }
                }.start();

                //taking the verification code sent in case the auto verification didn't work
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
    }

    private static void sendVerificationCode(Activity verificationActivity, String phoneNumber)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(verificationActivity)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private static void signIn(PhoneAuthCredential credential, Activity verificationActivity, Database database) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(
                verificationActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in succeed
                            //start the next activity
                            saveUserLoginData(database);
                            Intent intent = new Intent(verificationActivity,
                                    RoomsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            verificationActivity.startActivity(intent);
                        } else {
                            //Sign in failed
                            EditText verCode = (EditText) verificationActivity
                                    .findViewById(R.id.editText_verificationCode);
                            verCode.setError("Code not valid");
                        }
                    }
                });
    }

    public static void manualVerify(String code, Activity verificationActivity, Database database) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signIn(credential, verificationActivity, database);
    }

    public static void resendVerificationCode(String phoneNumber, Activity verificationActivity) {
        maxCounter = 120000;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(2L, TimeUnit.MINUTES)
                        .setActivity(verificationActivity)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(mResendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private static void saveUserLoginData(Database database) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = new User(currentUser.getPhoneNumber(), currentUser.getUid());
        database.insertUserData(user.getPhoneNumber());
        reference_users.child(user.getPhoneNumber()).child("uid").setValue(user.getUid());
        reference_users.child(user.getPhoneNumber()).child("telNum").setValue(user.getPhoneNumber());
    }
    ////////////////////////

    //Contacts
    public static void getContactList(Context context, Database database)
    {
        ArrayList<Contact> allContacts = new ArrayList<Contact>();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+ " Asc";
        Cursor cursor = context.getContentResolver().query(uri,null,null,null,sort);
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Uri uriphone  =ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =?";
                Cursor phoneCursor = context.getContentResolver().query(uriphone,null,selection,new String[] {id},null);
                if (phoneCursor.moveToNext())
                {
                    @SuppressLint("Range") String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    number = number.replaceAll("[\\D]", "");
                    if(number.length() == 12)
                    {
                        number = "+" + number;
                    }
                    else if (number.length() == 11)
                    {
                        number = "+2" + number;
                    }
                    Contact mode = new Contact();
                    mode.setName(name);
                    mode.setNumber(number);
                    allContacts.add(mode);
                    phoneCursor.close();
                }
            }
            cursor.close();
        }
        reference_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(int i = 0; i < allContacts.size(); i++)
                {
                    if(snapshot.hasChild(allContacts.get(i).getNumber()))
                    {
                        if(!FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().equals(allContacts.get(i).getNumber()))
                        {
                            database.addContact(allContacts.get(i));
                        }
                    }
                    if(contactLoadFinishListener != null)
                        contactLoadFinishListener.onLoadFinish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //////////


    //Messages
    public static Message sendMessage(String receiverNum, String msg, Context con,
                                      Database database, ChatAdapter chatAdapter,
                                      ArrayList<Message> messages)
    {
        String userPhoneNumber = getUserPhoneNumber(database);
        DatabaseReference reference_sender = reference_sentFrom.child(userPhoneNumber),
                reference_receiver = reference_toBeReceived.child(receiverNum);
        String tmp_key = reference_receiver.push().getKey();
        //////////
        String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());
        //////////

        //Sent message receiver
        Message sentMessage_receiver = new Message(tmp_key, userPhoneNumber, receiverNum, msg,
                userPhoneNumber, "false", "false", "false", date, time);;
        reference_receiver.child(tmp_key).setValue(sentMessage_receiver);
        //////////
        Message sentMessage_sender = new Message(tmp_key, userPhoneNumber, receiverNum, msg,
                receiverNum, "false", "false", "false", date, time);
        database.addMessage(sentMessage_sender);

        //Sent message sender
        reference_sender.child(tmp_key).setValue(new Message()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.updateMessageState(tmp_key, true, false, false);
                sentMessage_sender.setSent(true);
                int position = findMessage(messages, tmp_key);
                if(position != -1)
                {
                    chatAdapter.notifyItemChanged(position);
                }
            }
        });
        /////////////////////
        return sentMessage_sender;
    }

    public static void sendUnsentMessages(Database database, RoomsAdapter roomsAdapter, ArrayList<Room> rooms)
    {
        String userPhoneNumber = getUserPhoneNumber(database);
        DatabaseReference reference_sender = reference_sentFrom.child(userPhoneNumber);
        for(Message message : database.getUnsentMessages())
        {
            DatabaseReference reference_receiver = reference_toBeReceived.child(message.getReceiver());
            String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
            String date = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());
            message.setTime(time); message.setDate(date);

            //For receiver
            message.setRoomNumber(message.getSender());
            reference_receiver.child(message.getId()).setValue(message);

            //For sender
            reference_sender.child(message.getId()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    database.updateMessageState(message.getId(), true, false, false);
                    for(int i = 0; i < rooms.size(); i++)
                    {
                        if(message.getId().equals(rooms.get(i).getLastMessage().getId()))
                        {
                            rooms.get(i).getLastMessage().setSent(true);
                            roomsAdapter.notifyItemChanged(i);
                        }
                    }
                }
            });
        }
    }

    public static int findMessage(ArrayList<Message> messages, String id)
    {
        for(int i = 0; i < messages.size(); i++)
        {
            if(messages.get(i).getId().equals(id))
            {
                return i;
            }
        }
        return -1;
    }

    public static void addOnLoadContactFinishListener(OnContactLoadFinishListener listener)
    {
        contactLoadFinishListener = listener;
    }
    //////////

    //Checkers
    public static boolean isRightEgyptNumber(String phoneNumber) {
        return (phoneNumber.length() == 11 && phoneNumber.charAt(0) == '0' &&
                phoneNumber.charAt(1) == '1' && (phoneNumber.charAt(2) == '2' ||
                phoneNumber.charAt(2) == '1' || phoneNumber.charAt(2) == '0' ||
                phoneNumber.charAt(2) == '5'));
    }

    public static boolean internetIsConnected() //not Working on emulator
    {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
    //Getters
    public static DatabaseReference getReference_sentFrom()
    {
        return reference_sentFrom;
    }

    public static String getUserPhoneNumber(Database database)
    {
        String userPhoneNumber = database.getUsrPhoneNumber();
        return userPhoneNumber;
    }
    /////////

}
