package cl.sidan.clac.access.impl;

import java.math.BigDecimal;

public class JSONObjectUtil {
    public static BigDecimal getBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
