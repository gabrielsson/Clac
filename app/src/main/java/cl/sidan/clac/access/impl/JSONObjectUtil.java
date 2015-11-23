package cl.sidan.clac.access.impl;

import java.math.BigDecimal;

/**
 * Created by max.gabrielsson on 2014-10-14.
 */
public class JSONObjectUtil {
    public static BigDecimal getBigDecimal(String value) {
       return new BigDecimal(value);
    }
}
