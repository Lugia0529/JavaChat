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

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CliHandler implements Runnable
{
    private BufferedReader in;
    
    public CliHandler() throws Exception
    {
        System.out.printf("\nInitializing Cli Command Handler.\n");
        in = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public void run()
    {
        System.out.printf("Cli Command Handler started.\n");
        
        String command;
        
        try
        {
            while(true)
            {
                command = in.readLine().trim();
                
                if (command.isEmpty())
                    continue;
                
                System.out.printf("Command Receive: %s\n", command);
                
                if (command.equals("uptime"))
                    System.out.printf("Server Uptime: %dsec.\n", (System.currentTimeMillis() - Main.startupTime) / 1000);
                else if (command.equals("clist"))
                    System.out.printf("Current Client List Size: %d\n", Main.clientList.size());
                else
                    System.out.printf("Unknown Command: %s\n", command);
            }
        }
        catch(Exception e){}
    }
}
