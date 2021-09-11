package summer.project.whatsappFinal.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import summer.project.whatsappFinal.Contact;
import summer.project.whatsappFinal.Message;
import summer.project.whatsappFinal.Room;

public class Database extends SQLiteOpenHelper
{
    public static final String DatabaseName="app.dp";
    private Context con;
    public static final int MESSAGES_IN_A_SCREEN = 14;

    public Database(Context context)
    {
        super(context, DatabaseName, null, 1);
        this.con = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table user (phoneNumber TEXT primary key)");
        db.execSQL("create table Msg (id TEXT primary key, sender TEXT, receiver TEXT, Msg TEXT, Room_number Text, sent BOOLEAN, seen BOOLEAN , delivered BOOLEAN , date TEXT ,time TEXT,FOREIGN KEY (Room_number)  REFERENCES Room (Room_number)  )");
        db.execSQL("create table Room (Room_number TEXT primary key ,Room_name TEXT    )");
        db.execSQL("create table Contact (number TEXT primary key, name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS Msg");
        db.execSQL("DROP TABLE IF EXISTS Room");
        db.execSQL("DROP TABLE IF EXISTS Contact");
        onCreate(db);
    }

    //User
    public void insertUserData(String phoneNum)
    {
        SQLiteDatabase s= this.getWritableDatabase();
        ContentValues values =new ContentValues();
        values.put("phoneNumber",phoneNum);
        s.insert("user",null,values);
    }
    public String getUsrPhoneNumber()
    {
        SQLiteDatabase readableDatabase = this.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery("select phoneNumber from user",null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    ////////



    //Messages
    public void addMessage(Message message)
    {
        SQLiteDatabase r = this.getReadableDatabase();
        Cursor c =r.rawQuery("select id from Msg where id = '" + message.getId() + "'",null );
        c.moveToFirst();
        if(!c.isAfterLast())
        {
            c.close();
            return;
        }
        SQLiteDatabase s= this.getWritableDatabase();
        ContentValues values =new ContentValues();
        values.put("id", message.getId());
        values.put("sender", message.getSender());
        values.put("receiver",message.getReceiver());
        values.put("Msg",message.getMsg());
        values.put("Room_number", message.getRoomNumber());
        values.put("sent", message.getSent());
        values.put("delivered", message.getDelivered());
        values.put("seen", message.getSeen());
        values.put("date", message.getDate());
        values.put("time", message.getTime());
        s.insert("Msg",null,values);
        c.close();
    }

    public boolean isMessageExist(String id)
    {
        String sql = "Select id from MSG where id = '" + id +"'";
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(sql, null);
        return cursor.getCount() != 0;
    }

    public int getMessagesCount(String contactNumber)
    {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String sqlQuery = "select count(id) from msg where Room_number = '" + contactNumber + "'";
        Cursor cursor = readableDatabase.rawQuery(sqlQuery, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public Message getLastMessage(String contactNumber)
    {
        SQLiteDatabase s = this.getReadableDatabase();
        Cursor c =s.rawQuery("select * from Msg where Room_number = '"+contactNumber+"'" ,null );
        c.moveToLast();
        Message message = new Message();
        if(!c.isAfterLast()) {
            message = new Message(c.getString(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5), c.getString(6),
                    c.getString(7), c.getString(8), c.getString(9));
        }
        return message;
    }

    public ArrayList<Message> getUnsentMessages()
    {
        ArrayList <Message>arrayList =new ArrayList();
        SQLiteDatabase readableDatabase = this.getReadableDatabase();
        Cursor cursor =readableDatabase.rawQuery("select * from Msg where sender = '"+
                getUsrPhoneNumber() +"' and sent = 0",null );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            arrayList.add(new Message(cursor.getString(0),cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),cursor.getString(4),
                    cursor.getString(5),cursor.getString(6),cursor.getString(7),
                    cursor.getString(8),cursor.getString(9)));
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList ;

    }

    public Cursor getMessagesCursor(String contactNumber)
    {
        SQLiteDatabase readableDatabase = this.getReadableDatabase();
        String sqlQuery = "select * from Msg where Room_number =  '" + contactNumber + "'" +
                " order by id DESC";
        Cursor cursor =readableDatabase.rawQuery(sqlQuery ,null);
        cursor.moveToFirst();
        return cursor;
    }

    private Message getMessagesFromACursor(Cursor cursor)
    {
        return new Message(cursor.getString(0),cursor.getString(1),
                cursor.getString(2),cursor.getString(3),cursor.getString(4),
                cursor.getString(5),cursor.getString(6),cursor.getString(7),
                cursor.getString(8),cursor.getString(9));
    }

    public ArrayList<Message> getFirstMessages(Cursor cursor)
    {
        ArrayList<Message> messages = new ArrayList<Message>();
        cursor.moveToFirst();
        for(int i = 1; i <= MESSAGES_IN_A_SCREEN && !cursor.isAfterLast(); i++, cursor.moveToNext())
        {
            messages.add(getMessagesFromACursor(cursor));
        }
        return messages;
    }

    public Message getOneMessageAtATime(Cursor cursor)
    {
        Message message = null;
        if(!cursor.isAfterLast())
        {
            message = getMessagesFromACursor(cursor);
            cursor.moveToNext();
        }
        return message;
    }
    public int getContactUnseenMessagesCounter(String contactNumber)
    {
        String sql = "Select msg from MSG where sender = '" + contactNumber + "' and seen = '0'";
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(sql, null);
        return cursor.getCount();
    }

    public String getUnseenContactMessages(String contactNumber)
    {
        String messages = "",
        sql = "Select msg from MSG where sender = '" + contactNumber + "' and seen = '0'";
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(sql ,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            messages = messages + "\n" + cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }

    public void updateUnseenContactMessages(String contactNumber)
    {
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        String sql = "Update Msg set seen = 1 where sender = '" + contactNumber +"'";
        writableDatabase.execSQL(sql);
    }

    public void updateMessageState(String id, Boolean sent, Boolean delivered, Boolean seen)
    {
        SQLiteDatabase s = this.getWritableDatabase();
        String sentSQL = "UPDATE MSG SET sent = " + (sent ? 1 : 0) + " where id = '" + id + "'";
        String deliveredSQL = "UPDATE MSG SET delivered = " + (delivered ? 1 : 0) + " where id = '" + id + "'";
        String seenSQL = "UPDATE MSG SET seen = " + (seen ? 1 : 0) + " where id = '" + id + "'";
        if(sent)
        {
            s.execSQL(sentSQL);
        }
        if(delivered)
        {
            s.execSQL(deliveredSQL);
        }
        if(seen)
        {
            s.execSQL(seenSQL);
        }
    }
    //////////////////



    //Rooms
    public boolean addChatRoom(String Room_number ,String Room_name)
    {
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        if(isChatRoomExist(Room_number))
        {
            SQLiteDatabase readableDatabase = this.getReadableDatabase();
            Cursor cursor =readableDatabase.rawQuery("select Room_name from Room where" +
                    " Room_number = '" + Room_number + "'",null );
            cursor.moveToFirst();
            if(!Room_name.equals(cursor.getString(0)))
            {
                String query = "UPDATE Room Set Room_name = '" + Room_name +"' where Room_number " +
                        "= '" + Room_number + "'";
                writableDatabase.execSQL(query);
            }
            return false;
        }
        ContentValues values =new ContentValues();
        values.put("Room_number",Room_number);
        values.put("Room_name",Room_name);
        writableDatabase.insert("Room", null, values);
        return true;
    }

    public boolean isChatRoomExist(String contactNumber)
    {
        SQLiteDatabase r = this.getReadableDatabase();
        Cursor c =r.rawQuery("select Room_number from Room where Room_number = '" +
                contactNumber + "'",null );
        c.moveToFirst();
        return !c.isAfterLast();
    }

    public ArrayList<Room> getChatRooms(){
        ArrayList <Room>arrayList =new ArrayList();
        SQLiteDatabase readableDatabase = this.getReadableDatabase();
        String roomsQuery ="select Room.Room_number, Room.Room_name, max(id) as id "  +
                " from Room"  +
                " inner join Msg" +" on Room.Room_number = Msg.Room_number " +
                " group by Room.Room_number"+
                "  order by id DESC";
        Cursor roomsCursor =readableDatabase.rawQuery(roomsQuery ,null );
        roomsCursor.moveToFirst();
        while (!roomsCursor.isAfterLast())
        {
            arrayList.add(new Room(roomsCursor.getString(0), roomsCursor.getString(1),
                    getLastMessage(roomsCursor.getString(0))));
            roomsCursor.moveToNext();
        }
        roomsCursor.close();
        return arrayList;
    }

    private Contact getRoomIfExist(String contactNumber)
    {
        Contact contact = null;
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String sqlQuery = "select * from Room where Room_number = '" + contactNumber +"'";
        Cursor cursor = readableDatabase.rawQuery(sqlQuery, null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast())
        {
            contact = new Contact(cursor.getString(1), cursor.getString(0));
        }
        cursor.close();
        return contact;
    }
    ///////////



    //Contacts
    public void addContact(Contact contact)
    {
        SQLiteDatabase s = this.getReadableDatabase();
        SQLiteDatabase r= this.getWritableDatabase();
        Cursor c = s.rawQuery("select * from Contact where number = '" + contact.getNumber() + "'", null);
        c.moveToFirst();
        if(isChatRoomExist(contact.getNumber()))
        {
            Contact room = getRoomIfExist(contact.getNumber());
            if(!room.getName().equals(contact.getName()))
            {
                setRoomContactName(contact.getNumber(), contact.getName());
            }
        }
        if(!c.isAfterLast())
        {
            if(!c.getString(1).equals(contact.getName()))
            {
                String SQL = "update Contact set name = '" + contact.getName() +"' where number = '" + contact.getNumber() +"'";
                r.execSQL(SQL);
            }
            return;
        }
        ContentValues values =new ContentValues();
        values.put("number", contact.getNumber());
        values.put("name", contact.getName());
        r.insert("Contact", null, values);
    }

    public ArrayList<Contact> getContacts()
    {
        ArrayList <Contact>arrayList =new ArrayList();
        SQLiteDatabase readableDatabase = this.getReadableDatabase();
        Cursor c = readableDatabase.query("Contact", null, null, null, null,
                null, "name ASC", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            arrayList.add(new Contact(c.getString(1), c.getString(0)));
            c.moveToNext();
        }
        c.close();
        return arrayList;
    }

    public void setRoomContactName(String contactNumber, String name)
    {
        String query = "Update Room set Room_name = '" + name + "' where Room_number = '" +
                contactNumber + "'";
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    public String getContactName(String number)
    {
        String name = "";
        SQLiteDatabase s = this.getReadableDatabase();
        Cursor c = s.rawQuery("select name from Contact where number = '" + number + "'",null);
        c.moveToFirst();
        if (!c.isAfterLast())
            name = c.getString(0);
        return name;
    }
    /////////////
}