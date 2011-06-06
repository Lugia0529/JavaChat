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

public interface Opcode
{
    final byte CMSG_LOGIN                   = 0x01;
    final byte CMSG_LOGOUT                  = 0x02;
    final byte SMSG_LOGIN_SUCCESS           = 0x03;
    final byte SMSG_LOGIN_FAILED            = 0x04;
    final byte SMSG_MULTI_LOGIN             = 0x05;
    final byte CMSG_GET_CONTACT_LIST        = 0x06;
    final byte CMSG_ADD_CONTACT             = 0x07;
    final byte CMSG_REMOVE_CONTACT          = 0x08;
    final byte SMSG_ADD_CONTACT_SUCCESS     = 0x09;
    final byte CMSG_STATUS_CHANGED          = 0x0A;
    final byte SMSG_CONTACT_DETAIL          = 0x0B;
    final byte SMSG_CONTACT_LIST_ENDED      = 0x0C;
    final byte CMSG_SEND_CHAT_MESSAGE       = 0x0D;
    final byte SMSG_SEND_CHAT_MESSAGE       = 0x0E;
    final byte SMSG_STATUS_CHANGED          = 0x0F;
    final byte SMSG_CONTACT_ALREADY_IN_LIST = 0x10;
    final byte SMSG_CONTACT_NOT_FOUND       = 0x11;
    final byte SMSG_CONTACT_REQUEST         = 0x12;
    final byte CMSG_CONTACT_ACCEPT          = 0x13;
    final byte CMSG_CONTACT_DECLINE         = 0x14;
    final byte CMSG_TIME_SYNC_RESP          = 0x15;
    final byte CMSG_PING                    = 0x16;
    final byte SMSG_PING                    = 0x17;
    final byte SMSG_LOGOUT_COMPLETE         = 0x18;
    final byte CMSG_TITLE_CHANGED           = 0x19;
    final byte SMSG_TITLE_CHANGED           = 0x1A;
    final byte CMSG_PSM_CHANGED             = 0x1B;
    final byte SMSG_PSM_CHANGED             = 0x1C;
    
    enum SessionStatus
    {
        NEVER,         // Opcode is never send by a server, maybe a client opcode, or an invalid opcode.
        NOTLOGGEDIN,   // Can receive when not logged in.
        LOGGEDIN,      // Client is logged in, but contact list havent finish loaded.
        READY,         // Client is logged in, and the contact list is fully loaded.
        INSTANT        // Special case, direct process this packet.
    }
    
    final OpcodeDetail[] opcodeTable = 
    {
        new OpcodeDetail /* 0x00 */ ("UNKNOWN",                      false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x01 */ ("CMSG_LOGIN",                   false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x02 */ ("CMSG_LOGOUT",                  false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x03 */ ("SMSG_LOGIN_SUCCESS",           false, SessionStatus.NOTLOGGEDIN, 0, null                              ),
        new OpcodeDetail /* 0x04 */ ("SMSG_LOGIN_FAILED",            false, SessionStatus.NOTLOGGEDIN, 0, null                              ),
        new OpcodeDetail /* 0x05 */ ("SMSG_MULTI_LOGIN",             false, SessionStatus.NOTLOGGEDIN, 0, null                              ),
        new OpcodeDetail /* 0x06 */ ("CMSG_GET_CONTACT_LIST",        false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x07 */ ("CMSG_ADD_CONTACT",             false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x08 */ ("CMSG_REMOVE_CONTACT",          false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x09 */ ("SMSG_ADD_CONTACT_SUCCESS",     true,  SessionStatus.READY,       5, "HandleAddContactSuccessOpcode"   ),
        new OpcodeDetail /* 0x0A */ ("CMSG_STATUS_CHANGED",          false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x0B */ ("SMSG_CONTACT_DETAIL",          true,  SessionStatus.LOGGEDIN,    5, "HandleContactDetailOpcode"       ),
        new OpcodeDetail /* 0x0C */ ("SMSG_CONTACT_LIST_ENDED",      false, SessionStatus.LOGGEDIN,    0, "HandleContactListEndedOpcode"    ),
        new OpcodeDetail /* 0x0D */ ("CMSG_SEND_CHAT_MESSAGE",       false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x0E */ ("SMSG_SEND_CHAT_MESSAGE",       true,  SessionStatus.READY,       2, "HandleChatMessageOpcode"         ),
        new OpcodeDetail /* 0x0F */ ("SMSG_STATUS_CHANGED",          true,  SessionStatus.READY,       2, "HandleContactStatusChangedOpcode"),
        new OpcodeDetail /* 0x10 */ ("SMSG_CONTACT_ALREADY_IN_LIST", false, SessionStatus.READY,       0, "HandleContactAlreadyInListOpcode"),
        new OpcodeDetail /* 0x11 */ ("SMSG_CONTACT_NOT_FOUND",       false, SessionStatus.READY,       0, "HandleContactNotFoundOpcode"     ),
        new OpcodeDetail /* 0x12 */ ("SMSG_CONTACT_REQUEST",         true,  SessionStatus.READY,       2, "HandleContactRequestOpcode"      ),
        new OpcodeDetail /* 0x13 */ ("CMSG_CONTACT_ACCEPT",          false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x14 */ ("CMSG_CONTACT_DECLINE",         false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x15 */ ("CMSG_TIME_SYNC_RESP",          false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x16 */ ("CMSG_PING",                    false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x17 */ ("SMSG_PING",                    false, SessionStatus.INSTANT,     0, null                              ),
        new OpcodeDetail /* 0x18 */ ("SMSG_LOGOUT_COMPLETE",         false, SessionStatus.LOGGEDIN,    0, "HandleLogoutCompleteOpcode"      ),
        new OpcodeDetail /* 0x19 */ ("CMSG_TITLE_CHANGED",           false, SessionStatus.NEVER,       0, null                              ),
        new OpcodeDetail /* 0x1A */ ("SMSG_TITLE_CHANGED",           true,  SessionStatus.READY,       2, "HandleContactDetailChangedOpcode"),
        new OpcodeDetail /* 0x1B */ ("CMSG_PSM_CHANGED",             false, SessionStatus.READY,       0, null                              ),
        new OpcodeDetail /* 0x1C */ ("SMSG_PSM_CHANGED",             true,  SessionStatus.NEVER,       2, "HandleContactDetailChangedOpcode")
    };
    
    final class OpcodeDetail
    {
        final String name;
        final boolean reqPacketData;
        final SessionStatus sessionStatus;
        final int length;
        final String handler;
        
        public OpcodeDetail(String name, boolean reqPacketData, SessionStatus status, int length, String Handler)
        {
            this.name = name;
            this.reqPacketData = reqPacketData;
            this.sessionStatus = status;
            this.length = length;
            this.handler = Handler;
        }
    }
}