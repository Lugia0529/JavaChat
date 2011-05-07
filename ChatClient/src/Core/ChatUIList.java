package Core;

import UI.ChatUI;

import java.util.ArrayList;
import java.util.ListIterator;

public class ChatUIList
{
    private ArrayList<ChatUI> list;
    
    public ChatUIList()
    {
        list = new ArrayList<ChatUI>();
    }
    
    public boolean add(ChatUI ui)
    {
        return list.add(ui);
    }
    
    public boolean remove(ChatUI ui)
    {
        ui.dispose();
        return list.remove(ui);
    }
    
    public void disposeAllUI()
    {
        for (ListIterator<ChatUI> ui = list.listIterator(); ui.hasNext(); )
            ui.next().dispose();
        
        list = new ArrayList<ChatUI>();
    }
    
    public ChatUI findUI(Contact c)
    {
        for (ListIterator<ChatUI> ui = list.listIterator(); ui.hasNext(); )
            if (ui.next().getContact().equals(c))
                return ui.previous();
        
        return null;
    }
    
    public int size()
    {
        return list.size();
    }
}
