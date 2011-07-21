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

import java.util.ListIterator;

public class Room
{
    private int roomID;
    private String roomName;
    private String roomPassword;
    private ClientList clientList;
    
    public Room(String roomName, String roomPassword)
    {
        this.roomID = Main.roomList.getNextRoomID();
        this.roomName = roomName;
        this.roomPassword = roomPassword;
        
        clientList = new ClientList();
    }
    
    public void setRoomName(String roomName)
    {
        this.roomName = roomName;
    }
    
    public void setRoomPassword(String roomPassword)
    {
        this.roomPassword = roomPassword;
    }
    
    public int getRoomID()
    {
        return this.roomID;
    }
    
    public String getRoomName()
    {
        return this.roomName;
    }
    
    public String getRoomPassword()
    {
        return this.roomPassword;
    }
    
    public int getRoomSize()
    {
        return clientList.size();
    }
    
    public void addClient(Client c)
    {
        clientList.add(c);
    }
    
    public void removeClient(Client c)
    {
        clientList.remove(c);
    }
    
    public Client findClient(String username)
    {
        return clientList.findClient(username);
    }
    
    public Client findClient(int guid)
    {
        return clientList.findClient(guid);
    }
    
    public ListIterator<Client> clientListIterator()
    {
        return clientList.listIterator();
    }
}
