package connection.serverMessages;

import com.google.gson.Gson;

public abstract class ServerMessage {
    public enum Status {
        UNINITILIALIZED,
        NOTIFICATIONERROR,
        NOTIFICATIONLOGGEDIN,
        NOTIFICATIONUSERONLINESTATUS,
        NOTIFICATIONSECURITYQUESTION,
        NOTIFICATIONPASSWORDCHANGED,
        NOTIFICATIONLOGGEDOUT,
        NOTIFICATIONQUERYRESULTS,
        NOTIFICATIONCONTACTADDED,
        NOTIFICATIONCONTACTREMOVED,
        NOTIFICATIONPROFILEUPDATED,
        NOTIFICATIONMESSAGERECEIVED,
        NOTIFICATIONMESSAGEUPDATED,
        NOTIFICATIONMESSAGEREACTION,
        NOTIFICATIONUSERADDEDTOCONVERSATION,
        NOTIFICATIONUSERREMOVEDFROMCONVERSATION,
        NOTIFICATIONMESSAGEREAD,
        NOTIFICATIONTYPING,
        NOTIFICATIONFRIENDSHIPSTATS,
        NOTIFICATIONACCOUNTDELETED,

        ACTIONREGISTER,
        ACTIONLOGIN,
        ACTIONREQUESTSECURITYQUESTION,
        ACTIONCHANGEPASSWORD,
        ACTIONLOGOUT,
        ACTIONQUERYUSERS,
        ACTIONADDCONTACT,
        ACTIONREMOVECONTACT,
        ACTIONUPDATEPROFILE,
        ACTIONSENDMESSAGE,
        ACTIONUPDATEMESSAGE,
        ACTIONREACTTOMESSAGE,
        ACTIONADDUSERTOCONVERSATION,
        ACTIONREMOVEDUSERFROMCONVERSATION,
        ACTIONREADMESSAGE,
        ACTIONSETTYPING,
        ACTIONGETFRIENDSHIPSTATISTICS,
        ACTIONDELETEACCOUNT,
    }
    public int status;
    public int errorNumber = 0;
    public String errorString;

    public Status getStatus() {
        return Status.values()[status];
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public String getErrorString() {
        return errorString;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean error() {
        return errorNumber != 0;
    }
}
