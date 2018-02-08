import org.junit.jupiter.api.Test;

import sample.*;

import com.google.gson.Gson;

public class Tests {

    @Test
    public void toJsonStringTest() {
        LoginServerMessage m = new LoginServerMessage("thead9", "bogus");

        Gson gson = new Gson();
        String jsonString = gson.toJson(m);

        System.out.println(jsonString);
    }
}
