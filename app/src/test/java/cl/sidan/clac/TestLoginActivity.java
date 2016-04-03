package cl.sidan.clac;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by max.gabrielsson on 03/04/16.
 */
public class TestLoginActivity {

    private LoginActivity loginActivity = new LoginActivity();
    @Test
    public void testIsPasswordValid() {
        Assert.assertTrue(loginActivity.isPasswordValid("123"));
        Assert.assertFalse(loginActivity.isPasswordValid("00"));

    }

    @Test
    public void testIsEmailValid() {
        Assert.assertTrue(loginActivity.isEmailValid("max.gabrielsson@gmail.com"));
        Assert.assertFalse(loginActivity.isEmailValid("max.gabrielsson_a_gmail.com"));
        Assert.assertFalse(loginActivity.isEmailValid("@@@@@@"));

    }
}
