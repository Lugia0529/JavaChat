package Core;

public class Contact
{
    private int guid;
    private String username;
    private String title;
    private String psm;
    
    public Contact(int guid, String username, String title, String psm)
    {
        this.guid = guid;
        this.username = username;
        this.title = title;
        this.psm = psm;
    }
    
    public int getGuid()
    {
        return this.guid;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    // Overide toString() method, so the Contact list can show proper contact detail instead of instance memory location.
    public String toString()
    {
        if (title.isEmpty())
            return this.username;
        else
            return String.format("%s - %s", this.title, this.psm);
    }
}
