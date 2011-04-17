package Core;

import java.io.ObjectOutputStream;
import java.sql.ResultSet;

public class Session implements Runnable, Opcode
{
    private Client c;
    private ObjectOutputStream out;
    
    private volatile Thread session;
    
    public Session(Client c)
    {
        this.c = c;
        this.out = c.getOutputStream();
    }
    
    public void stop()
    {
        session = null;
    }
    
    public void run()
    {
        try
        {
            this.session = Thread.currentThread();
            
            while(session == Thread.currentThread())
            {
                byte b = c.getInputStream().readByte();
                
                switch(b)
                {
                    case CMSG_GET_CONTACT_LIST:
                        System.out.printf("Opcode: CMSG_GET_CONTACT_LIST\n");
                        
                        ResultSet rs = Main.db.query("SELECT a.guid, a.username, a.title, a.psm FROM contact AS c LEFT JOIN account AS a ON c.c_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
                        
                        while(rs.next())
                        {
                            out.writeByte(SMSG_CONTACT_DETAIL);
                            out.writeInt(rs.getInt(1));        //guid
                            out.writeObject(rs.getString(2));  //username
                            out.writeObject(rs.getString(3));  //title
                            out.writeObject(rs.getString(4));  //psm
                            out.flush();
                            Thread.sleep(10);
                            System.out.printf("Send Contact: %s to client %d\n", rs.getString(2), c.getGuid());
                        }
                        
                        System.out.print("Send Opcode: SMSG_CONTACT_LIST_ENDED");
                        out.writeByte(SMSG_CONTACT_LIST_ENDED);
                        out.flush();
                        
                        System.out.printf("Send contact: Finish\n");
                        
                        break;
                    case CMSG_LOGOUT:
                        System.out.printf("Opcode: CMSG_LOGOUT\n");
                        
                        Main.clientList.remove(c);
                        
                        System.out.printf("Closing client socket %d.\n", c.getGuid());
                        c.getSocket().close();
                        
                        Main.db.execute("UPDATE account SET online = 0 WHERE guid = %d", c.getGuid());
                        System.out.printf("Stopping session thread %d.\n", c.getGuid());
                        
                        stop();
                        
                        break;
                    case CMSG_ADD_CONTACT:
                        break;
                    case CMSG_REMOVE_CONTACT:
                        break;
                    default:
                        System.out.printf("Unknown Opcode Receive");
                        break;
                }
            }
            
            System.out.printf("Session thread %d stopped successfully.\n", c.getGuid());
            
            c = null;
            out = null;
        }
        catch(Exception e){}
    }
}
