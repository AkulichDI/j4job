package converter;

import java.util.Objects;

public class ValidatorLines {

    public static void validateLine ( String line ){

        Objects.requireNonNull(line, " Строка не может быть пустой в списке ответов бота" );
        if ( line.trim().isEmpty()){
            throw  new IllegalArgumentException("Строка не может быть пустой в списке ответов бота");
        }

    }






}
