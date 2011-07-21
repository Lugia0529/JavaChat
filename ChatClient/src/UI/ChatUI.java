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

import Core.AccountDetail;
import Core.Contact;
import Core.NetworkManager;
import Core.Opcode;
import Core.Packet;
import Core.UICore;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class ChatUI extends JFrame implements Opcode
{
    Contact c;
    
    JScrollPane paneOutput;
    JScrollPane paneInput;
    
    JTextArea txtOutput;
    JTextArea txtInput;
    
    public ChatUI(Contact c)
    {
        this.c = c;
        
        UpdateTitle();
        
        setLayout(null);
        
        txtOutput = new JTextArea();
        txtInput = new JTextArea();
        
        paneOutput = new JScrollPane(txtOutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        paneInput = new JScrollPane(txtInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(paneOutput);
        add(paneInput);
        
        paneOutput.setBounds(10, 10, 350, 300);
        paneInput.setBounds(10, 315, 350, 100);
        
        txtOutput.setEditable(false);
        
        setSize(375, 450);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
        addWindowListener(winListener);
        txtInput.addKeyListener(keyListener);
    }
    
    public void append(String title, String message)
    {
        message = message.replaceAll("\n", "\n     ");
        txtOutput.append(String.format("%s says:\n", title));
        txtOutput.append(String.format("     %s\n", message));
    }
    
    public Contact getContact()
    {
        return this.c;
    }
    
    public void UpdateTitle()
    {
        setTitle(c.toString());
    }
    
    public void close()
    {
        UICore.getChatUIList().remove(this);
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
                Packet p = new Packet(CMSG_SEND_CHAT_MESSAGE);
                p.put(c.getGuid());
                p.put(txtInput.getText().trim());
                
                NetworkManager.SendPacket(p);
                
                String message = txtInput.getText().trim();
                message = message.replaceAll("\n", "\n     ");
                
                // Output the message to Chat Interface too.
                append(AccountDetail.getDisplayTitle(), message);
                
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
