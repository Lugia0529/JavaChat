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

public class Room
{
    private int roomID;
    private String roomName;
    
    public Room(int roomID, String roomName)
    {
        this.roomID = roomID;
        this.roomName = roomName;
    }
    
    public void setRoomName(String roomName)
    {
        this.roomName = roomName;
    }
    
    public int getRoomID()
    {
        return this.roomID;
    }
    
    public String getRoomName()
    {
        return this.roomName;
    }
}
