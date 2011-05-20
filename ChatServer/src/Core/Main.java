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

import java.awt.Toolkit;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;

public class Main implements Opcode
{
    public static ClientList clientList;
    public static ServerSocket serverSocket;
    public static Socket connectionSocket;
    public static Database db;
    public static long startupTime;
    
    public static void main(String[] args) throws Exception
    {
        clientList = new ClientList();
        
        System.out.printf("Lugia Chat Server Beta\n\n");
        
        //Database Connection
        db = new Database("jdbc:mysql://localhost/chat?user=root&password=password");
        
        System.out.println();
        
        new Thread(new CliHandler()).start();
        
        serverSocket = new ServerSocket(6769);
        
        startupTime = System.currentTimeMillis();
        
        //Update account database online status to 0 at startup
        //some client may not record as logout in database if the server crash
        System.out.printf("\nLogout all account\n");
        db.execute("UPDATE account SET online = 0");
        
        System.out.printf("\nSocket connection start.\n");
        
        Toolkit.getDefaultToolkit().beep();
        
        while(true)
        {
            connectionSocket = serverSocket.accept();
            
            ObjectInputStream in = new ObjectInputStream(connectionSocket.getInputStream());
            
            Packet loginPacket = (Packet)in.readObject();
            
            switch(loginPacket.getOpcode())
            {
                case CMSG_LOGIN:
                    System.out.printf("Opcode: CMSG_LOGIN\n");
                    
                    String username = (String)loginPacket.get();
                    String password = (String)loginPacket.get();
                    
                    ResultSet rs = db.query("Select guid, username, title, psm, online from account where username='%s' and password='%s'", username, password);
                    
                    if (rs.first())
                    {
                        if (rs.getInt(5) == 0)
                        {
                            Client c = new Client(rs.getInt(1), rs.getString(2));
                            
                            c.setTitle(rs.getString(3));
                            c.setPSM(rs.getString(4));
                            c.setStatus(0);
                            c.createSession(connectionSocket, in, new ObjectOutputStream(connectionSocket.getOutputStream()));
                            
                            db.execute("UPDATE account SET online = 1 WHERE guid = %d", c.getGuid());
                            
                            System.out.printf("Send Opcode: SMSG_LOGIN_SUCESS\n");
                            
                            Packet p = new Packet(SMSG_LOGIN_SUCCESS);
                            p.put(c.getGuid());
                            p.put(c.getUsername());
                            p.put(c.getTitle());
                            p.put(c.getPSM());
                            p.put(c.getStatus());
                            
                            c.getSession().SendPacket(p);
                            
                            clientList.add(c);
                        }
                        else
                        {
                            System.out.println("Send Opcode: SMSG_MULTI_LOGIN\n");
                            ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());
                            out.writeObject(new Packet(SMSG_MULTI_LOGIN));
                            out.close();
                        }
                    }
                    else
                    {
                       System.out.printf("Send Opcode: SMSG_LOGIN_FAILED\n");
                       ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());
                       out.writeObject(new Packet(SMSG_LOGIN_FAILED));
                       out.close();
                    }
                    
                    rs = null;
                    break;
                default:
                    System.out.printf("\nInvalid Opcode Receive: 0x%02X\n", loginPacket.getOpcode());
                    break;
            }
        }
    }
}