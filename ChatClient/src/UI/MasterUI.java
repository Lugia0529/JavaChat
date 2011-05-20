package UI;

import Core.AccountDetail;
import Core.Contact;
import Core.NetworkManager;
import Core.Opcode;
import Core.Packet;
import Core.UIManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class MasterUI extends JFrame implements Opcode
{
    private static JPanel loginPanel;
    private static JPanel contactPanel;
    
    private static boolean isLoginUI;
    
    /* Login Interface */
    private JButton btnLogin;
    private JButton btnExit;
    private JButton btnReg;
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblUsername;
    private JLabel lblPassword;
    
    /* Contact List Interface */
    private JComboBox cStatus;
    private JList contactList;
    private JScrollPane contactListPane;
    
    private JLabel lblTitle;
    private JLabel lblPSM;
    
    private JTextField txtTitle;
    private JTextField txtPSM;
    
    private JButton btnAddContact;
    private JButton btnRemoveContact;
    
    private DefaultListModel model;
    
    private String[] status = {"Online", "Away", "Busy", "Appear Offline", "Logout"};
    
    private AccountDetail accountContact;
    
    public MasterUI()
    {
        setTitle("Login");
        setLayout(null);
        
        isLoginUI = true;
        
        loginPanel = new JPanel(null);
        contactPanel = new JPanel(null);
        
        /* Login UI */
        lblUsername = new JLabel("Username");
        lblPassword = new JLabel("Password");
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        btnLogin = new JButton("Login");
        btnReg = new JButton("Register");
        btnExit = new JButton("Exit");
        
        loginPanel.add(lblPassword);
        loginPanel.add(lblUsername);
        loginPanel.add(txtPassword);
        loginPanel.add(txtUsername);
        loginPanel.add(btnLogin);
        loginPanel.add(btnReg);
        loginPanel.add(btnExit);
        
        lblPassword.setBounds(25, 250, 100, 25);
        lblUsername.setBounds(25, 180, 100, 25);
        txtPassword.setBounds(25, 270, 220, 25);
        txtUsername.setBounds(25, 200, 220, 25);
        btnLogin.setBounds(85,310, 100, 25);
        btnReg.setBounds(30, 420, 100, 25);
        btnExit.setBounds(140, 420, 100, 25);
        
        /* Contact List Interface */
        lblTitle = new JLabel();
        lblPSM = new JLabel();
        
        txtTitle = new JTextField();
        txtPSM = new JTextField();
        
        cStatus = new JComboBox(status);
        
        btnAddContact = new JButton("Add Contact");
        btnRemoveContact = new JButton("Remove Contact");
        
        model = new DefaultListModel();
        contactList = new JList(model);
        contactListPane = new JScrollPane(contactList);
        
        contactPanel.add(lblTitle);
        contactPanel.add(txtTitle);
        contactPanel.add(lblPSM);
        contactPanel.add(txtPSM);
        contactPanel.add(cStatus);
        contactPanel.add(btnAddContact);
        contactPanel.add(btnRemoveContact);
        contactPanel.add(contactListPane);
        
        lblTitle.setBounds(15, 10, 240, 25);
        txtTitle.setBounds(15, 10, 240, 25);
        lblPSM.setBounds(15, 35, 240, 25);
        txtPSM.setBounds(15, 35, 240, 25);
        cStatus.setBounds(10, 65, 245, 25);
        btnAddContact.setBounds(10, 100, 120, 25);
        btnRemoveContact.setBounds(135, 100, 120, 25);
        contactListPane.setBounds(10, 130, 245, 330);
        
        loginPanel.setBounds(0, 0, 270, 500);
        contactPanel.setBounds(270 , 0, 270, 500);
        
        lblTitle.setFont(new Font("sansserif", Font.BOLD, 16));
        lblPSM.setFont(new Font("sansserif", Font.PLAIN, 12));
        
        txtTitle.setVisible(false);
        txtPSM.setVisible(false);
        
        add(loginPanel);
        add(contactPanel);
        
        setSize(270, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
        btnLogin.addActionListener(actListener);
        cStatus.addActionListener(actListener);
        btnExit.addActionListener(actListener);
        btnAddContact.addActionListener(actListener);
        btnRemoveContact.addActionListener(actListener);
        
        txtTitle.addFocusListener(focusListener);
        txtPSM.addFocusListener(focusListener);
        
        txtUsername.addKeyListener(loginKeyListener);
        txtPassword.addKeyListener(loginKeyListener);
        
        txtTitle.addKeyListener(contactKeyListener);
        txtPSM.addKeyListener(contactKeyListener);
        
        lblTitle.addMouseListener(mouseListener);
        lblPSM.addMouseListener(mouseListener);
        contactList.addMouseListener(mouseListener);
        
        addWindowListener(winListener);
    }
    
    public void setAccountDetail(int guid, String username, String title, String psm, int status)
    {
        this.accountContact = new AccountDetail(guid, username, title, psm, status);
        
        lblTitle.setText(accountContact.getTitle());
        
        lblPSM.setText(psm.equals("") ? "<Click to type a personal message>" : psm);
        lblPSM.setFont(new Font("sansserif", psm.equals("") ? Font.ITALIC : Font.PLAIN, 12));
        lblPSM.setForeground(psm.equals("") ? Color.GRAY : Color.BLACK);
        
        updateUITitle();
    }
    
    public AccountDetail getAccountDetail()
    {
        return this.accountContact;
    }
    
    public void clearAccountDetail()
    {
        this.accountContact = null;
    }
    
    public void updateUITitle()
    {
        setTitle(accountContact.getUITitle());
    }
    
    public void enableLoginInput(boolean enable)
    {
        txtUsername.setEnabled(enable);
        txtPassword.setEnabled(enable);
    }
    
    public void addContact(Contact c)
    {
        model.addElement(c);
    }
    
    public Contact searchContact(int guid)
    {
        for (int i = 0; i < model.getSize(); i++)
            if (((Contact)model.elementAt(i)).getGuid() == guid)
                return (Contact)model.elementAt(i);
        
        return null;
    }
    
    public void UpdateContactStatus(int guid, int status)
    {
        Contact c = searchContact(guid);
        
        if (c != null)
        {
            c.setStatus(status);
            contactList.repaint();
        }
    }
    
    public void UpdateContactDetail(int guid, String title, String psm)
    {
        Contact c = searchContact(guid);
        ChatUI ui = UIManager.getChatUIList().findUI(c);
        
        if (c != null)
        {
            if (title != null)
                c.setTitle(title);
            
            if (psm != null)
                c.setPSM(psm);
            
            contactList.repaint();
        }
        
        if (ui != null)
            ui.UpdateTitle();
    }
    
    public void login()
    {
        enableLoginInput(false);
        
        if (txtUsername.getText().equals(""))
        {
            UIManager.showMessageDialog("Please enter your username.", "Error", JOptionPane.ERROR_MESSAGE);
            enableLoginInput(true);
            return;
        }
        
        if (txtPassword.getPassword().length == 0)
        {
            UIManager.showMessageDialog("Please enter your password.", "Error", JOptionPane.ERROR_MESSAGE);
            enableLoginInput(true);
            return;
        }
        
        NetworkManager.login(txtUsername.getText(), new String(txtPassword.getPassword()));
    }
    
    public void resetUI()
    {
        if (isLoginUI)
        {
            txtPassword.setText("");
        }
        else
        {
            model.clear();
            lblTitle.setText("");
            lblPSM.setText("");
            cStatus.setSelectedIndex(0);
        }
    }
    
    ActionListener actListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(btnLogin))
                login();
            
            if (e.getSource().equals(btnAddContact))
                new AddContactUI();
            
            if (e.getSource().equals(btnExit))
                System.exit(0);
            
            if (e.getSource().equals(btnRemoveContact))
            {
                if (contactList.getSelectedIndex() > -1)
                {
                    if (JOptionPane.showConfirmDialog(null, "Do you want to remove this contact?", "Remove Contact", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                        return;
                    
                    Contact c = (Contact)model.getElementAt(contactList.getSelectedIndex());
                    ChatUI chatUI = UIManager.getChatUIList().findUI(c);
                    
                    int guid = c.getGuid();
                    
                    if (chatUI != null)
                        UIManager.getChatUIList().remove(chatUI);
                    
                    
                    Packet p = new Packet(CMSG_REMOVE_CONTACT);
                    p.put(guid);
                    
                    NetworkManager.SendPacket(p);
                    
                    model.removeElementAt(contactList.getSelectedIndex());
                    
                    contactList.repaint();
                }
                else
                    JOptionPane.showMessageDialog(null, "No contact is selected!", "Remove Contact", JOptionPane.ERROR_MESSAGE);
            }
            
            if (e.getSource().equals(cStatus))
            {
                //Only logout have special handle, status change only inform server
                if (cStatus.getSelectedIndex() != 4)
                {
                    Packet p = new Packet(CMSG_STATUS_CHANGED);
                    p.put(cStatus.getSelectedIndex());
                    
                    NetworkManager.SendPacket(p);
                }
                else
                    NetworkManager.SendPacket(new Packet(CMSG_LOGOUT));
            }
        }
    };
    
    FocusListener focusListener = new FocusAdapter()
    {
        public void focusLost(FocusEvent e)
        {
            if (e.getSource().equals(txtTitle))
            {
                txtTitle.setVisible(false);
                lblTitle.setVisible(true);
                
                return;
            }
            
            if (e.getSource().equals(txtPSM))
            {
                txtPSM.setVisible(false);
                lblPSM.setVisible(true);
                
                return;
            }
        }
    };
    
    KeyListener loginKeyListener = new KeyAdapter()
    {
        public void keyReleased(KeyEvent e)
        {
            // Only handle enter key in Chat Interface.
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
                login();
        }
    };
    
    KeyListener contactKeyListener = new KeyAdapter()
    {
        public void keyPressed(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            {
                if (e.getSource().equals(txtTitle))
                {
                    txtTitle.setVisible(false);
                    lblTitle.setVisible(true);
                    
                    return;
                }
                
                if (e.getSource().equals(txtPSM))
                {
                    txtPSM.setVisible(false);
                    lblPSM.setVisible(true);
                    
                    return;
                }
            }
            
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
            {
                if (e.getSource().equals(txtTitle))
                {
                    String newTitle = txtTitle.getText().trim();
                    
                    if (!newTitle.equals(accountContact.getOriginalTitle()))
                    {
                        accountContact.setTitle(newTitle);
                        
                        lblTitle.setText(accountContact.getTitle());
                        
                        Packet p = new Packet(CMSG_TITLE_CHANGED);
                        p.put(newTitle);
                        
                        NetworkManager.SendPacket(p);
                    }
                    
                    txtTitle.setVisible(false);
                    lblTitle.setVisible(true);
                }
                
                if (e.getSource().equals(txtPSM))
                {
                    String newPSM = txtPSM.getText().trim();
                    
                    if (!newPSM.equals(accountContact.getPSM()))
                    {
                        accountContact.setPSM(newPSM);
                        
                        lblPSM.setText(newPSM.equals("") ? "<Click to type a personal message>" : newPSM);
                        lblPSM.setFont(new Font("sansserif", newPSM.equals("") ? Font.ITALIC : Font.PLAIN, 12));
                        lblPSM.setForeground(newPSM.equals("") ? Color.GRAY : Color.BLACK);
                        
                        Packet p = new Packet(CMSG_PSM_CHANGED);
                        p.put(newPSM);
                        
                        NetworkManager.SendPacket(p);
                    }
                    
                    txtPSM.setVisible(false);
                    lblPSM.setVisible(true);
                }
                
                updateUITitle();
            }
        }
    };
    
    MouseListener mouseListener = new MouseAdapter()
    {
        public void mouseClicked(MouseEvent e)
        {
            if (e.getSource().equals(lblTitle))
            {
                lblTitle.setVisible(false);
                txtTitle.setVisible(true);
                
                txtTitle.setText(accountContact.getOriginalTitle());
                txtTitle.requestFocusInWindow();
            }
            
            if (e.getSource().equals(lblPSM))
            {
                lblPSM.setVisible(false);
                txtPSM.setVisible(true);
                
                txtPSM.setText(accountContact.getPSM());
                txtPSM.requestFocusInWindow();
            }
            
            // Handle double click event of contact list.
            // Open contact ChatUI when client is double click on contact detail.
            if (e.getClickCount() == 2)
            {
                // Get the contact detail first.
                int index = contactList.locationToIndex(e.getPoint());
                Contact c = (Contact)model.getElementAt(index);
                
                ChatUI ui = UIManager.getChatUIList().findUI(c);
                
                if (ui != null)
                {
                    if (ui.getState() == JFrame.ICONIFIED)
                        ui.setState(JFrame.NORMAL);
                    
                    ui.toFront();
                }
                else
                    UIManager.getChatUIList().add(new ChatUI(c, accountContact.getTitle()));
            }
        }
    };
    
    WindowListener winListener = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e) 
        {
            // Inform the server that client is ready to logout when client is close that Contact List.
            // Only logout when client is logged in
            if (!isLoginUI)
                NetworkManager.SendPacket(new Packet(CMSG_LOGOUT));
            
            System.exit(0);
        }
    };
    
    public static final class UISwitcher implements Runnable
    {
        public void run()
        {
            int movement = isLoginUI ? -3 : 3;
            
            // 270 / 3 = 90
            for(int i = 0; i < 90; i++)
            {
                loginPanel.setLocation(loginPanel.getX() + movement, 0);
                contactPanel.setLocation(contactPanel.getX() + movement, 0);
                
                try { Thread.sleep(2); }
                catch(Exception e) {}
            }
            
            UIManager.getMasterUI().resetUI();
            
            isLoginUI = !isLoginUI;
        }
    }
}
