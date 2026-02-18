import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileReader {

    public static void main(String[] args) throws IOException {


/*        byte[] file  = Files.readAllBytes(Path.of("pom.xml"));
        String cont = new String(file, StandardCharsets.UTF_8);



        System.out.println(cont);
 */


        final List<String> results = new ArrayList<>();
        Optional<String> optionalValue = evaluate(50);
        final Optional<Boolean> added1 = optionalValue.map(results::add);
        optionalValue.ifPresent(results::add);
        optionalValue.map(results::add);

        optionalValue = evaluate(101);


        optionalValue.ifPresent(results::add);
        optionalValue.map(results::add);

        final Optional<Boolean> added = optionalValue.map(results::add);
        System.out.println(added);
        System.out.println(added1);
        System.out.println(results);

    }
    public static Optional<String> evaluate(int i) {

        if (i > 100 ){
            return Optional.of("OK");
        }else{
            return Optional.ofNullable(null);
        }
    }
}
