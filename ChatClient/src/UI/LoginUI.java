package UI;

import javax.swing.*;
import java.awt.event.*;
import Core.*;

public class LoginUI extends JFrame implements ActionListener, Opcode
{
    JButton btnLogin, btnExit, btnReg;
    JTextField txtUsername;
    JPasswordField txtPassword;
    JLabel lblUsername,lblPassword;
    
    public LoginUI()
    {
        setTitle("Login Form");
        setLayout(null);
        
        lblUsername = new JLabel("Username");
        lblPassword = new JLabel("Password");
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        btnLogin = new JButton("Login");
        btnReg = new JButton("Register");
        btnExit = new JButton("Exit");
        
        add(lblPassword);
        add(lblUsername);
        add(txtPassword);
        add(txtUsername);
        add(btnLogin);
        add(btnReg);
        add(btnExit);
        
        lblPassword.setBounds(30,250,100,25);
        lblUsername.setBounds(30,180,100,25);
        txtPassword.setBounds(30,270,220,25);
        txtUsername.setBounds(30,200,220,25);
        btnLogin.setBounds(90,300,100,25);
        btnReg.setBounds(30,420,100,25);
        btnExit.setBounds(150, 420, 100, 25);
        
        btnLogin.addActionListener(this);
        btnReg.addActionListener(this);
        btnExit.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(btnLogin))
        {
            if (txtUsername.getText().equals(""))
            {
                JOptionPane.showMessageDialog(this, "Please enter your username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtPassword.getPassword().length == 0)
            {
                JOptionPane.showMessageDialog(this, "Please enter your password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try
            {
                switch(Main.m_session.login(txtUsername.getText(), new String(txtPassword.getPassword())))
                {
                    case SMSG_LOGIN_SUCCESS:
                        String name = String.format("%s", Main.m_session.readObject());
                        String psm = String.format("%s", Main.m_session.readObject());
                        new FriendListUI(name, psm, this);
                        break;
                    case SMSG_LOGIN_FAILED:
                        Main.m_session.destroy();
                        JOptionPane.showMessageDialog(this, "Invalid Username and Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        break;
                    case SMSG_MULTI_LOGIN:
                        Main.m_session.destroy();
                        JOptionPane.showMessageDialog(this, "This account is logged in on other computer.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        break;
                    default:
                        System.out.println("Unknown Opcode Receive.");
                        break;
                }
            }
            catch(Exception ex){}
        }
        
        if (e.getSource().equals(btnExit))
        {
            System.exit(0);
        }
    }
}
