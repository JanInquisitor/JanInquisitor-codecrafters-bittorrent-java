import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import com.dampcake.bencode.Bencode; - available if you need it!

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
//        System.out.println("Logs from your program will appear here!");
        String command = args[0];
        if ("decode".equals(command)) {
            //  Uncomment this block to pass the first stage
            String bencodedValue = args[1];
            Object decoded;
            try {

                decoded = decodeBencode(bencodedValue);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return;
            }


            if (decoded instanceof Integer) {
                System.out.println(decoded);
            } else if (decoded instanceof String) {
                System.out.println(gson.toJson(decoded));
            }

        } else {
            System.out.println("Unknown command: " + command);
        }

    }

    // I could use a stack or dequeue...
    static Object decodeBencode(String bencodedString) {
        if (Character.isDigit(bencodedString.charAt(0))) {
            return decodeString(bencodedString);
        } else if (bencodedString.startsWith("i")) {
            return decodeInteger(bencodedString);
        } else if (bencodedString.startsWith("l")) {
            return decodeList(bencodedString);
        } else {
            throw new RuntimeException("Only strings are supported at the moment");
        }
    }

    private static Object[] decodeList(String bencodedString) {
        List<String> list = new ArrayList<>();

        // This takes out the 'e' char that finish the list, for now at least.
        String wholelist = bencodedString.substring(1, bencodedString.length() - 1);

        String[] arr = wholelist.split("i");

        for (int i = 0; i < arr.length; i++) {
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
