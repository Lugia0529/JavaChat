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
