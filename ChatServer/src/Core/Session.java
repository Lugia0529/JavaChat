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
                        
                        System.out.printf("Send recent contact request to client %d.\n", c.getGuid());
                        
                        ResultSet requestRS = Main.db.query("SELECT a.guid, a.username FROM contact_request AS c LEFT JOIN account AS a ON c.r_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
                        
                        while (requestRS.next())
                        {
                            System.out.printf("Send Contact Request: %s to client %d\n", requestRS.getString(2), c.getGuid());
                            out.writeByte(SMSG_CONTACT_REQUEST);
                            out.writeInt(requestRS.getInt(1));
                            out.writeObject(requestRS.getString(2));
                            out.flush();
                        }
                        
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
                        System.out.printf("\nOpcode: CMSG_ADD_CONTACT\n");
                        
                        String username = String.format("%s", c.getInputStream().readObject());
                        
                        // Contact to add is self
                        if (c.getUsername().equalsIgnoreCase(username))
                        {
                            System.out.printf("Client %d add self to contact list.\n", c.getGuid());
                            
                            if (!Main.db.query("SELECT id FROM contact WHERE o_guid = %d AND c_guid= %d", c.getGuid(), c.getGuid()).first())
                            {
                                out.writeByte(SMSG_ADD_CONTACT_SUCCESS);
                                out.writeInt(c.getGuid());
                                out.writeObject(c.getUsername());
                                out.writeObject(c.getTitle());
                                out.writeObject(c.getPSM());
                                out.writeInt(c.getStatus());
                                out.flush();
                                
                                Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), c.getGuid());
                            }
                            break;
                        }
                        
                        ResultSet ars = Main.db.query("SELECT guid, username, title, psm FROM account WHERE username = '%s'", username);
                        
                        if (ars.first())
                        {
                            int guid = ars.getInt(1);
                            username = ars.getString(2);
                            String title = ars.getString(3);
                            String psm = ars.getString(4);
                            
                            ResultSet acrs = Main.db.query("SELECT id FROM contact WHERE o_guid = %d and c_guid = %d", c.getGuid(), guid);
                            
                            if (acrs.first())
                                out.writeByte(SMSG_CONTACT_ALREADY_IN_LIST);
                            else
                            {
                                System.out.printf("Send Contact: %s to client %d\n", username, c.getGuid());
                                
                                Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), guid);
                                
                                Client target = null;
                                    
                                for (ListIterator<Client> i = Main.clientList.listIterator(); i.hasNext(); )
                                {
                                    Client temp = i.next();
                                
                                    if (temp.getGuid() == guid)
                                    {
                                        target = temp;
                                        break;
                                    }
                                }
                                
                                int currentStatus = 3;
                                
                                ResultSet ccrs = Main.db.query("SELECT id FROM contact WHERE o_guid = %d and c_guid = %d", guid, c.getGuid());
                                
                                if (!ccrs.first())
                                {
                                    if (target != null)
                                    {
                                        System.out.printf("Send Contact Request: %s to client %d\n", c.getUsername(), guid);
                                        target.getOutputStream().writeByte(SMSG_CONTACT_REQUEST);
                                        target.getOutputStream().writeInt(c.getGuid());
                                        target.getOutputStream().writeObject(c.getUsername());
                                        target.getOutputStream().flush();
                                    }
                                    else
                                        Main.db.execute("INSERT INTO contact_request(o_guid, r_guid) VALUES(%d, %d)", guid, c.getGuid());
                                }
                                else
                                {
                                    System.out.printf("Send Contact Request Cancel: %s is already in contact list of %s.\n", c.getUsername(), username);
                                    
                                    if (target != null)
                                    {
                                        currentStatus = target.getStatus();
                                        
                                        System.out.printf("Send status change From: %d, To: %d, Status: %d\n", c.getGuid(), guid, c.getStatus());
                                        target.getOutputStream().writeByte(SMSG_STATUS_CHANGED);
                                        target.getOutputStream().writeInt(c.getGuid());
                                        target.getOutputStream().writeInt(c.getStatus());
                                        target.getOutputStream().flush();
                                    }
                                }
                                
                                out.writeByte(SMSG_ADD_CONTACT_SUCCESS);
                                out.writeInt(guid);
                                out.writeObject(username);
                                out.writeObject(title);
                                out.writeObject(psm);
                                out.writeInt(currentStatus);
                            }
                        }
                        else
                        {
                            out.writeByte(SMSG_CONTACT_NOT_FOUND);
                        }
                        
                        out.flush();
                        
                        break;
                    case CMSG_CONTACT_ACCEPT:
                        System.out.printf("\nOpcode: CMSG_CONTACT_ACCEPT\n");
                        
                        int guid = c.getInputStream().readInt();
                        
                        Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), guid);
                        Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), guid);
                        
                        ResultSet rrs = Main.db.query("SELECT username, title, psm FROM account WHERE guid = %d", guid);
                        
                        Client requestor = null;
                        
                        for (ListIterator<Client> i = Main.clientList.listIterator(); i.hasNext(); )
                        {
                            Client temp = i.next();
                        
                            if (temp.getGuid() == guid)
                            {
                                requestor = temp;
                                break;
                            }
                        }
                        
                        int requestorStatus = requestor != null ? requestor.getStatus() : 3;
                        
                        if (rrs.first())
                        {
                            System.out.printf("Send Contact: %s to client %d\n", rrs.getString(1), c.getGuid());
                            out.writeByte(SMSG_ADD_CONTACT_SUCCESS);
                            out.writeInt(guid);
                            out.writeObject(rrs.getString(1));
                            out.writeObject(rrs.getString(2));
                            out.writeObject(rrs.getString(3));
                            out.writeInt(requestorStatus);
                            out.flush();
                        }
                        
                        if (requestor != null)
                        {
                            System.out.printf("Send status change From: %d, To: %d, Status: %d\n", c.getGuid(), guid, c.getStatus());
                            requestor.getOutputStream().writeByte(SMSG_STATUS_CHANGED);
                            requestor.getOutputStream().writeInt(c.getGuid());
                            requestor.getOutputStream().writeInt(c.getStatus());
                            requestor.getOutputStream().flush();
                        }
                        
                        break;
                    case CMSG_CONTACT_DECLINE:
                        System.out.printf("\nOpcode: CMSG_CONTACT_DECLINE\n");
                        
                        int c_guid = c.getInputStream().readInt();
                        
                        Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), c_guid);
                        
                        break;
                    case CMSG_REMOVE_CONTACT:
                        System.out.printf("\nOpcode: CMSG_REMOVE_CONTACT\n");
                        
                        int r_guid = c.getInputStream().readInt();
                        
                        Main.db.execute("DELETE FROM contact WHERE o_guid = %d AND c_guid = %d", c.getGuid(), r_guid);
                        
                        for (ListIterator<Client> i = Main.clientList.listIterator(); i.hasNext(); )
                        {
                            Client temp = i.next();
                        
                            if (temp.getGuid() == r_guid)
                            {
                                temp.getOutputStream().writeByte(SMSG_STATUS_CHANGED);
                                temp.getOutputStream().writeInt(c.getGuid());
                                temp.getOutputStream().writeInt(3);
                                temp.getOutputStream().flush();
                                break;
                            }
                        }
                        
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
