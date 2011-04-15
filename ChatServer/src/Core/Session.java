package Core;

import java.io.ObjectOutputStream;
import java.sql.ResultSet;

public class Session implements Runnable, Opcode
{
    Client c;
    ObjectOutputStream out;
    
    public Session(Client c)
    {
        this.c = c;
        out = c.getOutputStream();
    }
    
    public void run()
    {
        try
        {
            while(true)
            {
                byte b = c.getInputStream().readByte();
                
                switch(b)
                {
                    case CMSG_GET_FRIEND_LIST:
                        System.out.printf("Opcode: CMSG_GET_FRIEND_LIST\n");
                        
                        ResultSet rs = Main.db.query("SELECT a.guid, a.username, a.title, a.psm FROM friend AS c LEFT JOIN account AS a ON c.f_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
                        
                        while(rs.next())
                        {
                            out.writeByte(SMSG_FRIEND_DETAIL);
                            out.writeInt(rs.getInt(1));        //guid
                            out.writeObject(rs.getString(2));  //username
                            out.writeObject(rs.getString(3));  //title
                            out.writeObject(rs.getString(4));  //psm
                            out.flush();
                            Thread.sleep(10);
                            System.out.printf("Send Contact: %s\n", rs.getString(2));
                        }
                        
                        out.writeByte(SMSG_FRIEND_LIST_ENDED);
                        out.flush();
                        
                        System.out.printf("Send Friend: Finish\n");
                        
                        break;
                    case CMSG_LOGOUT:
                        break;
                    case CMSG_ADD_FRIEND:
                        break;
                    case CMSG_REMOVE_FRIEND:
                        break;
                    default:
                        System.out.printf("Unknown Opcode Receive");
                        break;
                }
            }
        }
        catch(Exception e){}
    }
}
