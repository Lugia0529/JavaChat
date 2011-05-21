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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

public class ContactRequestUI extends JFrame implements Opcode
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
        
        setTitle("Contact Request");
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
        
        btnOk.addActionListener(actListener);
        btnLater.addActionListener(actListener);
        
        setSize(500, 200);
        setResizable(false);
        setLocationRelativeTo(UICore.getMasterUI());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    
    ActionListener actListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(btnOk))
            {
                Packet p = new Packet(rbAllow.isSelected() ?  CMSG_CONTACT_ACCEPT : CMSG_CONTACT_DECLINE);
                p.put(guid);
                
                NetworkManager.SendPacket(p); 
            }

            dispose();
        }
    };
}
