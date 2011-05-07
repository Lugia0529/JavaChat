package Core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JOptionPane;

public class NetworkManager implements Opcode
{
    private static Socket socket;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;
    
    public NetworkManager()
    {
    }
    
    public static void destroy()
    {
        try { socket.close(); }
        catch (Exception e) {e.printStackTrace();}
        
        socket = null;
        in = null;
        out = null;
    }
    
    public static void login(String username, String password)
    {
        try
        {
            // Connect to the server.
            socket = new Socket("127.0.0.1", 6769);
            out = new ObjectOutputStream(socket.getOutputStream());
            
            // If connection is create succefully, send the login detail to the server.
            writeByte(CMSG_LOGIN);
            writeObject(username);
            writeObject(password);
            flush();
            
            // Create input stream.
            in = new ObjectInputStream(socket.getInputStream());
            
            switch(in.readByte())
            {
                case SMSG_LOGIN_SUCCESS: /* Login is success */
                    String name = String.format("%s", in.readObject());
                    String psm = String.format("%s", in.readObject());
                    
                    new Thread(new NetworkThread()).start();
                    
                    UIManager.getMasterUI().setAccountInfo(name, psm);
                    UIManager.switchUI();
                    break;
                case SMSG_LOGIN_FAILED: /* Login failed */
                    NetworkManager.destroy();
                    
                    UIManager.showMessageDialog("The infomation you entered is not valid.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    UIManager.getMasterUI().enableLoginInput(true);
                    
                    break;
                case SMSG_MULTI_LOGIN: /* Account is already login on other computer. */
                    NetworkManager.destroy();
                    
                    UIManager.showMessageDialog("Your account is currently logged in on another computer. To log in here, please log out from the other computer.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    UIManager.getMasterUI().enableLoginInput(true);
                    
                    break;
                default: /* Server problem? */
                    UIManager.showMessageDialog("Unknown error occur, please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                    UIManager.getMasterUI().enableLoginInput(true);
                    break;
            }
        }
        catch (Exception e){}
    }
    
    public static void logout()
    {
        writeByte(CMSG_LOGOUT);
        flush();
        
        NetworkThread.stop();
        destroy();
        
        UIManager.switchUI();
        UIManager.getChatUIList().disposeAllUI();
        UIManager.getMasterUI().setTitle("Login");
        UIManager.getMasterUI().enableLoginInput(true);
    }
    
    public static void getContactList()
    {
        writeByte(CMSG_GET_CONTACT_LIST);
        flush();
    }
    
    public static void writeByte(byte b)
    {
        try
        {
            out.writeByte(b);
        }
        catch(Exception e) {}
    }
    
    public static void writeInt(int i)
    {
        try
        {
            out.writeInt(i);
        }
        catch(Exception e) {}
    }
    
    public static void writeObject(Object o)
    {
        try
        {
            out.writeObject(o);
        }
        catch(Exception e) {}
    }
    
    public static void writeObject(Object... o)
    {
        try
        {
            for(int i = 0; i < o.length; i++)
                out.writeObject(o[i]);
        }
        catch(Exception e) {}
    }
    
    public static byte readByte()
    {
        try
        {
            return in.readByte();
        }
        catch (Exception e) { return 0; }
    }
    
    public static int readInt()
    {
        try
        {
            return in.readInt();
        }
        catch (Exception e) { return 0; }
    }
    
    public static Object readObject()
    {
        try
        {
            return in.readObject();
        }
        catch (Exception e) { return null; }
    }
    
    public static void flush()
    {
       try
       {
           out.flush();
       }
       catch (Exception e) {}
    }
}
