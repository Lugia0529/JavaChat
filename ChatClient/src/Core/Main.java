package Core;

import javax.swing.JFrame;
import UI.*;

public class Main
{
    public static Session m_session;
    
    public static void main(String[] args)
    {
        m_session = new Session();
        
        LoginUI frame = new LoginUI();
        frame.setSize(300,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}