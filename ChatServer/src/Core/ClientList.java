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

public class ClientList
{
    private ArrayList<Client> list;
    
    public ClientList()
    {
        list = new ArrayList<Client>();
    }
    
    public boolean add(Client c)
    {
        return list.add(c);
    }
    
    public boolean remove(Client c)
    {
        return list.remove(c);
    }
    
    public int size()
    {
        return list.size();
    }
    
    public Client findClient(int guid)
    {
        for (ListIterator<Client> c = list.listIterator(); c.hasNext(); )
            if (c.next().getGuid() == guid)
                return c.previous();
        
        return null;
    }
    
    public Client findClient(String username)
    {
        for (ListIterator<Client> c = list.listIterator(); c.hasNext(); )
            if (c.next().getUsername().equalsIgnoreCase(username))
                return c.previous();
        
        return null;
    }
}
