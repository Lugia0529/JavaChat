/*
 * Copyright (C) 2011 Lugia Programming Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
