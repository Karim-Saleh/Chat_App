package summer.project.whatsappFinal.Listener;

import summer.project.whatsappFinal.Message;
import summer.project.whatsappFinal.Room;

public abstract class OnMessageReceivedListener
{
    public void MessageSaved(Message message){}
    public void RoomAdded(Room room){}
    public void roomChanged(Message newMessage){}
}
