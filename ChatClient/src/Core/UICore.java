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
import UI.MasterUI;
import UI.MasterUI.UISwitcher;

import javax.swing.JOptionPane;

public class UICore
{
    private static MasterUI ui;
    private static ChatUIList chatList;
    private static RoomChatUIList roomChatUIList;
    
    public UICore()
    {
    }
    
    public static void initiate()
    {
        ui = new MasterUI();
        chatList = new ChatUIList();
        roomChatUIList = new RoomChatUIList();
    }
    
    public static MasterUI getMasterUI()
    {
        return ui;
    }
    
    public static ChatUIList getChatUIList()
    {
        return chatList;
    }
    
    public static RoomChatUIList getRoomChatUIList()
    {
        return roomChatUIList;
    }
    
    public static void UpdateContactStatus(int guid, int status)
    {
        Contact c = getMasterUI().searchContact(guid);
        
        if (c != null)
        {
            UICore.getMasterUI().UpdateContactStatus(guid, status);
            
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
