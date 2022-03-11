import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) {
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher("1$T0");
        System.out.println(matcherTemp.matches());
    }
}
