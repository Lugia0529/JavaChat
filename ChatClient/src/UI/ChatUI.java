package UI;

import javax.swing.*;
import java.awt.event.*;

import Core.*;

public class ChatUI extends JFrame implements Opcode
{
    Contact c;
    
    String accountTitle;
    
    JScrollPane paneOutput;
    JScrollPane paneInput;
    
    JTextArea txtOutput;
    JTextArea txtInput;
    
    public ChatUI(Contact c, String accountTitle)
    {
        this.c = c;
        this.accountTitle = accountTitle;
        
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
        txtInput.addKeyListener(keyListener);
    }
    
    KeyListener keyListener = new KeyAdapter()
    {
        public void keyReleased(KeyEvent e)
        {
            if (e.getKeyCode() == e.VK_ENTER)
            {
                if (e.isShiftDown())
                {
                    txtInput.append("\n");
                    return;
                }
                
                if (txtInput.getText().trim().equals(""))
                {
                    txtInput.setText("");
                    return;
                }
                
                Main.m_session.writeByte(CMSG_SEND_CHAT_MESSAGE);
                Main.m_session.writeInt(c.getGuid());
                Main.m_session.writeObject(txtInput.getText().trim());
                Main.m_session.flush();
                
                String message = txtInput.getText().trim();
                message = message.replaceAll("\n", "\n     ");
                
                txtOutput.append(String.format("%s says:\n", accountTitle));
                txtOutput.append(String.format("     %s\n", message));
                
                txtInput.setText("");
            }
        }   
    };
    
    WindowListener winListener = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e) 
        {
            ContactListUI.chatWindow.removeElement((ChatUI)e.getSource());
        }
    };
    
    public Contact getContact()
    {
        return this.c;
    }
}
