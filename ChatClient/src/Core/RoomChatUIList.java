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

import UI.RoomChatUI;

import java.util.ArrayList;
import java.util.ListIterator;

public class RoomChatUIList
{
    private ArrayList<RoomChatUI> list;
    
    public RoomChatUIList()
    {
        list = new ArrayList<RoomChatUI>();
    }
    
    public boolean add(RoomChatUI ui)
    {
        return list.add(ui);
    }
    
    public boolean remove(RoomChatUI ui)
    {
        ui.dispose();
        return list.remove(ui);
    }
    
    public void disposeAllUI()
    {
        for (ListIterator<RoomChatUI> ui = list.listIterator(); ui.hasNext(); )
            ui.next().dispose();
        
        list = new ArrayList<RoomChatUI>();
    }
    
    public RoomChatUI findUI(int roomID)
    {
        for (ListIterator<RoomChatUI> ui = list.listIterator(); ui.hasNext(); )
            if (ui.next().getRoom().getRoomID() == roomID)
                return ui.previous();
        
        return null;
    }
    
    public int size()
    {
        return list.size();
    }
}
