package Core;

import java.io.IOException;
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
        catch (Exception e) {}
        
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
            
            // 5 mins timeout
            socket.setSoTimeout(5 * 60 * 1000);
            
            // If connection is create succefully, send the login detail to the server.
            Packet loginPacket = new Packet(CMSG_LOGIN);
            loginPacket.put(username);
            loginPacket.put(password);
            
            SendPacket(loginPacket);
            
            // Create input stream.
            in = new ObjectInputStream(socket.getInputStream());
            
            Packet p = (Packet)in.readObject();
            
            switch(p.getOpcode())
            {
                case SMSG_LOGIN_SUCCESS: /* Login is success */
                    int accountGuid = (int)p.get();
                    String accountUsername = (String)p.get();
                    String accountTitle = (String)p.get();
                    String accountPSM = (String)p.get();
                    int accountStatus = (int)p.get();
                    
                    new Thread(new NetworkThread()).start();
                    
                    UIManager.getMasterUI().setAccountDetail(accountGuid, accountUsername, accountTitle, accountPSM, accountStatus);
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
        catch (IOException ioe)
        {
            UIManager.showMessageDialog("Unable to connect to server. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            UIManager.getMasterUI().enableLoginInput(true);
        }
        catch (Exception e)
        {
            UIManager.showMessageDialog("Unknown error occur, please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            UIManager.getMasterUI().enableLoginInput(true);
        }
    }
    
    public static void logout()
    {
        NetworkThread.stop();
        destroy();
        
        UIManager.switchUI();
        UIManager.getChatUIList().disposeAllUI();
        UIManager.getMasterUI().setTitle("Login");
        UIManager.getMasterUI().enableLoginInput(true);
        UIManager.getMasterUI().clearAccountDetail();
    }
    
    public static void getContactList()
    {
        SendPacket(new Packet(CMSG_GET_CONTACT_LIST));
    }
    
    public static void SendPacket(Packet p)
    {
        try
        {
            out.writeObject(p);
            out.flush();
        }
        catch (Exception e){}
    }
    
    public static Packet ReceivePacket() throws Exception
    {
        return (Packet)in.readObject();
    }
}
