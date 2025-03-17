import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class practice1 {

    public static String longestCommonStr (String str1, String str2) {
        if(null == str1 || null == str2) {
            return null;
        }
        if("".equals(str1) || "".equals(str2)) {
            return "";
        }
        int firstLen = str1.length();
        int secondLen = str2.length();

        String result = "";

        String[] arr1 = str1.split("");
        String[] arr2 = str2.split("");
        for (int i = 0; i < arr1.length; i++) {
            String cur = arr1[i];
            if(str2.contains(cur)) {
                int j = 0;
                while(true) {
                    if(firstLen <= i + j) {
                        break;
                    }
                    if(j >= secondLen) {
                        break;
                    }
                    if(arr2[j].equals(cur)) {
                        int tempIdx = 1;
                        String tempStr = cur;
                        while(true) {
                            if(firstLen <= i + tempIdx || secondLen <= j+tempIdx) {
                                break;
                            }
                            if(!arr2[j+tempIdx].equals(arr1[i+tempIdx])){
                                break;
                            }
                            tempStr += arr1[i+tempIdx];
                            tempIdx++;
                        }
                        result = tempStr.length() > result.length() ? tempStr : result;
                    }
                    j++;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String s = stringCut("中a国人民站起来了");
        System.out.println(s);
    }
    

    public static String stringCut (String val) {
        // write code here
        if(null == val) {
            return null;
        }
        if(val.isEmpty()) {
            return val;
        }
        byte[] valArr = val.getBytes();
        int length = valArr.length;
        if(length <= 10) {
            return val;
        }
        char[] charArray = val.toCharArray();
        String result = "";
        int tempIdx = 0;
        while(true) {
            char c = charArray[tempIdx++];
            String tmpStr = Character.toString(c);
            byte[] cur = result.getBytes();
            if(cur.length == 10) {
                break;
            }
            byte[] tmp = tmpStr.getBytes();
            if(cur.length + tmp.length <= 10) {
                result += tmpStr;
            } else {
                break;
            }
        }
        return result;
    }
}
