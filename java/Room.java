package summer.project.whatsappFinal;

public class Room
{
    private String contactName, contactNumber;
    private Message lastMessage;
    public static final int NEW_ROOM = -1;
    public Room() {}

    public Room(String contactNumber,String contactName, Message lastMessage)
    {
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.lastMessage = lastMessage;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}