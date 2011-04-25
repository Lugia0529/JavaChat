package UI;

import javax.swing.*;
import java.awt.event.*;

import Core.*;
import java.awt.Color;

public class AddContactUI extends JFrame implements Opcode, ActionListener
{
    JLabel lblUsername;
    
    JTextField txtUsername;
    
    JButton btnOK;
    JButton btnCancel;
    
    public AddContactUI()
    {
        setTitle("Add New Contact");
        setLayout(null);
        
        lblUsername = new JLabel("Please enter your contact username.");
        txtUsername = new JTextField();
        btnOK = new JButton("OK");
        btnCancel = new JButton("Cancel");
        
        lblUsername.setBounds(10, 10, 275, 25);
        txtUsername.setBounds(10, 45, 275, 25);
        btnOK.setBounds(110, 100, 80, 25);
        btnCancel.setBounds(200, 100, 80, 25);
        
        add(lblUsername);
        add(txtUsername);
        add(btnOK);
        add(btnCancel);
        
        btnOK.addActionListener(this);
        btnCancel.addActionListener(this);
        
        setSize(300, 170);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) 
    {
        if (e.getSource().equals(btnOK))
        {
            String username = txtUsername.getText().trim();
            
            if (!username.equals(""))
            {
                Main.m_session.writeByte(CMSG_ADD_CONTACT);
                Main.m_session.writeObject(username);
                Main.m_session.flush();
                
                dispose();
            }
            else
                JOptionPane.showMessageDialog(this, "Please enter a valid username.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if (e.getSource().equals(btnCancel))
            dispose();
    }
}
