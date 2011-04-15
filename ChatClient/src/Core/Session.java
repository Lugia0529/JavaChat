package Core;

import java.io.*;
import java.net.*;

public class Session implements Opcode
{
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    public Session()
    {
    }
    
    public void destroy()
    {
        socket = null;
        in = null;
        out = null;
    }
    
    public byte login(String username, String password)
    {
        try
        {
            socket = new Socket("127.0.0.1", 6769);
            out = new ObjectOutputStream(socket.getOutputStream());
            
            out.writeByte(CMSG_LOGIN);
            out.writeObject(username);
            out.writeObject(password);
            out.flush();
            
            in = new ObjectInputStream(socket.getInputStream());
            
            return in.readByte();
        }
        catch (Exception e) { return 0; }
    }
    
    public void writeByte(byte b)
    {
        try
        {
            out.writeByte(b);
        }
        catch(Exception e) {}
    }
    
    public void writeObject(Object o)
    {
        try
        {
            out.writeObject(o);
        }
        catch(Exception e) {}
    }
    
    public void writeObject(Object... o)
    {
        try
        {
            for(int i = 0; i < o.length; i++)
                out.writeObject(o[i]);
        }
        catch(Exception e) {}
    }
    
    public byte readByte()
    {
        try
        {
            return in.readByte();
        }
        catch (Exception e) { return 0; }
    }
    
    public int readInt()
    {
        try
        {
            return in.readInt();
        }
        catch (Exception e) { return 0; }
    }
    
    public Object readObject()
    {
        try
        {
            return in.readObject();
        }
        catch (Exception e) { return null; }
    }
    
    public void flush()
    {
       try
       {
           out.flush();
       }
       catch (Exception e) {}
    }
}
