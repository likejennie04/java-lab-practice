import java.util.Scanner;
class OutOfRangeException extends Exception {
    public OutOfRangeException() {
        super("OutOfRangeException");
    }
}
class AddZeroException extends Exception {
    public AddZeroException() {
        super("AddZeroException");
    }
}
class SubtractZeroException extends Exception {
    public SubtractZeroException() {
        super("SubtractZeroException");
    }
}
public class SimpleCalculator {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        try {
            int opIdx = -1;
            char op = ' ';
            if (input.indexOf('+') != -1) {
                opIdx = input.indexOf('+');
                op = '+';
            } else if (input.indexOf('-') != -1) {
                opIdx = input.indexOf('-');
                op = '-';
            }
            int a = Integer.parseInt(input.substring(0, opIdx));
            int b = Integer.parseInt(input.substring(opIdx + 1)); 
            if (op == '+') {
                if (a == 0 || b == 0) throw new AddZeroException(); // [cite: 285]
            } else {
                if (a == 0 || b == 0) throw new SubtractZeroException(); // [cite: 287]
            }
            
           
            if (a < 0 || a > 1000 || b < 0 || b > 1000) {
                throw new OutOfRangeException(); // [cite: 284]
            }
            
            
            int result = 0;
            if (op == '+') {
                result = a + b;
            } else {
                result = a - b;
            }
            if (result < 0 || result > 1000) {
                throw new OutOfRangeException();
            }
            System.out.println(result);
            
        } catch (AddZeroException e) {
            System.out.println("AddZeroException");
        } catch (SubtractZeroException e) {
            System.out.println("SubtractZeroException");
        } catch (OutOfRangeException e) {
            System.out.println("OutOfRangeException");
        } catch (Exception e) {
        } finally {
            sc.close();
        }
    }
}