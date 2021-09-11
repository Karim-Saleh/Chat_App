package summer.project.whatsappFinal;

public class User
{
    private final String phoneNumber, uid;

    public User(String phoneNumber, String uid) {
        this.phoneNumber = phoneNumber;
        this.uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUid() {
        return uid;
    }
}
