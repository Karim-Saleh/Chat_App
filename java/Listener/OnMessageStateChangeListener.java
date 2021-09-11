package summer.project.whatsappFinal.Listener;

public interface OnMessageStateChangeListener
{
    public final static int delivered = 20, seen = 21;
    void messageStateChange(String messageId, int state);
}
