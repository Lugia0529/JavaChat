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
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RoomFormUI extends JFrame implements Opcode
{
    private JLabel lblRoomName;
    private JLabel lblRoomPassword;
    
    private JTextField txtRoomName;
    private JPasswordField txtRoomPassword;
    
    private JButton btnOK;
    private JButton btnCancel;
    
    private int formType;
    
    public static final int CREATE_ROOM = 0;
    public static final int JOIN_ROOM = 1;
    
    public RoomFormUI(int formType)
    {
        this(formType, "");
    }
    
    public RoomFormUI(int formType, String initRoomName)
    {
        switch (formType)
        {
            case CREATE_ROOM:
                setTitle("Create Room");
                break;
            case JOIN_ROOM:
                setTitle("Join An Existing Room");
                break;
            default:
                return;
        }
        
        setLayout(null);
        
        this.formType = formType;
        
        lblRoomName = new JLabel("Room Name");
        lblRoomPassword = new JLabel("Room Password (Leave blank if no password)");
        
        txtRoomName = new JTextField(initRoomName);
        txtRoomPassword = new JPasswordField();
        
        btnOK = new JButton("OK");
        btnCancel = new JButton("Cancel");
        
        lblRoomName.setBounds(12, 10, 275, 25);
        txtRoomName.setBounds(10, 30, 275, 25);
        lblRoomPassword.setBounds(12, 60, 275, 25);
        txtRoomPassword.setBounds(10, 80, 275, 25);
        btnOK.setBounds(110, 120, 80, 25);
        btnCancel.setBounds(200, 120, 80, 25);
        
        add(lblRoomName);
        add(txtRoomName);
        add(lblRoomPassword);
        add(txtRoomPassword);
        add(btnOK);
        add(btnCancel);
        
        btnOK.addActionListener(actListener);
        btnCancel.addActionListener(actListener);
        
        txtRoomName.addKeyListener(keyListener);
        txtRoomPassword.addKeyListener(keyListener);
        
        setSize(300, 190);
        setResizable(false);
        setLocationRelativeTo(UICore.getMasterUI());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    
    public void submitData()
    {
        String roomName = txtRoomName.getText().trim();
        String roomPassword = new String(txtRoomPassword.getPassword()).trim();
        
        if (!roomName.equals(""))
        {
            Packet p;
            
            switch (formType)
            {
                case CREATE_ROOM:
                    p = new Packet(CMSG_CREATE_ROOM);
                    break;
                case JOIN_ROOM:
                    p = new Packet(CMSG_JOIN_ROOM);
                    break;
                default:
                    return;
            }
            
            p.put(roomName);
            p.put(roomPassword);
            
            NetworkManager.SendPacket(p);
            
            dispose();
        }
        else
            JOptionPane.showMessageDialog(null, "Please enter a valid room name.", "Error", JOptionPane.ERROR_MESSAGE);
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
