package Core;

import java.io.Serializable;
import java.util.ArrayList;

public class Packet<PacketData> implements Serializable
{
    private byte opcode;
    private int pointer;
    private ArrayList<PacketData> packet;
    
    public Packet(byte opcode)
    {
        this.opcode = opcode;
        packet = new ArrayList<PacketData>();
        pointer = 0;
    }
    
    public void setOpcode(byte opcode)
    {
        this.opcode = opcode;
    }
    
    public byte getOpcode()
    {
        return this.opcode;
    }
    
    public boolean put(PacketData p)
    {
        return packet.add(p);
    }
    
    public PacketData get()
    {
        return get(pointer++);
    }
    
    public PacketData get(int index)
    {
        if (packet.size() > index)
            return packet.get(index);
        else
            return null;
    }
    
    public int size()
    {
        return packet.size();
    }
    
    public int getCurrentPosition()
    {
        return pointer;
    }
}
