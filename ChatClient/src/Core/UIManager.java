package Core;

import UI.ChatUI;
import UI.MasterUI;
import UI.MasterUI.UISwitcher;

import javax.swing.JOptionPane;

public class UIManager
{
    private static MasterUI ui;
    public static ChatUIList chatList;
    
    public UIManager()
    {
    }
    
    public static void initiate()
    {
        ui = new MasterUI();
        chatList = new ChatUIList();
    }
    
    public static MasterUI getMasterUI()
    {
        return ui;
    }
    
    public static ChatUIList getChatUIList()
    {
        return chatList;
    }
    
    public static void UpdateContactStatus(int guid, int status)
    {
        Contact c = getMasterUI().searchContact(guid);
        
        if (c != null)
        {
            UIManager.getMasterUI().UpdateContactStatus(guid, status);
            
            ChatUI chatUI =  getChatUIList().findUI(c);
            
            if (chatUI != null)
                chatUI.UpdateTitle();
        }
        
    }
    
    public static void switchUI()
    {
        new Thread(new UISwitcher()).start();
    }
    
    public static void showMessageDialog(Object message, String title, int messageType)
    {
        JOptionPane.showMessageDialog(ui, message, title, messageType);
    }
}
