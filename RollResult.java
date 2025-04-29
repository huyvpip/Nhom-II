package rollresult;
import java.util.*;
import java.util.logging.*;
public class RollResult {

    private static final Logger logger = Logger.getLogger(RollResult.class.getName());

    private int total;
    private int modifier;
    private Vector<Integer> rolls;

    // Constructor private cho nội bộ class sử dụng
    private RollResult(int total, int modifier, Vector<Integer> rolls) {
        this.total = total;
        this.modifier = modifier;
        this.rolls = rolls;
    }

    // Constructor khởi tạo RollResult với bonus
    public RollResult(int bonus) {
        this.total = bonus;
        this.modifier = bonus;
        this.rolls = new Vector<>();
    }

    /**
     * Thêm kết quả xúc xắc mới vào danh sách và cập nhật tổng điểm
     * Kiểm tra input phải >= 1 (giá trị xúc xắc hợp lệ)
     */
    public void addResult(int res) {
        if (res < 1) {
            throw new IllegalArgumentException("Ket qua xuc xac phai>= 1"); // Validate đầu vào
        }

        total += res;
        rolls.add(res);
        logger.info("Da them ket qua xuc xac: " + res + " -> Tong hien tai: " + total); // Logging
    }

    /**
     * Gộp 2 kết quả RollResult thành một
     */
    public RollResult andThen(RollResult r2) {
        int newTotal = this.total + r2.total;
        Vector<Integer> combinedRolls = new Vector<>(this.rolls);
        combinedRolls.addAll(r2.rolls);

        return new RollResult(newTotal, this.modifier + r2.modifier, combinedRolls);
    }

    @Override
    public String toString() {
        return total + " <= " + rolls +
                (modifier > 0 ? ("+" + modifier) :
                        modifier < 0 ? ("" + modifier) : "");
    }
  // ===========================
    // Hàm main để chạy thử lớp này
    // ===========================
    public static void main(String[] args) {
        // Tạo RollResult đầu tiên với bonus là 2
        RollResult result1 = new RollResult(2);
        result1.addResult(3);
        result1.addResult(4);

        // Tạo RollResult thứ hai với bonus là 1
        RollResult result2 = new RollResult(1);
        result2.addResult(5);

        // Kết hợp 2 kết quả
        RollResult combined = result1.andThen(result2);

        // In kết quả
        System.out.println("Result 1: " + result1);
        System.out.println("Result 2: " + result2);
        System.out.println("Combined : " + combined);
    }
}



