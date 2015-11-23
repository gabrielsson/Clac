package cl.sidan.clac.access.interfaces;

public interface Poll {
    String getQuestion();
    String getYae();
    String getNay();
    String getDatum();
    Integer getId(); // Identifier
    Integer getNrYay();
    Integer getNrNay();
}
