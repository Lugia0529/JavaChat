package Core;

import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.util.ListIterator;

public class Session implements Runnable, Opcode
{
    private Client c;
    private ObjectOutputStream out;
    
    private volatile Thread session;
    
    public Session(Client c)
    {
        this.c = c;
        this.out = c.getOutputStream();
    }
    
    public void stop()
    {
        session = null;
    }
    
    public void run()
    {
        try
        {
            this.session = Thread.currentThread();
            
            while(session == Thread.currentThread())
            {
                byte b = c.getInputStream().readByte();
                
                switch(b)
                {
                    case CMSG_GET_CONTACT_LIST:
                        System.out.printf("\nOpcode: CMSG_GET_CONTACT_LIST\n");
                        
                        ResultSet rs = Main.db.query("SELECT a.guid, a.username, a.title, a.psm FROM contact AS c LEFT JOIN account AS a ON c.c_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
                        
                        while(rs.next())
                        {
                            int guid = rs.getInt(1);
                            String username = rs.getString(2);
                            String title = rs.getString(3);
                            String psm = rs.getString(4);
                            
                            // initial client status as appear offline, if it is online, status will change on for loop checking below.
                            int status = 3;
                            
                            for (ListIterator<Client> i = Main.clientList.listIterator(); i.hasNext(); )
                            {
                                Client target = i.next();
                                
                                if (target.getGuid() == rs.getInt(1))
                                {
                                    status = target.getStatus();
                                    break;
                                }
                            }
                            
                            out.writeByte(SMSG_CONTACT_DETAIL);
                            out.writeInt(guid);
                            out.writeObject(username);
                            out.writeObject(title);
                            out.writeObject(psm);
                            out.writeInt(status);
                            out.flush();
                            
                            Thread.sleep(10);
                            System.out.printf("Send Contact: %s to client %d\n", rs.getString(2), c.getGuid());
                        }
                        
                        System.out.print("Send Opcode: SMSG_CONTACT_LIST_ENDED\n");
                        out.writeByte(SMSG_CONTACT_LIST_ENDED);
                        out.flush();
                        
                        System.out.printf("Send contact: Finish\n");
                        
                        break;
                    case CMSG_LOGOUT:
                        System.out.printf("\nOpcode: CMSG_LOGOUT\n");
                        
                        Main.clientList.remove(c);
                        
                        System.out.printf("Closing client socket %d.\n", c.getGuid());
                        c.getSocket().close();
                        
                        Main.db.execute("UPDATE account SET online = 0 WHERE guid = %d", c.getGuid());
                        System.out.printf("Stopping session thread %d.\n", c.getGuid());
                        
                        stop();
                        
                        break;
                    case CMSG_STATUS_CHANGED:
                        System.out.printf("\nOpcode: CMSG_STATUS_CHANGED\n");
                        
                        int toStatus = c.getInputStream().readInt();
                        c.setStatus(toStatus);
                        
                        System.out.printf("Client %d change status to %d.\n" , c.getGuid(), toStatus);
                        
                        ResultSet crs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
                        
                        while(crs.next())
                        {
                            int c_guid = crs.getInt(1);
                            
                            for (ListIterator<Client> i = Main.clientList.listIterator(); i.hasNext(); )
                            {
                                Client target = i.next();
                                
                                if (target.getGuid() == c_guid)
                                {
                                    System.out.printf("Send status change From: %d, To: %d, Status: %d\n", c.getGuid(), c_guid, toStatus);
                                    target.getOutputStream().writeByte(SMSG_STATUS_CHANGED);
                                    target.getOutputStream().writeInt(c.getGuid());
                                    target.getOutputStream().writeInt(toStatus);
                                    target.getOutputStream().flush();
                                    break;
                                }
                            }
                        }
                        
                        System.out.printf("Client %d update status to %d: Finish.\n", c.getGuid(), toStatus);
                        
                        break;
                    case CMSG_ADD_CONTACT:
                        break;
                    case CMSG_REMOVE_CONTACT:
                        break;
                    case CMSG_SEND_CHAT_MESSAGE:
                        System.out.printf("\nOpcode: CMSG_SEND_CHAT_MESSAGE\n");
                        
                        int from = c.getGuid();
                        int to = c.getInputStream().readInt();
                        String message = String.format("%s", c.getInputStream().readObject());
                        
                        System.out.printf("Chat Message Receive From: %d, To %d, Message: %s\n", from, to, message);
                        
                        for (ListIterator<Client> i = Main.clientList.listIterator(); i.hasNext(); )
                        {
                            Client target = i.next();
                            if (target.getGuid() == to)
                            {
                                target.getOutputStream().writeByte(SMSG_SEND_CHAT_MESSAGE);
                                target.getOutputStream().writeInt(from);
                                target.getOutputStream().writeObject(message);
                                target.getOutputStream().flush();
                                System.out.printf("Send message success\n");
                                break;
                            }
                        }
                        
                        break;
                    default:
                        System.out.printf("Unknown Opcode Receive\n");
                        break;
                }
            }
            
            System.out.printf("Session thread %d stopped successfully.\n", c.getGuid());
            
            c = null;
            out = null;
        }
        catch(Exception e){}
    }
}
