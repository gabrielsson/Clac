package cl.sidan.clac.other;

import java.util.Arrays;
import java.util.List;

import cl.sidan.clac.R;

public class ThemePicker {
    //Set the NoActionBar since we are not using the actionbar, otherwise we will get crashes (npe)
    static Integer[] themes = {R.style.DefaultTheme_NoActionBar, R.style.NightTheme_NoActionBar, R.style.ClassicTheme_NoActionBar};
    static String[] names = {"Default", "Night", "Classic"};

    static final List<Integer> themeStyles = Arrays.asList(themes);
    static final List<String> themeNames = Arrays.asList(names);

    public static Integer getTheme(int position) {
        if (0 > position || position > themeStyles.size()) {
            position = 0;
        }
        return themeStyles.get(position);
    }

    public static String getThemeName(int position) {
        if (0 > position || position > themeNames.size()) {
            position = 0;
        }
        return themeNames.get(position);
    }

    public static List<String> getThemeNames() {
        return themeNames;
    }
}
