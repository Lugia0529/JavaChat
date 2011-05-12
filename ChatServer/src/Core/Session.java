package Core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;

public class Session implements Runnable, Opcode
{
    private Client c;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Timer timer;
    
    private volatile Thread session;
    
    private long pingTicks;
    
    public Session(Client c, ObjectInputStream in, ObjectOutputStream out)
    {
        this.c = c;
        this.in = in;
        this.out = out;
    }
    
    public void stop()
    {
        timer.cancel();
        
        session = null;
        timer = null;
    }
    
    public Packet ReceivePacket() throws Exception
    {
        return (Packet)in.readObject();
    }
    
    public void SendPacket(Packet p) throws Exception
    {
        out.writeObject(p);
        out.flush();
    }
    
    public void run()
    {
        try
        {
            this.session = Thread.currentThread();
            
            while(session == Thread.currentThread())
            {
                Packet p = ReceivePacket();
                
                switch(p.getOpcode())
                {
                    case CMSG_GET_CONTACT_LIST:
                        HandleGetContactListOpcode();
                        break;
                    case CMSG_LOGOUT:
                        HandleLogoutOpcode();
                        break;
                    case CMSG_STATUS_CHANGED:
                        HandleStatusChangedOpcode(p);
                        break;
                    case CMSG_ADD_CONTACT:
                        HandleAddContactOpcode(p);
                        break;
                    case CMSG_CONTACT_ACCEPT:
                        HandleContactAcceptOpcode(p);
                        break;
                    case CMSG_CONTACT_DECLINE:
                        HandleContactDeclineOpcode(p);
                        break;
                    case CMSG_REMOVE_CONTACT:
                        HandleRemoveContactOpcode(p);
                        break;
                    case CMSG_SEND_CHAT_MESSAGE:
                        HandleChatMessageOpcode(p);
                        break;
                    case CMSG_TIME_SYNC_RESP:
                        HandleTimeSyncRespOpcode(p);
                        break;
                    case CMSG_PING:
                        HandlePingOpcode();
                        break;
                    default:
                        System.out.printf("\nUnknown Opcode Receive: 0x%02X\n", p.getOpcode());
                        break;
                }
            }
        }
        catch (SocketException se)
        {
            System.out.printf("\nClient %s (guid: %d) connection was closed unexpectedly. (possible disconnected?)\n", c.getUsername(), c.getGuid());
            
            Logout();
        }
        catch (SocketTimeoutException ste)
        {
            // Client will send a Time Sync every 10 sec.
            // Every 30 sec, the server will request the client to send a ping acknowledgement.
            // If a client does not send any packet for 60 seconds, we consider that it is disconnected.
            System.out.printf("\nClient %s (guid: %d) is not respond for 60 seconds. (possible disconnected?)\n", c.getUsername(), c.getGuid());
            
            Logout();
        }
        catch (Exception e){}
        
        System.out.printf("Session thread %d stopped successfully.\n", c.getGuid());
    }
    
    void HandleGetContactListOpcode(/* Packet packet */) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_GET_CONTACT_LIST\n");
        
        ResultSet rs = Main.db.query("SELECT a.guid, a.username, a.title, a.psm FROM contact AS c LEFT JOIN account AS a ON c.c_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
        
        Packet p;
        
        while(rs.next())
        {
            int guid = rs.getInt(1);
            String username = rs.getString(2);
            String title = rs.getString(3);
            String psm = rs.getString(4);
            
            Client target = Main.clientList.findClient(rs.getInt(1));
            
            int status = target != null ? target.getStatus() : 3;
            
            p = new Packet(SMSG_CONTACT_DETAIL);
            p.put(guid);
            p.put(username);
            p.put(title);
            p.put(psm);
            p.put(status);
            
            SendPacket(p);
            
            System.out.printf("Send Contact: %s to client %d\n", rs.getString(2), c.getGuid());
            
            Thread.sleep(10);
        }
        
        System.out.print("Send Opcode: SMSG_CONTACT_LIST_ENDED\n");
        
        SendPacket(new Packet(SMSG_CONTACT_LIST_ENDED));
        
        System.out.printf("Send contact: Finish\n");
        
        System.out.printf("Send recent contact request to client %d.\n", c.getGuid());
        
        ResultSet requestRS = Main.db.query("SELECT a.guid, a.username FROM contact_request AS c LEFT JOIN account AS a ON c.r_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
        
        while (requestRS.next())
        {
            System.out.printf("Send Contact Request: %s to client %d\n", requestRS.getString(2), c.getGuid());
            
            p = new Packet(SMSG_CONTACT_REQUEST);
            p.put(requestRS.getInt(1));
            p.put(requestRS.getString(2));
            
            SendPacket(p);
            
            Thread.sleep(10);
        }
        
        // We start a latency check after 1 sec.
        timer = new Timer();
        timer.schedule(new PeriodicLatencyCheck(), 1000);
    }
    
    void HandleLogoutOpcode(/* Packet packet */) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_LOGOUT\n");
        
        SendPacket(new Packet(SMSG_LOGOUT_COMPLETE));
        
        Logout();
    }
    
    void HandleStatusChangedOpcode(Packet packet) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_STATUS_CHANGED\n");
        
        int toStatus = (int)packet.get();
        
        c.setStatus(toStatus);
        
        System.out.printf("Client %d change status to %d.\n" , c.getGuid(), toStatus);
        
        ResultSet rs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
        
        while(rs.next())
        {
            Client target = Main.clientList.findClient(rs.getInt(1));
        
            if (target != null)
                target.getSession().SendStatusChanged(c.getGuid(), c.getStatus());
        }
        
        System.out.printf("Client %d update status to %d: Finish.\n", c.getGuid(), toStatus);
    }
    
    void HandleAddContactOpcode(Packet packet) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_ADD_CONTACT\n");
        
        String username = (String)packet.get();
        
        // Contact to add is self
        if (c.getUsername().equalsIgnoreCase(username))
        {
            System.out.printf("Client %d add self to contact list.\n", c.getGuid());
            
            if (!Main.db.query("SELECT id FROM contact WHERE o_guid = %d AND c_guid= %d", c.getGuid(), c.getGuid()).first())
            {
                Packet p = new Packet(SMSG_ADD_CONTACT_SUCCESS);
                p.put(c.getGuid());
                p.put(c.getUsername());
                p.put(c.getTitle());
                p.put(c.getPSM());
                p.put(c.getStatus());
                
                SendPacket(p);
                
                Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), c.getGuid());
            }
            
            return;
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
                SendPacket(new Packet(SMSG_CONTACT_ALREADY_IN_LIST));
            else
            {
                System.out.printf("Send Contact: %s to client %d\n", username, c.getGuid());
                
                Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), guid);
                
                Client target = Main.clientList.findClient(guid);
                
                int currentStatus = 3;
                
                ResultSet ccrs = Main.db.query("SELECT id FROM contact WHERE o_guid = %d and c_guid = %d", guid, c.getGuid());
                
                if (!ccrs.first())
                {
                    if (target != null)
                    {
                        System.out.printf("Send Contact Request: %s to client %d\n", c.getUsername(), guid);
                        
                        Packet p = new Packet(SMSG_CONTACT_REQUEST);
                        p.put(c.getGuid());
                        p.put(c.getUsername());
                        
                        target.getSession().SendPacket(p);
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
                        
                        target.getSession().SendStatusChanged(c.getGuid(), c.getStatus());
                    }
                }
                
                Packet p = new Packet(SMSG_ADD_CONTACT_SUCCESS);
                p.put(guid);
                p.put(username);
                p.put(title);
                p.put(psm);
                p.put(currentStatus);
                
                SendPacket(p);
            }
        }
        else
        {
            SendPacket(new Packet(SMSG_CONTACT_NOT_FOUND));
        }
    }
    
    void HandleContactAcceptOpcode(Packet packet) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_CONTACT_ACCEPT\n");
        
        int guid = (int)packet.get();
        
        Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), guid);
        Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), guid);
        
        ResultSet rrs = Main.db.query("SELECT username, title, psm FROM account WHERE guid = %d", guid);
        
        Client requestor = Main.clientList.findClient(guid);
        
        int requestorStatus = requestor != null ? requestor.getStatus() : 3;
        
        if (rrs.first())
        {
            System.out.printf("Send Contact: %s to client %d\n", rrs.getString(1), c.getGuid());
            Packet p = new Packet(SMSG_ADD_CONTACT_SUCCESS);
            p.put(guid);
            p.put(rrs.getString(1));
            p.put(rrs.getString(2));
            p.put(rrs.getString(3));
            p.put(requestorStatus);
            
            SendPacket(p);
        }
        
        if (requestor != null)
            requestor.getSession().SendStatusChanged(c.getGuid(), c.getStatus());
    }
    
    void HandleContactDeclineOpcode(Packet packet) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_CONTACT_DECLINE\n");
        
        int guid = (int)packet.get();
        
        Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), guid);
    }
    
    void HandleRemoveContactOpcode(Packet packet) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_REMOVE_CONTACT\n");
        
        int guid = (int)packet.get();
        
        Main.db.execute("DELETE FROM contact WHERE o_guid = %d AND c_guid = %d", c.getGuid(), guid);
        
        Client target = Main.clientList.findClient(guid);
        
        if (target != null)
            target.getSession().SendStatusChanged(c.getGuid(), 3);
    }
    
    void HandleChatMessageOpcode(Packet packet) throws Exception
    {
        System.out.printf("\nOpcode: CMSG_SEND_CHAT_MESSAGE\n");
                        
        int from = c.getGuid();
        int to = (int)packet.get();
        String message = (String)packet.get();
                        
        System.out.printf("Chat Message Receive From: %d, To %d, Message: %s\n", from, to, message);
        
        Client target = Main.clientList.findClient(to);
        
        if (target != null)
        {
            Packet p = new Packet(SMSG_SEND_CHAT_MESSAGE);
            p.put(from);
            p.put(message);
            
            target.getSession().SendPacket(p);
            
            System.out.printf("Send message success\n");
        }
        else
            System.out.printf("Send Chat Message Cancel: Client %d is currently offline.\n", to);
    }
    
    void HandleTimeSyncRespOpcode(Packet packet)
    {
        System.out.printf("\nOpcode: CMSG_TIME_SYNC_RESP\n");
        
        int counter = (int)packet.get();
        long ticks = (long)packet.get();
        
        System.out.printf("From Client: %s (guid: %d)\n", c.getUsername(), c.getGuid());
        
        // first time receive this opcode
        if (counter == 0)
        {
            c.setCounter(counter);
            c.setTicks(ticks);
            System.out.printf("First time sync received: counter %d, client ticks %d\n", counter, ticks);
            return;
        }
        
        if (counter != c.getCounter() + 1)
            System.out.printf("Wrong time sync counter: should be %d, but receive %d\n", c.getCounter() + 1, counter);
        
        System.out.printf("Time sync received: counter %d, client ticks %d, time since last sync %d\n", counter, ticks, ticks - c.getTicks());
        
        c.setCounter(counter);
        c.setTicks(ticks);
    }
    
    void HandlePingOpcode(/* Packet packet */)
    {
        System.out.printf("\nOpcode: CMSG_PING\n");
        int latency = (int)(System.currentTimeMillis() - pingTicks);
        
        System.out.printf("From client: %s (guid: %d), latency: %dms\n", c.getUsername(), c.getGuid(), latency);
        
        c.setLatency(latency);
        
        // Check latency 30 sec later.
        timer.schedule(new PeriodicLatencyCheck(), 30 * 1000);
    }
    
    void SendStatusChanged(int guid, int status) throws Exception
    {
        System.out.printf("Send status change From: %d, To: %d, Status: %d\n", guid, c.getGuid(), status);
        
        Packet p = new Packet(SMSG_STATUS_CHANGED);
        p.put(guid);
        p.put(status);
        
        SendPacket(p);
    }
    
    void Logout()
    {
        try
        {
            Main.clientList.remove(c);
            
            System.out.printf("Closing client socket %d.\n", c.getGuid());
            c.getSocket().close();
            
            Main.db.execute("UPDATE account SET online = 0 WHERE guid = %d", c.getGuid());
            System.out.printf("Stopping session thread %d.\n", c.getGuid());
            
            ResultSet rs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
            
            while(rs.next())
            {
                Client target = Main.clientList.findClient(rs.getInt(1));
            
                if (target != null)
                    target.getSession().SendStatusChanged(c.getGuid(), 3);
            }
            
            stop();
        }
        catch (Exception e){}
    }
    
    class PeriodicLatencyCheck extends TimerTask 
    {
        public void run()
        {
            try
            {
                pingTicks = System.currentTimeMillis();
                SendPacket(new Packet(SMSG_PING));
            }
            catch (Exception e){}
        }
    }
}
