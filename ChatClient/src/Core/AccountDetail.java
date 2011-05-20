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

public class AccountDetail
{
    private int guid;
    private String username;
    private String title;
    private String psm;
    private int status;
    
    public AccountDetail(int guid, String username, String title, String psm, int status)
    {
        this.guid = guid;
        this.username = username;
        this.title = title;
        this.psm = psm;
        this.status = status;
    }
    
    public int getGuid()
    {
        return guid;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getTitle()
    {
        return title.equals("") ? username : title;
    }
    
    public String getOriginalTitle()
    {
        return title;
    }
    
    public String getPSM()
    {
        return psm;
    }
    
    public int getStatus()
    {
        return status;
    }
    
    public void setGuid(int guid)
    {
        this.guid = guid;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
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
    
    public String getUITitle()
    {
        return String.format("JavaChat <%s>", getUsername());
    }
}
