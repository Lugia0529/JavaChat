package UI;

import javax.swing.*;
import java.awt.event.*;
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
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(loginFrame);
        setSize(270, 500);
        setResizable(false);
        setVisible(true);
        loginFrame.dispose();
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
}