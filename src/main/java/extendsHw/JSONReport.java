package extendsHw;

public class JSONReport extends TextReport{

    @Override
    public String generate(String name, String body) {
       String l = System.lineSeparator();
        return "{"
                + l
                + "\t\"name\" : \"" + name + "\","  // имя
                + l
                + "\t\"body\" : \"" + body + "\""    // тело
                + l
                + "}";
    }
}
