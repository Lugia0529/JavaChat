package Core;

public class AccountDetail
{
    private int guid;
    private String username;
    private String title;
    private String psm;
    private int status;
    
    public AccountDetail(int guid, String username, String title, String psm, int status)
    {
        this.guid = guid;
        this.username = username;
        this.title = title;
        this.psm = psm;
        this.status = status;
    }
    
    public int getGuid()
    {
        return guid;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getTitle()
    {
        return title.equals("") ? username : title;
    }
    
    public String getOriginalTitle()
    {
        return title;
    }
    
    public String getPSM()
    {
        return psm;
    }
    
    public int getStatus()
    {
        return status;
    }
    
    public void setGuid(int guid)
    {
        this.guid = guid;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
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
    
    public String getUITitle()
    {
        return String.format("JavaChat <%s>", getUsername());
    }
}
