import tas.Tas;

import java.io.IOException;

public class TasTest {
    public static void main(String[] args) throws IOException {
        Tas.main(new String[]{"test_program.pas", "test_program_error.pas"});
    }
}
