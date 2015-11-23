package cl.sidan.clac.access.interfaces;

import java.util.Date;

/**
 * Created by max.gabrielsson on 2014-10-14.
 */
public interface Article {

    String getId();
    String getHeader();
    String getBody();
    Date getDate();

}
