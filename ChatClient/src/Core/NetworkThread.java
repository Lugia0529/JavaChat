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

import UI.ChatUI;
import UI.ContactRequestUI;
import UI.RoomChatUI;
import UI.RoomFormUI;

import java.io.EOFException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

public class NetworkThread implements Runnable, Opcode
{
    private static ArrayList<Packet> PacketStorage;
    private static volatile Thread thread;
    private static Timer timer;
    private static SessionStatus sessionStatus;
    private int counter;
    
    public static void stop()
    {
        timer.cancel();
        
        thread = null;
        timer = null;
    }
    
    public void run()
    {
        thread = Thread.currentThread();
        
        sessionStatus = SessionStatus.LOGGEDIN;
        
        PacketStorage = new ArrayList<Packet>();
        
        // The server will first send SMSG_CONTACT_DETAIL signal to inform client that this is a client detail data.
        NetworkManager.getContactList();
        
        timer = new Timer();
        timer.scheduleAtFixedRate(new PeriodicTimeSyncResp(), 0, 10 * 1000);
        
        Packet p;
        
        while(thread == Thread.currentThread())
        {
            try
            {
                p = NetworkManager.ReceivePacket();
                
                if (p.getOpcode() < 0x00 || p.getOpcode() >= opcodeTable.length)
                    continue;
                
                OpcodeDetail opcode = opcodeTable[p.getOpcode()];
                
                if (p.size() != opcode.length)
                    continue;
                
                if (opcode.sessionStatus == sessionStatus.INSTANT)
                {
                    if (p.getOpcode() == SMSG_PING)
                    {
                        NetworkManager.SendPacket(new Packet(CMSG_PING));
                        continue;
                    }
                }
                
                if (!IsOpcodeCanProcessNow(opcode))
                {
                    PacketStorage.add(p);
                    continue;
                }
                
                ProcessPacket(p);
            }
            catch (EOFException eof)
            {
                NetworkManager.logout();

                UICore.showMessageDialog("You have been disconnected from the server.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (SocketException se)
            {
                NetworkManager.logout();

                UICore.showMessageDialog("You have been disconnected from the server.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (SocketTimeoutException ste)
            {
                NetworkManager.logout();

                UICore.showMessageDialog("You have been disconnected from the server.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (Exception e) {}
        }
    }
    
    boolean IsOpcodeCanProcessNow(OpcodeDetail detail)
    {
        if (sessionStatus == detail.sessionStatus || sessionStatus == SessionStatus.READY || detail.sessionStatus == SessionStatus.INSTANT)
            return true;
        
        return false;
    }
    
    void ProcessQueuePacket() throws Exception
    {
        for (ListIterator<Packet> packet = PacketStorage.listIterator(); packet.hasNext(); )
        {
            Packet p = packet.next();
            
            OpcodeDetail opcode = opcodeTable[p.getOpcode()];
            
            if (!IsOpcodeCanProcessNow(opcode))
                continue;
            
            ProcessPacket(p);
            packet.remove();
        }
    }
    
    void ProcessPacket(Packet p) throws Exception
    {
        OpcodeDetail opcode = opcodeTable[p.getOpcode()];
        
        if (opcode.handler != null)
        {
            Class[] types = new Class[] { Packet.class };
            Object[] args = new Object[] { p };
        
            this.getClass().getDeclaredMethod(opcode.handler, types).invoke(this, args);
        }
    }
    
    void HandleContactDetailOpcode(Packet packet)
    {
        int guid = (Integer)packet.get();
        String c_username = (String)packet.get();
        String c_title = (String)packet.get();
        String c_psm = (String)packet.get();
        int c_status = (Integer)packet.get();
        
        Contact c = new Contact(guid, c_username, c_title, c_psm, c_status);
        
        UICore.getMasterUI().addContact(c);
    }
    
    void HandleContactListEndedOpcode(Packet packet) throws Exception
    {
        sessionStatus = SessionStatus.READY;
        ProcessQueuePacket();
    }
    
    void HandleContactAlreadyInListOpcode(Packet packet)
    {
        UICore.showMessageDialog("The contact is already in list.", "Add Contact", JOptionPane.INFORMATION_MESSAGE);
    }
    
    void HandleContactNotFoundOpcode(Packet packet)
    {
        UICore.showMessageDialog("No such user found.", "Add Contact", JOptionPane.INFORMATION_MESSAGE);
    }
    
    void HandleChatMessageOpcode(Packet packet)
    {
        int senderGuid = (Integer)packet.get();
        String message = (String)packet.get();
        
        Contact s_contact = null;
        
        // Search contact list have this contact detail or not.
        // This help the client to deny chat message if the contact is deleted.
        s_contact = UICore.getMasterUI().searchContact(senderGuid);
        
        // Cant find sender contact detail in list. Possible deleted.
        if (s_contact == null)
            return;
        
        ChatUI targetUI = UICore.getChatUIList().findUI(s_contact);
        
        if (targetUI == null)
            UICore.getChatUIList().add(targetUI = new ChatUI(s_contact));
        
        // Output the message in sender ChatUI.
        targetUI.append(s_contact.getTitle(), message);
        targetUI.toFront();
    }
    
    void HandleContactStatusChangedOpcode(Packet packet)
    {
        int guid = (Integer)packet.get();
        int status = (Integer)packet.get();
        
        UICore.UpdateContactStatus(guid, status);
    }
    
    void HandleAddContactSuccessOpcode(Packet packet)
    {
        int guid = (Integer)packet.get();
        String username = (String)packet.get();
        String title = (String)packet.get();
        String psm = (String)packet.get();
        int c_status = (Integer)packet.get();
       
        Contact c = new Contact(guid, username, title, psm, c_status);
       
        UICore.getMasterUI().addContact(c);
    }

    void HandleContactRequestOpcode(Packet packet)
    {
        int r_guid = (Integer)packet.get();
        String r_username = (String)packet.get();
        
        new ContactRequestUI(r_guid, r_username);
    }
    
    void HandleContactDetailChangedOpcode(Packet packet)
    {
        int guid = (Integer)packet.get();
        String data = (String)packet.get();
        
        if (packet.getOpcode() == SMSG_TITLE_CHANGED)
            UICore.getMasterUI().UpdateContactDetail(guid, data, null);
        else if (packet.getOpcode() == SMSG_PSM_CHANGED)
            UICore.getMasterUI().UpdateContactDetail(guid, null, data);
    }
    
    void HandleCreateRoomFailOpcode(Packet packet)
    {
        UICore.showMessageDialog("Fail to create room, a room with same name is already exists.", "Create Room", JOptionPane.INFORMATION_MESSAGE);
    }
    
    void HandleJoinRoomOpcode(Packet packet)
    {
        int roomID = (Integer)packet.get();
        String userName = (String)packet.get();
        
        RoomChatUI ui = UICore.getRoomChatUIList().findUI(roomID);
        
        if (ui != null)
            ui.addMember(userName, true);
    }
    
    void HandleLeaveRoomOpcode(Packet packet)
    {
        int roomID = (Integer)packet.get();
        String userName = (String)packet.get();
        
        RoomChatUI ui = UICore.getRoomChatUIList().findUI(roomID);
        
        if (ui != null)
            ui.removeMember(userName, true);
    }
    
    void HandleJoinRoomSuccessOpcode(Packet packet)
    {
        int roomID = (Integer)packet.get();
        String roomName = (String)packet.get();
        
        Room r = new Room(roomID, roomName);
        RoomChatUI ui = new RoomChatUI(r, "abc");
        
        UICore.getRoomChatUIList().add(ui);
    }
    
    void HandleLeaveRoomSuccessOpcode(Packet packet)
    {
        int roomID = (Integer)packet.get();
        
        RoomChatUI ui = UICore.getRoomChatUIList().findUI(roomID);
        
        if (ui != null)
        {
            ui.dispose();
            UICore.getRoomChatUIList().remove(ui);
        }
    }
    
    void HandleRoomChatOpcode(Packet packet)
    {
        int roomID = (Integer)packet.get();
        String sender = (String)packet.get();
        String message = (String)packet.get();
        
        RoomChatUI ui = UICore.getRoomChatUIList().findUI(roomID);
        
        if (ui != null)
            ui.append(sender, message);
    }
    
    void HandleRoomNotFoundOpcode(Packet packet)
    {
        String roomName = (String)packet.get();
        
        UICore.showMessageDialog(String.format("Could not found a room with name %s!", roomName), "Join An Existing Room", JOptionPane.INFORMATION_MESSAGE);
        
        new RoomFormUI(RoomFormUI.JOIN_ROOM, roomName);
    }
    
    void HandleWrongRoomPasswordOpcode(Packet packet)
    {
        String roomName = (String)packet.get();
        
        UICore.showMessageDialog(String.format("Wrong password for room %s!", roomName), "Join An Existing Room", JOptionPane.INFORMATION_MESSAGE);
        
        new RoomFormUI(RoomFormUI.JOIN_ROOM, roomName);
    }
    
    void HandleRoomMemberDetailOpcode(Packet packet)
    {
        int roomID = (Integer)packet.get();
        String userName = (String)packet.get();
        
        RoomChatUI ui = UICore.getRoomChatUIList().findUI(roomID);
        
        if (ui != null)
            ui.addMember(userName, false);
    }
    
    void HandleAlreadyInRoomOpcode(Packet packet)
    {
        String roomName = (String)packet.get();
        
        UICore.showMessageDialog(String.format("You are already a member of room %s!", roomName), "Join An Existing Room", JOptionPane.INFORMATION_MESSAGE);
    }
    
    void HandleLogoutCompleteOpcode(Packet packet)
    {
        NetworkManager.logout();
    }
    
    class PeriodicTimeSyncResp extends TimerTask 
    {
        public PeriodicTimeSyncResp()
        {
            counter = 0;
        }
        
        public void run() 
        {
            Packet p = new Packet(CMSG_TIME_SYNC_RESP);
            p.put(counter++);
            p.put(System.currentTimeMillis());
            
            NetworkManager.SendPacket(p);
        }
    }
}
