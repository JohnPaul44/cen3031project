package ServerMessages;

import model.Profile;
import model.Reactions;

import java.util.HashMap;
import java.util.Map;

class DummyData {
    private String username = "thead9";
    private String password = "bogus";
    private String firstName = "Thomas";
    private String lastName = "Headley";
    private String email = "thead9@ufl.edu";
    private String phone = "4074086638";
    private Profile.Gender gender = Profile.Gender.MALE;
    private String birthday = "06/14/1995";
    private String securityQuestion = "Who are you?";
    private String securityAnswer = "Me";

    private Profile profile;

    private String conversationKey1 = "conversationKey1";
    private String messageText1 = "Message Text";
    private String messageKey1 = "messageKey1";

    private Map<String, Reactions> reactions1 = new HashMap<>();
    private Reactions specificReactions1 = new Reactions(new int[]{1, 2}, username);

    DummyData() {
        profile = new Profile(firstName, lastName, email, phone, securityQuestion, securityAnswer, gender, birthday);
        setUpReactions();
    }

    private void setUpReactions() {
        reactions1.put(username, specificReactions1);
    }
}
