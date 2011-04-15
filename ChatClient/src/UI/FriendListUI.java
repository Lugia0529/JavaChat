package UI;

import javax.swing.*;
import java.awt.event.*;

public class FriendListUI extends JFrame
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
}
