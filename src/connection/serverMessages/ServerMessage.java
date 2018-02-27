package connection.serverMessages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class ServerMessage {
    public enum Status {
        UNINITILIALIZED,
        NOTIFICATIONERROR,
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

        @Override
        public String toString() {
            return String.valueOf(super.ordinal());
        }
    }
    int status;

    public int getStatus() {
        return status;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
