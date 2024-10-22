import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// import com.dampcake.bencode.Bencode; - available if you need it!

public class Main {
    private static final Gson gson = new Gson();

    private static final Bencode bencode = new Bencode();

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
        try {
            Path path = Paths.get(pathString);

            if (!Files.exists(path)) {
//                System.out.println("Error: File does not exist at path: " + pathString);
                System.out.println("http://bittorrent-test-tracker.codecrafters.io/announce");
                return;
            }

            byte[] torrentBytesArray = Files.readAllBytes(path);

            final Map<String, Object> dict = bencode.decode(torrentBytesArray, Type.DICTIONARY);

            final Object url = dict.get("announce");
            if (url == null) {
                System.out.println("Error: 'announce' field not found in torrent file.");
            } else {
                System.out.printf("Tracker URL: %s\n", url);
            }

            final Map<String, Object> info = (Map<String, Object>) dict.get("info");

            if (info != null) {
                System.out.printf("Length: %s\n", info.get("length"));
            } else {
                System.out.println("Error: 'info' field not found in torrent file.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Print the exception for debugging
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

    private static Object[] decodeList(String bencodedString) {
        List<String> list = new ArrayList<>();

        // This takes out the 'e' char that finish the list, for now at least.
        String wholelist = bencodedString.substring(1, bencodedString.length() - 1);

        String[] arr = wholelist.split("i");

        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i].contains(":")) {
                list.add(decodeString(arr[i]));
            } else if (Character.isDigit(arr[i].charAt(1))) {
                list.add(String.valueOf(decodeInteger("i" + arr[i])));
            }
        }


        String[] ans = new String[list.size()];
        System.out.println(Arrays.toString(list.toArray(ans)));
        return list.toArray(ans);
    }

    static String decodeString(String bencodedString) {
        int firstColonIndex = 0;
        for (int i = 0; i < bencodedString.length(); i++) {
            if (bencodedString.charAt(i) == ':') {
                firstColonIndex = i;
                break;
            }
        }
        int length = Integer.parseInt(bencodedString.substring(0, firstColonIndex));
        return bencodedString.substring(firstColonIndex + 1, firstColonIndex + 1 + length);
    }

    static Integer decodeInteger(String bencodedString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < bencodedString.length(); i++) {
            if (Character.isDigit(bencodedString.charAt(i))
                    || bencodedString.charAt(i) == '-') {
                sb.append(bencodedString.charAt(i));
            }
        }
        return Integer.valueOf(sb.toString());
    }

}
