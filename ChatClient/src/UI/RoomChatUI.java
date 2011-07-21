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
import Core.Room;
import Core.UICore;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class RoomChatUI extends JFrame implements Opcode
{
    private Room r;
    
    private JScrollPane paneMemberList;
    private JScrollPane paneOutput;
    private JScrollPane paneInput;
    
    private JList memberList;
    
    private JTextArea txtOutput;
    private JTextArea txtInput;
    
    private DefaultListModel model;
    
    public RoomChatUI(Room r, String accountTitle)
    {
        this.r = r;
        
        setTitle(r.getRoomName());
        setLayout(null);
        
        model = new DefaultListModel();
        memberList = new JList(model);
        
        txtOutput = new JTextArea();
        txtInput = new JTextArea();
        
        paneMemberList = new JScrollPane(memberList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paneOutput = new JScrollPane(txtOutput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paneInput = new JScrollPane(txtInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(paneMemberList);
        add(paneOutput);
        add(paneInput);
        
        paneMemberList.setBounds(10, 10, 150, 405);
        paneOutput.setBounds(165, 10, 350, 300);
        paneInput.setBounds(165, 315, 350, 100);
        
        txtOutput.setEditable(false);
        
        setSize(535, 450);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
        addWindowListener(winListener);
        txtInput.addKeyListener(keyListener);
    }
    
    public void addMember(String memberName, boolean showMessage)
    {
        model.addElement(memberName);
        
        if (showMessage)
            txtOutput.append(String.format("%s has joined the room.\n", memberName));
    }
    
    public void removeMember(String memberName, boolean showMessage)
    {
        model.removeElement(memberName);
        
        if (showMessage)
            txtOutput.append(String.format("%s has leaved the room.\n", memberName));
    }
    
    public void append(String title, String message)
    {
        message = message.replaceAll("\n", "\n     ");
        txtOutput.append(String.format("%s says:\n", title));
        txtOutput.append(String.format("     %s\n", message));
    }
    
    public Room getRoom()
    {
        return this.r;
    }
    
    public void close()
    {
        Packet p = new Packet(CMSG_LEAVE_ROOM);
        p.put(r.getRoomID());
        
        NetworkManager.SendPacket(p);
        
        UICore.getRoomChatUIList().remove(this);
    }
    
    KeyListener keyListener = new KeyAdapter()
    {
        public void keyReleased(KeyEvent e)
        {
            // Only handle enter key in Chat Interface.
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
            {
                // Shift + Enter = next line.
                if (e.isShiftDown())
                {
                    txtInput.append("\n");
                    return;
                }
                
                // Trim the message, cancel the message sending if message is empty.
                if (txtInput.getText().trim().equals(""))
                {
                    txtInput.setText("");
                    return;
                }
                
                // Send the message to server.
                Packet p = new Packet(CMSG_ROOM_CHAT);
                p.put(r.getRoomID());
                p.put(txtInput.getText().trim());
                
                NetworkManager.SendPacket(p);
                
                // Reset the input text area.
                txtInput.setText("");
            }
        }   
    };
    
    WindowListener winListener = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e) 
        {
            close();
        }
    };
}
