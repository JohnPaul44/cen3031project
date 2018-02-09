package connection.serverMessaging;

import com.google.gson.Gson;

abstract class ServerMessage {
    enum Status {
        UNINITILIALIZED,
        NOTIFICATIONERROR,
        NOTIFICATIONLOGGEDIN,
        NOTIFICATIONUSERONLINESTATUS,
        NOTIFICATIONLOGGEDOUT,
        NOTIFICATIONCONTACTADDED,
        NOTIFICATIONCONTACTREMOVED,
        NOTIFICATIONPROFILEUPDATED,
        NOTIFICATIONMESSAGERECEIVED,
        NOTIFICIATIONMESSAGEUPDATED,
        NOTIFICATIONUSERADDEDTOCONVERSATION,
        NOTIFICATIONUSERREMOVEDFROMCONVERSATION,
        NOTIFICATIONMESSAGEREAD,
        NOTIFICATIONTYPING,

        ACTIONREGISTER,
        ACTIONLOGIN,
        ACTIONLOGOUT,
        ACTIONADDCONTACT,
        ACTIONREMOVECONTACT,
        ACTIONUPDATEPROFILE,
        ACTIONSENDMESSAGE,
        ACTIONUPDATEMESSAGE,
        ACTIONADDUSERTOCONVERSATION,
        ACTIONREMOVEDUSERFROMCONVERSATION,
        ACTIONREADMESSAGE,
        ACTIONSETTYPING
    }
    int status;

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
