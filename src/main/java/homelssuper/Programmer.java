package homelssuper;

public class Programmer extends Profession{
    private String programLanguage;

    public Programmer(String programLanguage, int experience, boolean degree) {
        super(degree);
        this.programLanguage = programLanguage;
    }
}
