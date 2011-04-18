package Core;

public class Contact
{
    private int guid;
    private String username;
    private String title;
    private String psm;
    private int status;
    
    public Contact(int guid, String username, String title, String psm, int status)
    {
        this.guid = guid;
        this.username = username;
        this.title = title;
        this.psm = psm;
        this.status = status;
    }
    
    public int getGuid()
    {
        return this.guid;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    
    public String getTitle()
    {
        return this.title;
    }
    
    public String getPSM()
    {
        return this.psm;
    }
    
    public int getStatus()
    {
        return this.status;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public void setPSM(String psm)
    {
        this.psm = psm;
    }
    
    public void setStatus(int status)
    {
        this.status = status;
    }
    
    // Overide toString() method, so the Contact list can show proper contact detail instead of instance memory location.
    public String toString()
    {
        String str = "";
        
        switch (status)
        {
            case 0:
                str += "<Online>";
                break;
            case 1:
                str += "<Away>";
                break;
            case 2:
                str += "<Busy>";
                break;
            case 3:
                str += "<Offline>";
                break;
        }
        
        if (title.isEmpty())
            str += username;
        else
            str += title;
        
        if (!psm.isEmpty())
            str += " - " + psm;
        
        return str;
    }
}
