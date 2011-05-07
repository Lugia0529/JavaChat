package Core;

import UI.ChatUI;
import UI.ContactRequestUI;

import javax.swing.JOptionPane;

public class NetworkThread implements Runnable, Opcode
{
    private static volatile Thread thread;
    
    public static void stop()
    {
        thread = null;
    }
    
    public void run()
    {
        thread = Thread.currentThread();
        
        NetworkManager.getContactList();
        
        try
        {
            byte b;
            
            // The server will first send SMSG_CONTACT_DETAIL signal to inform client that this is a client detail data.
            while((b = NetworkManager.readByte()) == SMSG_CONTACT_DETAIL)
            {
                int guid = NetworkManager.readInt();
                String c_username = String.format("%s", NetworkManager.readObject());
                String c_title = String.format("%s", NetworkManager.readObject());
                String c_psm = String.format("%s", NetworkManager.readObject());
                int c_status = NetworkManager.readInt();
                
                Contact c = new Contact(guid, c_username, c_title, c_psm, c_status);
                
                UIManager.getMasterUI().addContact(c);
            }
            
            // The server will send SMSG_CONTACT_LIST_ENDED signal to inform client that all client data is sent.
            // If the client receive signal other than SMSG_CONTACT_LIST_ENDED, the client may miss some contact data while receiving.
            if (b != SMSG_CONTACT_LIST_ENDED)
                UIManager.showMessageDialog("Fail to load contact list, your contact list may incomplete.", "Error", JOptionPane.WARNING_MESSAGE);
            
            // Tell the server the current status of client. Will be useful in login as this status when it is implemented.
            NetworkManager.writeByte(CMSG_STATUS_CHANGED);
            NetworkManager.writeInt(0);
            NetworkManager.flush();
            
            while(thread == Thread.currentThread())
            {
                b = NetworkManager.readByte();
                
                switch (b)
                {
                    case SMSG_SEND_CHAT_MESSAGE:
                        HandleChatMessageOpcode();
                        break;
                    case SMSG_STATUS_CHANGED:
                        HandleStatusChangedOpcode();
                        break;
                    case SMSG_CONTACT_ALREADY_IN_LIST:
                        UIManager.showMessageDialog("The contact is already in list.", "Add Contact", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case SMSG_CONTACT_NOT_FOUND:
                        UIManager.showMessageDialog("No such user found.", "Add Contact", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case SMSG_ADD_CONTACT_SUCCESS:
                        HandleAddContactSuccessOpcode();
                        break;
                    case SMSG_CONTACT_REQUEST:
                        HandleContactRequestOpcode();
                        break;
                }
            }
        }
        catch(Exception e) {}
    }
    
    void HandleChatMessageOpcode()
    {
        int senderGuid = NetworkManager.readInt();
        String message = String.format("%s", NetworkManager.readObject());
        Contact s_contact = null;
        
        // Search contact list have this contact detail or not.
        // This help the client to deny chat message if the contact is deleted.
        s_contact = UIManager.getMasterUI().searchContact(senderGuid);
        
        // Cant find sender contact detail in list. Possible deleted.
        if (s_contact == null)
            return;
        
        ChatUI targetUI = UIManager.getChatUIList().findUI(s_contact);
        
        if (targetUI == null)
            UIManager.getChatUIList().add(targetUI = new ChatUI(s_contact, UIManager.getMasterUI().getAccountTitle()));
        
        // Output the message in sender ChatUI.
        targetUI.append(s_contact.getTitle(), message);
        targetUI.toFront();
    }
    
    void HandleStatusChangedOpcode()
    {
        int guid = NetworkManager.readInt();
        int status = NetworkManager.readInt();
        
        UIManager.UpdateContactStatus(guid, status);
    }
    
    void HandleAddContactSuccessOpcode()
    {
        int guid = NetworkManager.readInt();
        String username = String.format("%s", NetworkManager.readObject());
        String title = String.format("%s", NetworkManager.readObject());
        String psm = String.format("%s", NetworkManager.readObject());
        int c_status = NetworkManager.readInt();
       
        Contact c = new Contact(guid, username, title, psm, c_status);
       
        UIManager.getMasterUI().addContact(c);
    }

    void HandleContactRequestOpcode()
    {
        int r_guid = NetworkManager.readInt();
        String r_username = String.format("%s", NetworkManager.readObject());
        
        new ContactRequestUI(r_guid, r_username);
    }
}
