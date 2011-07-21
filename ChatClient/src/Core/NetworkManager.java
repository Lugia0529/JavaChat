/*
 * Copyright (C) 2011 Lugia Programming Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    
    public static void login(String username, String password,int status)
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
            loginPacket.put(status);
            
            SendPacket(loginPacket);
            
            // Create input stream.
            in = new ObjectInputStream(socket.getInputStream());
            
            Packet p = (Packet)in.readObject();
            
            switch(p.getOpcode())
            {
                case SMSG_LOGIN_SUCCESS: /* Login is success */
                    int accountGuid = (Integer)p.get();
                    String accountUsername = (String)p.get();
                    String accountTitle = (String)p.get();
                    String accountPSM = (String)p.get();
                    int accountStatus = (Integer)p.get();
                    
                    new Thread(new NetworkThread()).start();
                    
                    UICore.getMasterUI().setAccountDetail(accountGuid, accountUsername, accountTitle, accountPSM, accountStatus);
                    UICore.switchUI();
                    break;
                case SMSG_LOGIN_FAILED: /* Login failed */
                    NetworkManager.destroy();
                    
                    UICore.showMessageDialog("The infomation you entered is not valid.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    UICore.getMasterUI().enableLoginInput(true);
                    
                    break;
                case SMSG_MULTI_LOGIN: /* Account is already login on other computer. */
                    NetworkManager.destroy();
                    
                    UICore.showMessageDialog("Your account is currently logged in on another computer. To log in here, please log out from the other computer.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    UICore.getMasterUI().enableLoginInput(true);
                    
                    break;
                default: /* Server problem? */
                    UICore.showMessageDialog("Unknown error occur, please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                    UICore.getMasterUI().enableLoginInput(true);
                    break;
            }
        }
        catch (IOException ioe)
        {
            UICore.showMessageDialog("Unable to connect to server. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            UICore.getMasterUI().enableLoginInput(true);
        }
        catch (Exception e)
        {
            UICore.showMessageDialog("Unknown error occur, please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            UICore.getMasterUI().enableLoginInput(true);
        }
    }
    
    public static void logout()
    {
        NetworkThread.stop();
        destroy();
        
        UICore.switchUI();
        UICore.getChatUIList().disposeAllUI();
        UICore.getRoomChatUIList().disposeAllUI();
        UICore.getMasterUI().setTitle("Login");
        UICore.getMasterUI().enableLoginInput(true);
        AccountDetail.clear();
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
