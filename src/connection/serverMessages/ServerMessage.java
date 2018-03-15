package connection.serverMessages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public abstract class ServerMessage {
    public enum Status {
        UNINITILIALIZED,
        NOTIFICATIONERROR,
        NOTIFICATIONCHANGEPASSWORD,
        NOTIFICATIONLOGGEDIN,
        NOTIFICATIONUSERONLINESTATUS,
        NOTIFICATIONLOGGEDOUT,
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

        ACTIONREGISTER,
        ACTIONREQUESTCHANGEPASSWORD,
        ACTIONLOGIN,
        ACTIONLOGOUT,
        ACTIONADDCONTACT,
        ACTIONREMOVECONTACT,
        ACTIONUPDATEPROFILE,
        ACTIONSENDMESSAGE,
        ACTIONUPDATEMESSAGE,
        ACTIONREACTTOMESSAGE,
        ACTIONADDUSERTOCONVERSATION,
        ACTIONREMOVEDUSERFROMCONVERSATION,
        ACTIONREADMESSAGE,
        ACTIONSETTYPING;
    }
    int status;

    public Status getStatus() {
        return Status.values()[status];
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
