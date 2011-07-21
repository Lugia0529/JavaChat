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

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.util.ListIterator;
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
        this.session = Thread.currentThread();
        
        while(session == Thread.currentThread())
        {
            try
            {
                Packet p = ReceivePacket();
                
                if (p.getOpcode() < 0x00 || p.getOpcode() >= opcodeTable.length)
                {
                    System.out.printf("\nUnknown Opcode Receive: 0x%02X\n", p.getOpcode());
                    continue;
                }
                
                OpcodeDetail opcode = opcodeTable[p.getOpcode()];
                
                System.out.printf("\nOpcode: %s\n", opcode.name);
                
                if (opcode.sessionStatus != SessionStatus.LOGGEDIN)
                {
                    System.out.printf("Invalid Opcode Receive: %s\n", opcode.name);
                    continue;
                }
                
                if (p.size() != opcode.length)
                {
                    System.out.printf("Client %s (guid: %d) send a packet with wrong size, should be %d, but receive %d. (Attemp to crash server?)\n", c.getUsername(), c.getGuid(), opcode.length, p.size());
                    continue;
                }
                
                if (opcode.handler != null)
                {
                    Class[] types = new Class[] { Packet.class };
                    Object[] args = new Object[] { p };
                
                    this.getClass().getDeclaredMethod(opcode.handler, types).invoke(this, args);
                }
                else
                {
                    System.out.printf("Processing is not require for this packet.\n");
                    continue;
                }
            }
            catch (InvocationTargetException ite)
            {
                // Throw when an exception occur while processing packet.
                Throwable t = ite.getCause();
                
                if (t instanceof ClassCastException)
                    System.out.printf("Client %s (guid: %d) send a packet with wrong structure. (Attemp to crash server?)", c.getUsername(), c.getGuid());
                else
                    System.out.printf("Unhandler exception occur while processing packet data.\nException message: %s\n", ite.getCause());
            }
            catch (EOFException eof)
            {
                System.out.printf("\nClient %s (guid: %d) unexpected EOF while waiting for packet. (possible disconnected?)\n", c.getUsername(), c.getGuid());
                
                Logout();
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
            catch (Exception e){e.printStackTrace();}
        }
        
        System.out.printf("Session thread of %s (guid: %d) stopped successfully.\n", c.getUsername(), c.getGuid());
    }
    
    void HandleGetContactListOpcode(Packet packet) throws Exception
    {
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
        
        rs.close();
        
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
        
        requestRS.close();
        
        // We start a latency check after 1 sec.
        timer = new Timer();
        timer.schedule(new PeriodicLatencyCheck(), 1000);
    }
    
    void HandleLogoutOpcode(Packet packet) throws Exception
    {
        SendPacket(new Packet(SMSG_LOGOUT_COMPLETE));
        
        Logout();
    }
    
    void HandleStatusChangedOpcode(Packet packet) throws Exception
    {
        int toStatus = (Integer)packet.get();
        
        c.setStatus(toStatus);
        
        System.out.printf("Client %d change status to %d.\n" , c.getGuid(), toStatus);
        
        InformOthersForStatusChange();
        
        System.out.printf("Client %d update status to %d: Finish.\n", c.getGuid(), toStatus);
    }
    
    void HandleAddContactOpcode(Packet packet) throws Exception
    {
        String username = (String)packet.get();
        
        // Contact to add is self
        if (c.getUsername().equalsIgnoreCase(username))
        {
            System.out.printf("Client %d add self to contact list.\n", c.getGuid());
            
            ResultSet rs = Main.db.query("SELECT id FROM contact WHERE o_guid = %d AND c_guid= %d", c.getGuid(), c.getGuid());
            
            if (rs.first())
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
            
            rs.close();
            
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
                        
                        Packet statusPacket = new Packet(SMSG_STATUS_CHANGED);
                        statusPacket.put(c.getGuid());
                        statusPacket.put(c.getStatus());
                        
                        target.getSession().SendPacket(statusPacket);
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
            
            acrs.close();
        }
        else
        {
            SendPacket(new Packet(SMSG_CONTACT_NOT_FOUND));
        }
        
        ars.close();
    }
    
    void HandleContactAcceptOpcode(Packet packet) throws Exception
    {
        int guid = (Integer)packet.get();
        
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
        {
            Packet statusPacket = new Packet(SMSG_STATUS_CHANGED);
            statusPacket.put(c.getGuid());
            statusPacket.put(c.getStatus());
            
            requestor.getSession().SendPacket(statusPacket);
        }
        
        rrs.close();
    }
    
    void HandleContactDeclineOpcode(Packet packet) throws Exception
    {
        int guid = (Integer)packet.get();
        
        Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), guid);
    }
    
    void HandleRemoveContactOpcode(Packet packet) throws Exception
    {
        int guid = (Integer)packet.get();
        
        Main.db.execute("DELETE FROM contact WHERE o_guid = %d AND c_guid = %d", c.getGuid(), guid);
        
        Client target = Main.clientList.findClient(guid);
        
        if (target != null)
        {
            Packet p = new Packet(SMSG_STATUS_CHANGED);
            p.put(c.getGuid());
            p.put(3);
            
            target.getSession().SendPacket(p);
        }
    }
    
    void HandleChatMessageOpcode(Packet packet) throws Exception
    {
        int from = c.getGuid();
        int to = (Integer)packet.get();
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
        int counter = (Integer)packet.get();
        long ticks = (Long)packet.get();
        
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
    
    void HandlePingOpcode(Packet packet)
    {
        int latency = (int)(System.currentTimeMillis() - pingTicks);
        
        System.out.printf("From client: %s (guid: %d), latency: %dms\n", c.getUsername(), c.getGuid(), latency);
        
        c.setLatency(latency);
        
        // Check latency 30 sec later.
        timer.schedule(new PeriodicLatencyCheck(), 30 * 1000);
    }
    
    void HandleClientDetailChangedOpcode(Packet packet) throws Exception
    {
        String data = (String)packet.get();
        String str = null;
        Packet p = null;
        
        if (packet.getOpcode() == CMSG_TITLE_CHANGED)
        {
            str = "title";
            p = new Packet(SMSG_TITLE_CHANGED);
            c.setTitle(data);
        }
        else if (packet.getOpcode() == CMSG_PSM_CHANGED)
        {
            str = "psm";
            p = new Packet(SMSG_PSM_CHANGED);
            c.setPSM(data);
        }
        else
        {
            System.out.printf("Opcode 0x%02X shouldn't be process in this handler!", packet.getOpcode());
            return;
        }
        
        System.out.printf("Client %d change %s to %s.\n", c.getGuid(), str, data);
        Main.db.execute("UPDATE account SET %s = '%s' WHERE guid = '%d'", str, data, c.getGuid());
        
        p.put(c.getGuid());
        p.put(data);
        
        ResultSet rs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
        
        while(rs.next())
        {
            int guid = rs.getInt(1);
            
            Client target = Main.clientList.findClient(guid);
            
            if (target != null)
            {
                System.out.printf("Send %s change From: %d, To: %d, Data: %s\n", str, c.getGuid(), guid, data);
                target.getSession().SendPacket(p);
            }
        }
    }
    
    void HandleCreateRoomOpcode(Packet packet) throws Exception
    {
        String roomName = (String)packet.get();
        String roomPassword = (String)packet.get();
        
        System.out.printf("From Client: %s (guid: %d)\n", c.getUsername(), c.getGuid());
        System.out.printf("Room name: %s, password: %s\n", roomName, roomPassword.equals("") ? "*NONE*" : roomPassword);
        
        if (Main.roomList.findRoom(roomName) != null)
        {
            System.out.printf("Failed to create room %s, a room with same name is already create.\n", roomName);
            SendPacket(new Packet(SMSG_CREATE_ROOM_FAILED));
            return;
        }
        
        System.out.printf("Creating room %s.\n", roomName);
        Room r = new Room(roomName, roomPassword);
        
        System.out.printf("Register client %d into room %d.\n", c.getGuid(), r.getRoomID());
        r.addClient(c);
        
        Main.roomList.add(r);
        
        Packet p = new Packet(SMSG_JOIN_ROOM_SUCCESS);
        p.put(r.getRoomID());
        p.put(r.getRoomName());
        
        SendPacket(p);
        
        System.out.printf("Room %d created successfully.\n", r.getRoomID());
    }
    
    void HandleJoinRoomOpcode(Packet packet) throws Exception
    {
        String roomName = (String)packet.get();
        String roomPassword = (String)packet.get();
        
        System.out.printf("From Client: %s (guid: %d)\n", c.getUsername(), c.getGuid());
        System.out.printf("Room name: %s, password: %s\n", roomName, roomPassword.equals("") ? "*NONE*" : roomPassword);
        
        Room room = Main.roomList.findRoom(roomName);
        
        if (room == null)
        {
            System.out.printf("Room with name %s is not found!\n", roomName);
            
            Packet p = new Packet(SMSG_ROOM_NOT_FOUND);
            p.put(roomName);
            
            SendPacket(p);
            
            return;
        }
        
        if (!room.getRoomPassword().equals(roomPassword))
        {
            System.out.printf("Client %d supplied a wrong password for room %d.\n", c.getGuid(), room.getRoomID());
            
            Packet p = new Packet(SMSG_WRONG_ROOM_PASSWORD);
            p.put(roomName);
            
            SendPacket(p);
            
            return;
        }
        
        if (room.findClient(c.getGuid()) != null)
        {
            System.out.printf("Client %d is already in room %s.\n", c.getGuid(), roomName);
            
            Packet p = new Packet(SMSG_ALREADY_IN_ROOM);
            p.put(roomName);
            
            SendPacket(p);
            
            return;
        }
        
        Packet joinPacket = new Packet(SMSG_JOIN_ROOM_SUCCESS);
        joinPacket.put(room.getRoomID());
        joinPacket.put(room.getRoomName());
        
        SendPacket(joinPacket);
        
        Packet p = new Packet(SMSG_JOIN_ROOM);
        p.put(room.getRoomID());
        p.put(c.getUsername());
        
        for (ListIterator<Client> client = room.clientListIterator(); client.hasNext(); )
        {
            Client member = client.next();
            
            Packet memberPacket = new Packet(SMSG_ROOM_MEMBER_DETAIL);
            memberPacket.put(room.getRoomID());
            memberPacket.put(member.getUsername());
            
            member.getSession().SendPacket(p);
            SendPacket(memberPacket);
        }
        
        System.out.printf("Register client %d into room %d.\n", c.getGuid(), room.getRoomID());
        room.addClient(c);
        
        System.out.printf("Client %d join room %d successfully.\n", c.getGuid(), room.getRoomID());
    }
    
    void HandleLeaveRoomOpcode(Packet packet) throws Exception
    {
        int roomID = (Integer)packet.get();
        
        System.out.printf("From Client: %s (guid: %d)\n", c.getUsername(), c.getGuid());
        System.out.printf("Room ID: %d.\n", roomID);
        
        Room room = Main.roomList.findRoom(roomID);
        
        if (room == null)
        {
            System.out.printf("Room with ID %d is not found!\n", roomID);
            return;
        }
        
        if (room.findClient(c.getGuid()) == null)
        {
            // no in room?? Wrong packet??
            System.out.printf("Room %d does not contain client %d. (cheater?)\n", roomID, c.getGuid());
            return;
        }
        
        System.out.printf("Remove client %d from room %d.\n", c.getGuid(), room.getRoomID());
        room.removeClient(c);
        
        Packet p = new Packet(SMSG_LEAVE_ROOM);
        p.put(room.getRoomID());
        p.put(c.getUsername());
        
        for (ListIterator<Client> client = room.clientListIterator(); client.hasNext(); )
            client.next().getSession().SendPacket(p);
        
        Packet leavePacket = new Packet(SMSG_LEAVE_ROOM_SUCCESS);
        leavePacket.put(room.getRoomID());
        
        SendPacket(leavePacket);
        
        // Delete room if the room does not contain any client.
        if (room.getRoomSize() == 0)
        {
            System.out.printf("Room %d is empty, remove.\n", room.getRoomID());
            Main.roomList.remove(room);
        }
    }
    
    void HandleRoomChatOpcode(Packet packet) throws Exception
    {
        int roomID = (Integer)packet.get();
        String message = (String)packet.get();
        
        Room room = Main.roomList.findRoom(roomID);
        
        if (room == null)
            return;
        
        if (room.findClient(c.getGuid()) == null)
        {
            // no in room?? Wrong packet??
            System.out.printf("Room %d does not contain client %d. (cheater?)\n", roomID, c.getGuid());
            return;
        }
        
        System.out.printf("Room Chat Message Receive From: %d, Room ID: %d, Message: %s\n", c.getGuid(), roomID, message);
        
        Packet p = new Packet(SMSG_ROOM_CHAT);
        p.put(room.getRoomID());
        p.put(c.getUsername());
        p.put(message);
        
        for (ListIterator<Client> client = room.clientListIterator(); client.hasNext(); )
            client.next().getSession().SendPacket(p);
    }
    
    void InformOthersForStatusChange() throws Exception
    {
        Packet p = new Packet(SMSG_STATUS_CHANGED);
        p.put(c.getGuid());
        p.put(c.getStatus());
        
        ResultSet rs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
        
        while(rs.next())
        {
            Client target = Main.clientList.findClient(rs.getInt(1));
        
            if (target != null)
                target.getSession().SendPacket(p);
        }
        
        rs.close();
    }
    
    void LeaveAllRoom() throws Exception
    {
        /*  TODO: Optimize Required */
        
        for (ListIterator<Room> room = Main.roomList.listIterator(); room.hasNext(); )
        {
            Room r = room.next();
            
            if (r.findClient(c.getGuid()) != null)
            {
                r.removeClient(c);
                
                if (r.getRoomSize() == 0)
                {
                    System.out.printf("Room %d is empty, remove.\n", r.getRoomID());
                    Main.roomList.remove(r);
                }
                
                Packet p = new Packet(SMSG_LEAVE_ROOM);
                p.put(r.getRoomID());
                p.put(c.getUsername());
                
                for (ListIterator<Client> client = r.clientListIterator(); client.hasNext(); )
                    client.next().getSession().SendPacket(p);
            }
        }
    }
    
    void Logout()
    {
        try
        {
            Main.clientList.remove(c);
            
            System.out.printf("Closing client socket %d.\n", c.getGuid());
            c.getSocket().close();
            
            Main.db.execute("UPDATE account SET online = 0 WHERE guid = %d", c.getGuid());
            System.out.printf("Stopping session thread of %s (guid: %d).\n", c.getUsername(), c.getGuid());
            
            c.setStatus(3);
            
            InformOthersForStatusChange();
            LeaveAllRoom();
            
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
