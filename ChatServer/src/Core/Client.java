package Core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
 
public class Client
{
    private int guid;
    private String username;
    private String title;
    private String psm;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    public Client(int guid, String username)
    {
        this.guid = guid;
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
    
    public void setInputStream(ObjectInputStream in)
    {
        this.in = in;
    }
    
    public void setOutputStream(ObjectOutputStream out)
    {
        this.out = out;
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
    
    public ObjectInputStream getInputStream()
    {
        return this.in;
    }
    
    public ObjectOutputStream getOutputStream()
    {
        return this.out;
    }
}