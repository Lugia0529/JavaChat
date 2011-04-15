package Core;

public class Session implements Runnable, Opcode
{
    Client c;
    
    public Session(Client c)
    {
        this.c = c;
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
