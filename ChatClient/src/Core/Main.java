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
        frame.setSize(270, 500);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}