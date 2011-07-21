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

import java.util.ArrayList;
import java.util.ListIterator;

public class RoomList
{
    private ArrayList<Room> list;
    
    public RoomList()
    {
        list = new ArrayList<Room>();
    }
    
    public boolean add(Room r)
    {
        return list.add(r);
    }
    
    public boolean remove(Room r)
    {
        return list.remove(r);
    }
    
    public int size()
    {
        return list.size();
    }
    
    public int getNextRoomID()
    {
        if (size() == 0)
            return 0;
        
        return list.get(size() - 1).getRoomID() + 1;
    }
    
    public Room findRoom(int roomID)
    {
        for (ListIterator<Room> r = list.listIterator(); r.hasNext(); )
            if (r.next().getRoomID() == roomID)
                return r.previous();
        
        return null;
    }
    
    public Room findRoom(String roomName)
    {
        for (ListIterator<Room> r = list.listIterator(); r.hasNext(); )
            if (r.next().getRoomName().equalsIgnoreCase(roomName))
                return r.previous();
        
        return null;
    }
    
    public ListIterator<Room> listIterator()
    {
        return list.listIterator();
    }
}
