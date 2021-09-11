package summer.project.whatsappFinal;

public class Message
{
    private String id, msg, sender, receiver, roomNumber, date, time;
    private boolean sent, delivered, seen;
    public Message() {}

    public Message(String msg, String date, String time)
    {
        this.msg = msg;
        this.date = date;
        this.time = time;
    }


    public Message(String id,  String sender, String receiver,String msg,
                   String roomNumber,String sent, String seen,String delivered,
                   String date, String time )
    {
        this.id = id;
        this.msg = msg;
        this.sender = sender;
        this.receiver = receiver;
        this.roomNumber = roomNumber;
        this.date = date;
        this.time = time;
        this.sent = sent.equals("1");
        this.delivered = delivered.equals("1");
        this.seen = seen.equals("1");
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean getSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String data) {
        this.date = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
