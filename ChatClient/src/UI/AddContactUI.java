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

package UI;

import Core.NetworkManager;
import Core.Opcode;
import Core.Packet;
import Core.UICore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class AddContactUI extends JFrame implements Opcode
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
        
        btnOK.addActionListener(actListener);
        btnCancel.addActionListener(actListener);
        
        txtUsername.addKeyListener(keyListener);
        
        setSize(300, 170);
        setResizable(false);
        setLocationRelativeTo(UICore.getMasterUI());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    
    public void submitData()
    {
        String username = txtUsername.getText().trim();
        
        if (!username.equals(""))
        {
            Packet p = new Packet(CMSG_ADD_CONTACT);
            p.put(username);
            
            NetworkManager.SendPacket(p);
            
            dispose();
        }
        else
            JOptionPane.showMessageDialog(null, "Please enter a valid username.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    ActionListener actListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e) 
        {
            if (e.getSource().equals(btnOK))
                submitData();
        
            if (e.getSource().equals(btnCancel))
                dispose();
        }
    };
    
    KeyListener keyListener = new KeyAdapter()
    {
        public void keyReleased(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
                submitData();
        }   
    };
}
