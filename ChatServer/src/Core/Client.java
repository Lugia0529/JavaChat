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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client
{
    private int guid;
    private String username;
    private String title;
    private String psm;
    private int status;
    private Socket socket;
    private Session session;
    
    private int latency;
    private int counter;
    private long ticks;
    
    public Client(int guid, String username)
    {
        this.guid = guid;
        this.username = username;
        
        this.latency = 0;
        this.counter = 0;
        this.ticks = 0;
    }
    
    public void createSession(Socket socket, ObjectInputStream in, ObjectOutputStream out) throws IOException
    {
        this.session = new Session(this, in, out);
        this.socket = socket;
        
        // 60 sec timeout
        this.socket.setSoTimeout(60 * 1000);
        
        new Thread(session).start();
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public void setPSM(String psm)
    {
        this.psm = psm;
    }
    
    public void setStatus(int status)
    {
        this.status = status;
    }
    
    public void setLatency(int latency)
    {
        this.latency = latency;
    }
    
    public void setCounter(int counter)
    {
        this.counter = counter;
    }
    
    public void setTicks(Long ticks)
    {
        this.ticks = ticks;
    }
    
    public int getGuid()
    {
        return this.guid;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    
    public String getTitle()
    {
        return this.title;
    }
    
    public String getPSM()
    {
        return this.psm;
    }
    
    public int getStatus()
    {
        return this.status;
    }
    
    public int getLatency()
    {
        return this.latency;
    }
    
    public int getCounter()
    {
        return this.counter;
    }
    
    public long getTicks()
    {
        return this.ticks;
    }
    
    public Socket getSocket()
    {
        return this.socket;
    }
    
    public Session getSession()
    {
        return this.session;
    }
}