package UI;

import javax.swing.*;
import java.awt.event.*;
import java.util.ListIterator;
import java.util.Vector;

import Core.*;

public class FriendListUI extends JFrame implements Runnable, Opcode
{
    JComboBox cStatus;
    JList friendList;
    JScrollPane friendListPane;
    
    JLabel lblName;
    JLabel lblPSM;
    JLabel lblFList;
    
    DefaultListModel model;
    
    String[] status = {"Online", "Away", "Busy", "Appear Offline", "Logout"};
    
    static Vector<ChatUI> chatWindow;
    
    public FriendListUI(String name, String psm, JFrame loginFrame)
    {
        setTitle(String.format("%s - %s", name, psm));
        setLayout(null);
        
        lblName = new JLabel(name);
        lblPSM = new JLabel(psm);
        
        cStatus = new JComboBox(status);
        
        model = new DefaultListModel();
        friendList = new JList(model);
        friendListPane = new JScrollPane(friendList);
        
        add(lblName);
        add(lblPSM);
        add(cStatus);
        add(friendListPane);
        
        lblName.setBounds(15, 10, 245, 25);
        lblPSM.setBounds(15, 35, 245, 25);
        cStatus.setBounds(10, 65, 245, 25);
        friendListPane.setBounds(10, 100, 245, 360);
        
        
        setSize(270, 500);
        setResizable(false);
        setLocationRelativeTo(loginFrame);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        loginFrame.dispose();
        
        chatWindow = new Vector<ChatUI>();
        
        friendList.addMouseListener(mouseListener);
    }
    
    // Direct adding Contact instance, Contact.java toString() method show the correct contact detail instead of instance memory location.
    public void run()
    {
        try
        {
            Main.m_session.writeByte(CMSG_GET_FRIEND_LIST);
            Main.m_session.flush();
            
            byte b;
            
            while((b = Main.m_session.readByte()) == SMSG_FRIEND_DETAIL)
            {
                int guid = Main.m_session.readInt();
                String cUsername = String.format("%s", Main.m_session.readObject());
                String cTitle = String.format("%s", Main.m_session.readObject());
                String cPSM = String.format("%s", Main.m_session.readObject());
                
                Contact c = new Contact(guid, cUsername, cTitle, cPSM);
                
                model.addElement(c);
            }
            
            if (b != SMSG_FRIEND_LIST_ENDED)
                JOptionPane.showMessageDialog(this, "Fail to load contact list, your contact list may incomplete.", "Error", JOptionPane.WARNING_MESSAGE);
        }
        catch(Exception e){}
    }
    
    MouseListener mouseListener = new MouseAdapter()
    {
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2)
            {
                int index = friendList.locationToIndex(e.getPoint());
                Contact c =(Contact)model.getElementAt(index);
                
                for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
                {
                    ChatUI ui = i.next();
                    
                    if (ui.getContact().equals(c))
                    {
                        chatWindow.elementAt(index).toFront();
                        chatWindow.elementAt(index).repaint();
                        return;
                    }
                }
                
                chatWindow.add(new ChatUI(c));
            }
        }
    };
}
