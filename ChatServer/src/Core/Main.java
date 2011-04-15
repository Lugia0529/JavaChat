package Core;

import java.awt.Toolkit;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.ResultSet;

public class Main implements Opcode
{
    public static Vector<Client> clientList;
    public static ServerSocket serverSocket;
    public static Socket connectionSocket;
    public static Database db;
    public static long startupTime;
    
    public static void main(String[] args) throws Exception
    {
        clientList = new Vector<Client>();
        
        System.out.printf("Lugia Chat Server Beta\n\n");
        
        //Database Connection
        db = new Database("jdbc:mysql://localhost/chat?user=root&password=password");
        
        System.out.println();
        
        new Thread(new CliHandler()).start();
        
        serverSocket = new ServerSocket(6769);
        
        startupTime = System.currentTimeMillis();
        
        System.out.printf("\nSocket connection start.\n");
        
        Toolkit.getDefaultToolkit().beep();
        
        while(true)
        {
            connectionSocket = serverSocket.accept();
            
            ObjectInputStream in = new ObjectInputStream(connectionSocket.getInputStream());
            
            switch(in.readByte())
            {
                case CMSG_LOGIN:
                    System.out.println("Opcode: CMSG_LOGIN");
                    ResultSet rs = db.query("Select guid, username, title, psm, online from account where username='%s' and password='%s'", in.readObject(), in.readObject());
                    
                    if (rs.first())
                    {
                        if (rs.getInt(5) == 0)
                        {
                            Client c = new Client(rs.getInt(1), rs.getString(2));
                            
                            c.setTitle(rs.getString(3));
                            c.setPSM(rs.getString(4));
                            c.setInputStream(in);
                            c.setOutputStream(new ObjectOutputStream(connectionSocket.getOutputStream()));
                            
                            //db.execute("UPDATE account SET online=1 WHERE guid=%d", c.getGuid());
                            
                            System.out.println("Send Opcode: SMSG_LOGIN_SUCESS");
                            c.getOutputStream().writeByte(SMSG_LOGIN_SUCCESS);
                            c.getOutputStream().writeObject(c.getTitle());
                            c.getOutputStream().writeObject(c.getPSM());
                            c.getOutputStream().flush();
                            
                            clientList.add(c);
                            
                            new Thread(new Session(c)).start();
                        }
                        else
                        {
                            System.out.println("Send Opcode: SMSG_MULTI_LOGIN");
                            ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());
                            out.writeByte(SMSG_MULTI_LOGIN);
                            out.close();
                        }
                    }
                    else
                    {
                       System.out.println("Send Opcode: SMSG_LOGIN_FAILED");
                       ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());
                       out.writeByte(SMSG_LOGIN_FAILED);
                       out.close();
                    }
                    
                    rs = null;
                    break;
               default:
                   System.out.println("Unknow Opcode Receive");
                   break;
            }
        }
    }
}