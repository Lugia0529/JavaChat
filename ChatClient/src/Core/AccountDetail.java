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

public final class AccountDetail
{
    private static int guid;
    private static String username;
    private static String title;
    private static String psm;
    private static int status;
    
    public static void init(int cGuid, String cUsername, String cTitle, String cPSM, int cStatus)
    {
        guid = cGuid;
        username = cUsername;
        title = cTitle;
        psm = cPSM;
        status = cStatus;
    }
    
    public static void clear()
    {
        guid = 0;
        username = null;
        title = null;
        psm = null;
        status = 0;
    }
    
    public static int getGuid()
    {
        return guid;
    }
    
    public static String getUsername()
    {
        return username;
    }
    
    public static String getTitle()
    {
        return title;
    }
    
    public static String getPSM()
    {
        return psm;
    }
    
    public static int getStatus()
    {
        return status;
    }
    
    public static void setTitle(String newTitle)
    {
        title = newTitle;
    }
    
    public static void setPSM(String newPSM)
    {
        psm = newPSM;
    }
    
    public static void setStatus(int newStatus)
    {
        status = newStatus;
    }
    
    public static String getDisplayTitle()
    {
        return title.equals("") ? username : title;
    }
    
    public static String getUITitle()
    {
        return String.format("JavaChat <%s>", getUsername());
    }
}
