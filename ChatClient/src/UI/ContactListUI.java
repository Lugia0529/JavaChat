package UI;

import javax.swing.*;
import java.awt.event.*;
import java.util.ListIterator;
import java.util.Vector;

import Core.*;

public class ContactListUI extends JFrame implements Runnable, Opcode
{
    private JComboBox cStatus;
    private JList contactList;
    private JScrollPane contactListPane;
    
    private JLabel lblName;
    private JLabel lblPSM;
    
    private DefaultListModel model;
    
    private String[] status = {"Online", "Away", "Busy", "Appear Offline", "Logout"};
    
    private String accountTitle;
    private String accountPSM;
    
    static Vector<ChatUI> chatWindow;
    
    public ContactListUI(String accountTitle, String accountPSM, JFrame loginFrame)
    {
        setTitle(String.format("%s - %s", accountTitle, accountPSM));
        setLayout(null);
        
        this.accountTitle = accountTitle;
        this.accountPSM = accountPSM;
        
        lblName = new JLabel(accountTitle);
        lblPSM = new JLabel(accountPSM);
        
        cStatus = new JComboBox(status);
        
        model = new DefaultListModel();
        contactList = new JList(model);
        contactListPane = new JScrollPane(contactList);
        
        add(lblName);
        add(lblPSM);
        add(cStatus);
        add(contactListPane);
        
        lblName.setBounds(15, 10, 245, 25);
        lblPSM.setBounds(15, 35, 245, 25);
        cStatus.setBounds(10, 65, 245, 25);
        contactListPane.setBounds(10, 100, 245, 360);
        
        
        setSize(270, 500);
        setResizable(false);
        setLocationRelativeTo(loginFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        loginFrame.dispose();
        
        chatWindow = new Vector<ChatUI>();
        
        addWindowListener(winListener);
        contactList.addMouseListener(mouseListener);
        cStatus.addActionListener(actListener);
    }
    
    public void logout()
    {
        // Tell the server we are logout now.
        Main.m_session.writeByte(CMSG_STATUS_CHANGED);
        Main.m_session.writeInt(3);
        Main.m_session.writeByte(CMSG_LOGOUT);
        Main.m_session.flush();
            
        //close all existing ChatUI
        for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
        {
            ChatUI ui = i.next();
            ui.dispose();
        }
        
        chatWindow = null;
        
        System.exit(0);
    }
    
    // Direct adding Contact instance, Contact.java toString() method show the correct contact detail instead of instance memory location.
    public void run()
    {
        try
        {
            /* TODO: Can we separate socket receive thread to another file? */
            
            // Tell the server that we are ready to get the contact list.
            Main.m_session.writeByte(CMSG_GET_CONTACT_LIST);
            Main.m_session.flush();
            
            byte b;
            
            // The server will first send SMSG_CONTACT_DETAIL signal to inform client that this is a client detail data.
            while((b = Main.m_session.readByte()) == SMSG_CONTACT_DETAIL)
            {
                int guid = Main.m_session.readInt();
                String c_username = String.format("%s", Main.m_session.readObject());
                String c_title = String.format("%s", Main.m_session.readObject());
                String c_psm = String.format("%s", Main.m_session.readObject());
                int c_status = Main.m_session.readInt();
                
                Contact c = new Contact(guid, c_username, c_title, c_psm, c_status);
                
                model.addElement(c);
            }
            
            // The server will send SMSG_CONTACT_LIST_ENDED signal to inform client that all client data is sent.
            // If the client receive signal other than SMSG_CONTACT_LIST_ENDED, the client may miss some contact data while receiving.
            if (b != SMSG_CONTACT_LIST_ENDED)
                JOptionPane.showMessageDialog(this, "Fail to load contact list, your contact list may incomplete.", "Error", JOptionPane.WARNING_MESSAGE);
            
            // Tell the server the current status of client. Will be useful in login as this status when it is implemented.
            Main.m_session.writeByte(CMSG_STATUS_CHANGED);
            Main.m_session.writeInt(0);
            Main.m_session.flush();
            
            while(true)
            {
                switch (Main.m_session.readByte())
                {
                    case SMSG_SEND_CHAT_MESSAGE:
                        int sender = Main.m_session.readInt();
                        String message = String.format("%s", Main.m_session.readObject());
                        Contact s_contact = null;
                        int index;
                        
                        /* TODO: Need optimize */
                        
                        // Search contact list have this contact detail or not.
                        // This help the client to deny chat message if the contact is deleted.
                        for (index = 0; index < model.getSize(); index++)
                        {
                            if (((Contact)model.elementAt(index)).getGuid() == sender)
                            {
                                s_contact = (Contact)model.elementAt(index);
                                break;
                            }
                        }
                        
                        // Cant find sender contact detail in list. Possible deleted.
                        if (s_contact == null)
                            break;
                        
                        ChatUI targetUI = null;
                        
                        // Sender contact detail is founded. Now we search the ChatUI list to see is that ChatUI of sender is opened.
                        for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
                        {
                            ChatUI ui = i.next();
                            
                            if (ui.getContact().equals(s_contact))
                            {
                                targetUI = ui;
                                break;
                            }
                        }
                        
                        // Cant find ChatUI of sender, so we create it.
                        if (targetUI == null)
                        {
                            targetUI = new ChatUI(s_contact, accountTitle);
                            chatWindow.add(targetUI);
                        }
                        
                        // Output the message in sender ChatUI.
                        message = message.replaceAll("\n", "\n     ");
                        targetUI.txtOutput.append(String.format("%s says:\n", s_contact.getTitle()));
                        targetUI.txtOutput.append(String.format("     %s\n", message));
                        targetUI.toFront();
                        break;
                    case SMSG_STATUS_CHANGED:
                        int fromGuid = Main.m_session.readInt();
                        int toStatus = Main.m_session.readInt();
                        
                        /* TODO: Need optimize */
                        
                        // Search contact list have this contact detail or not.
                        // If contact is found, we update it status.
                        for (index = 0; index < model.getSize(); index++)
                        {
                            if (((Contact)model.elementAt(index)).getGuid() == fromGuid)
                            {
                                ((Contact)model.elementAt(index)).setStatus(toStatus);
                                contactList.repaint();
                                break;
                            }
                        }
                        
                        break;
                }
            }
        }
        catch(Exception e){}
    }
    
    ActionListener actListener = new ActionListener() 
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(cStatus))
            {
                switch (cStatus.getSelectedIndex())
                {
                    //Only logout have special handle, status change only inform server
                    case 4:
                        logout();
                        break;
                    default:
                        Main.m_session.writeByte(CMSG_STATUS_CHANGED);
                        Main.m_session.writeInt(cStatus.getSelectedIndex());
                        Main.m_session.flush();
                }
            }
        }
    };
    
    WindowListener winListener = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e) 
        {
            // Inform the server that client is ready to logout when client is close that Contact List.
            logout();
        }
    };
    
    MouseListener mouseListener = new MouseAdapter()
    {
        public void mouseClicked(MouseEvent e)
        {
            // Handle double click event of contact list.
            // Open contact ChatUI when client is double click on contact detail.
            if (e.getClickCount() == 2)
            {
                // Get the contact detail first.
                int index = contactList.locationToIndex(e.getPoint());
                Contact c =(Contact)model.getElementAt(index);
                
                // Search the ChatUI list to see is that ChatUI of contact is opened.
                for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
                {
                    ChatUI ui = i.next();
                    
                    // ChatUI found, pop it to the front of screen. If it is minimize, we restore it.
                    if (ui.getContact().equals(c))
                    {
                        if (ui.getState() == JFrame.ICONIFIED)
                            ui.setState(JFrame.NORMAL);
                        
                        ui.toFront();
                        return;
                    }
                }
                
                // ChatUI of contact not found, we create it.
                chatWindow.add(new ChatUI(c, accountTitle));
            }
        }
    };
}
