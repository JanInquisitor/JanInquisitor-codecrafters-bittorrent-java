import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

public class Main {
    private static final Gson gson = new Gson();

    private static final Bencode bencode = new Bencode(true);

    public static void main(String[] args) throws Exception {
        String command = args[0];

        if ("decode".equals(command)) {
            String bencodedValue = args[1];
            Object decoded;
            try {
                decoded = decodeBencode(bencodedValue);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return;
            }
            System.out.println(gson.toJson(decoded));
        } else if ("info".equals(args[0])) {
            readInfoFile(args[1]);
        } else {
            System.out.println("Unknown command: " + command);
        }
    }

    private static void readInfoFile(String pathString) {
        if (pathString.endsWith(".")) {
            StringBuilder sb = new StringBuilder(pathString);
            pathString = sb.substring(0, sb.length() - 1);
        }

        Path path = Paths.get(pathString);

        try {
            byte[] torrentBytesArray = Files.readAllBytes(path);

            Map<String, Object> dict = bencode.decode(torrentBytesArray, Type.DICTIONARY);

            Object url = dict.get("announce");
            System.out.printf("Tracker URL: %s\n", url);

            Map<String, Object> info = (Map<String, Object>) dict.get("info");
            System.out.printf("Length: %s\n", info.get("length"));

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] infoHash = digest.digest(bencode.encode((Map<String, Object>) bencode.decode(torrentBytesArray, Type.DICTIONARY).get("info")));
            System.out.println("Info Hash: " + bytesToHex(infoHash));
//            System.out.println(Arrays.toString(infoHash));

        } catch (Exception e) {
            System.out.println(e);
        }
    }


    // I could use a stack or dequeue for when I decide to implement my own bencode solution.
    static Object decodeBencode(String bencodedString) {

        if (Character.isDigit(bencodedString.charAt(0))) {
            return bencode.decode(bencodedString.getBytes(), Type.STRING);
        } else if (bencodedString.startsWith("i")) {
            return bencode.decode(bencodedString.getBytes(), Type.NUMBER);
        } else if (bencodedString.startsWith("l")) {
            return bencode.decode(bencodedString.getBytes(), Type.LIST);
        } else if (bencodedString.startsWith("d")) {
            return bencode.decode(bencodedString.getBytes(), Type.DICTIONARY);
        } else {
            throw new RuntimeException("Only strings are supported at the moment");
        }

    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
