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
}
