package UI;

import javax.swing.*;
import java.awt.event.*;

import Core.*;

public class ChatUI extends JFrame
{
    Contact c;
    
    JScrollPane paneOutput;
    JScrollPane paneInput;
    
    JTextArea txtOutput;
    JTextArea txtInput;
    
    public ChatUI(Contact c)
    {
        this.c = c;
        
        setTitle(c.toString());
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
    }
    
    WindowListener winListener = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e) 
        {
            FriendListUI.chatWindow.removeElement((ChatUI)e.getSource());
        }
    };
    
    public Contact getContact()
    {
        return this.c;
    }
}
