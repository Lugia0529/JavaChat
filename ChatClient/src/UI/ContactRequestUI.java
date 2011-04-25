package UI;

import javax.swing.*;
import java.awt.event.*;

import Core.*;

public class ContactRequestUI extends JFrame implements Opcode, ActionListener
{
    ButtonGroup grpSelection;
    
    JLabel lblContent;
    JLabel lblSelection;
    
    JButton btnOk;
    JButton btnLater;
    
    JRadioButton rbAllow;
    JRadioButton rbBlock;
    
    int guid;
    
    public ContactRequestUI(int guid, String username)
    {
        this.guid = guid;
        
        setTitle("Add New Contact");
        setLayout(null);
        
        lblContent = new JLabel(String.format("%s has added you to his/her contact list.", username));
        lblSelection = new JLabel("Do you want to:");
        
        rbAllow = new JRadioButton("Allow this person to see you when you are online and contact you.");
        rbBlock = new JRadioButton("Block this person from seeing you when you are online and contacting you.");
        
        btnOk = new JButton("Ok");
        btnLater = new JButton("Decide Later...");
        
        grpSelection = new ButtonGroup();
        grpSelection.add(rbAllow);
        grpSelection.add(rbBlock);
        
        lblContent.setBounds(10, 10, 475, 25);
        lblSelection.setBounds(10, 40, 475, 25);
        rbAllow.setBounds(15, 65, 475, 20);
        rbBlock.setBounds(15, 85, 475, 20);
        btnOk.setBounds(270, 130, 80, 25);
        btnLater.setBounds(360, 130, 120, 25);
        
        add(lblContent);
        add(lblSelection);
        add(rbAllow);
        add(rbBlock);
        add(btnOk);
        add(btnLater);
        
        rbAllow.setSelected(true);
        
        btnOk.addActionListener(this);
        btnLater.addActionListener(this);
        
        setSize(500, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(btnOk))
        {
            Main.m_session.writeByte(rbAllow.isSelected() ?  CMSG_CONTACT_ACCEPT : CMSG_CONTACT_DECLINE);
            Main.m_session.writeInt(guid);
            Main.m_session.flush(); 
        }
        
        dispose();
    }
}
