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
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class Main implements Opcode
{
    public static ClientList clientList;
    public static RoomList roomList;
    public static ServerSocket serverSocket;
    public static Socket connectionSocket;
    public static Database db;
    public static long startupTime;
    
    public static void main(String[] args)
    {
        try
        {
            clientList = new ClientList();
            roomList = new RoomList();
            
            System.out.printf("Lugia Chat Server Beta\n\n");
            
            // Load Config
            Config.loadConfig();
            
            String dbname = Config.getStringDefault("DatabaseName", "chat");
            String dbuser = Config.getStringDefault("DatabaseUsername", "root");
            String dbpass = Config.getStringDefault("DatabasePassword", "password");
            
            // Database Connection
            db = new Database(String.format("jdbc:mysql://localhost/%s?user=%s%s", dbname, dbuser, !dbpass.equals("") ? "&password=" + dbpass : ""));
            
            // Start CLI
            new Thread(new CliHandler()).start();
            
            // Update account database online status to 0 at startup
            // Some client may not record as logout in database if the server crash
            System.out.printf("\nLogout all account\n");
            db.execute("UPDATE account SET online = 0");
            
            // Open socket
            int socketPort = Config.getIntDefault("ServerPort", 6769);
            
            System.out.printf("\nServer Port: %d\n" , socketPort);
            
            serverSocket = new ServerSocket(socketPort);
            
            System.out.printf("Socket connection start.\n");
            
            startupTime = System.currentTimeMillis();
            
            if (Config.getBoolDefault("BeepAtStart", true))
                Toolkit.getDefaultToolkit().beep();
        }
        catch (FileNotFoundException fnfe)
        {
            System.out.printf("Config file could not be loaded.\n");
            System.exit(0);
        }
        catch (MySQLSyntaxErrorException mysqle)
        {
            System.out.printf("Database Connection Fail. Please check for your database connection setting.\n");
            System.exit(0);
        }
        catch (BindException be)
        {
            System.out.printf("Fail to open acceptor, please check for the port is free.\n");
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.printf("Unknown error occur while starting the server.\n");
            System.out.printf("Message: %s\n", e.getMessage());
            System.exit(0);
        }
        
        while(true)
        {
            try
            {
                connectionSocket = serverSocket.accept();
                
                ObjectInputStream in = new ObjectInputStream(connectionSocket.getInputStream());
                
                Packet packet = (Packet)in.readObject();
                
                if (packet.getOpcode() == CMSG_LOGIN)
                {
                    System.out.printf("\nOpcode: CMSG_LOGIN\n");
                    
                    String username = (String)packet.get();
                    String password = (String)packet.get();
                    int status = (Integer)packet.get();
                    
                    ResultSet rs = db.query("Select guid, username, title, psm, online from account where username='%s' and password='%s'", username, password);
                    
                    if (rs.first())
                    {
                        if (rs.getInt(5) == 0)
                        {
                            Client c = new Client(rs.getInt(1), rs.getString(2));
                            
                            c.setTitle(rs.getString(3));
                            c.setPSM(rs.getString(4));
                            c.setStatus(status);
                            c.createSession(connectionSocket, in, new ObjectOutputStream(connectionSocket.getOutputStream()));
                            
                            System.out.printf("%s (guid: %d) logged in as status %d.\n", c.getUsername(), c.getGuid(), c.getStatus());
                            
                            db.execute("UPDATE account SET online = 1 WHERE guid = %d", c.getGuid());
                            
                            System.out.printf("Send Opcode: SMSG_LOGIN_SUCESS\n");
                            
                            Packet p = new Packet(SMSG_LOGIN_SUCCESS);
                            p.put(c.getGuid());
                            p.put(c.getUsername());
                            p.put(c.getTitle());
                            p.put(c.getPSM());
                            p.put(c.getStatus());
                            
                            c.getSession().SendPacket(p);
                            
                            c.getSession().InformOthersForStatusChange();
                            
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
                }
                else
                {
                    System.out.printf("\nInvalid Opcode Receive: 0x%02X\n", packet.getOpcode());
                }
            }
            catch (Exception e)
            {
                System.out.printf("Exception % occur with message %s\n", e.getClass(), e.getMessage());
            }
        }
    }
}