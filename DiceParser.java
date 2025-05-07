package javaapplication1;

import java.util.*;

public class DiceParser {
    // -------------------- StringStream Helper --------------------
    private static class StringStream {
        private StringBuffer buff;

        public StringStream(String s) {
            buff = new StringBuffer(s);
        }

        private void munchWhiteSpace() {
            int index = 0;
            char curr;
            while (index < buff.length()) {
                curr = buff.charAt(index);
                if (!Character.isWhitespace(curr))
                    break;
                index++;
            }
            buff = buff.delete(0, index);
        }

        public boolean isEmpty() {
            munchWhiteSpace();
            return buff.toString().equals("");
        }

        public Integer getInt() {
            return readInt();
        }

        public Integer readInt() {
            int index = 0;
            munchWhiteSpace();
            char curr;
            while (index < buff.length()) {
                curr = buff.charAt(index);
                if (!Character.isDigit(curr))
                    break;
                index++;
            }
            try {
                Integer ans = Integer.parseInt(buff.substring(0, index));
                buff = buff.delete(0, index);
                return ans;
            } catch (Exception e) {
                return null;
            }
        }

        public Integer readSgnInt() {
            munchWhiteSpace();
            StringStream state = save();
            if (checkAndEat("+")) {
                Integer ans = readInt();
                if (ans != null)
                    return ans;
                restore(state);
                return null;
            }
            if (checkAndEat("-")) {
                Integer ans = readInt();
                if (ans != null)
                    return -ans;
                restore(state);
                return null;
            }
            return readInt();
        }

        public boolean checkAndEat(String s) {
            munchWhiteSpace();
            if (buff.indexOf(s) == 0) {
                buff = buff.delete(0, s.length());
                return true;
            }
            return false;
        }

        public StringStream save() {
            return new StringStream(buff.toString());
        }

        public void restore(StringStream ss) {
            this.buff = new StringBuffer(ss.buff);
        }

        public String toString() {
            return buff.toString();
        }
    }

    // -------------------- DieRoll Classes --------------------

    public static class DieRoll {
        protected int ndice;
        protected int dsides;
        protected int bonus;
        protected static Random rand = new Random();

        public DieRoll(int ndice, int dsides, int bonus) {
            this.ndice = ndice;
            this.dsides = dsides;
            this.bonus = bonus;
        }

        public int makeRoll() {
            int total = bonus;
            for (int i = 0; i < ndice; i++) {
                total += rand.nextInt(dsides) + 1;
            }
            return total;
        }

        public String toString() {
            String result = ndice + "d" + dsides;
            if (bonus > 0) {
                result += "+" + bonus;
            } else if (bonus < 0) {
                result += bonus;
            }
            return result;
        }
    }

    public static class SelectiveDieRoll extends DieRoll {
        private boolean keepHighest;
        private int keepCount;

        public SelectiveDieRoll(int ndice, int dsides, int bonus, boolean keepHighest, int keepCount) {
            super(ndice, dsides, bonus);
            this.keepHighest = keepHighest;
            this.keepCount = keepCount;
        }

        @Override
        public int makeRoll() {
            List<Integer> rolls = new ArrayList<>();
            for (int i = 0; i < ndice; i++) {
                rolls.add(rand.nextInt(dsides) + 1);
            }
            rolls.sort((a, b) -> keepHighest ? b - a : a - b);
            List<Integer> selected = rolls.subList(0, Math.min(keepCount, rolls.size()));
            int total = bonus;
            for (int val : selected) {
                total += val;
            }
            return total;
        }

        @Override
        public String toString() {
            return super.toString() + (keepHighest ? "kh" : "kl") + keepCount;
        }
    }

    public static class DiceSum extends DieRoll {
        private DieRoll left;
        private DieRoll right;

        public DiceSum(DieRoll left, DieRoll right) {
            super(0, 0, 0);
            this.left = left;
            this.right = right;
        }

        @Override
        public int makeRoll() {
            return left.makeRoll() + right.makeRoll();
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " & " + right.toString() + ")";
        }
    }

    // -------------------- DiceParser Logic --------------------

    public static Vector<DieRoll> parseRoll(String s) {
        StringStream ss = new StringStream(s.toLowerCase());
        Vector<DieRoll> v = parseRollInner(ss, new Vector<DieRoll>());
        if (ss.isEmpty())
            return v;
        return null;
    }

    private static Vector<DieRoll> parseRollInner(StringStream ss, Vector<DieRoll> v) {
        Vector<DieRoll> r = parseXDice(ss);
        if (r == null) {
            return null;
        }
        v.addAll(r);
        if (ss.checkAndEat(";")) {
            return parseRollInner(ss, v);
        }
        return v;
    }

    private static Vector<DieRoll> parseXDice(StringStream ss) {
        StringStream saved = ss.save();
        Integer x = ss.getInt();
        int num;
        if (x == null) {
            num = 1;
        } else {
            if (ss.checkAndEat("x")) {
                num = x;
            } else {
                num = 1;
                ss.restore(saved);
            }
        }
        DieRoll dr = parseDice(ss);
        if (dr == null) {
            return null;
        }
        Vector<DieRoll> ans = new Vector<DieRoll>();
        for (int i = 0; i < num; i++) {
            ans.add(dr);
        }
        return ans;
    }

    private static DieRoll parseDice(StringStream ss) {
        return parseDTail(parseDiceInner(ss), ss);
    }

    private static DieRoll parseDiceInner(StringStream ss) {
        Integer num = ss.getInt();
        int ndice = (num == null) ? 1 : num;

        if (!ss.checkAndEat("d")) return null;

        Integer dsides = ss.getInt();
        if (dsides == null) return null;

        int bonus = 0;
        Integer tempBonus = ss.readSgnInt();
        if (tempBonus != null) bonus = tempBonus;

        // Support for kh/kl
        boolean keepHighest = false;
        int keepCount = -1;

        if (ss.checkAndEat("kh")) {
            Integer count = ss.getInt();
            if (count != null) {
                keepHighest = true;
                keepCount = count;
            }
        } else if (ss.checkAndEat("kl")) {
            Integer count = ss.getInt();
            if (count != null) {
                keepHighest = false;
                keepCount = count;
            }
        }

        if (keepCount != -1) {
            return new SelectiveDieRoll(ndice, dsides, bonus, keepHighest, keepCount);
        } else {
            return new DieRoll(ndice, dsides, bonus);
        }
    }

    private static DieRoll parseDTail(DieRoll r1, StringStream ss) {
        if (r1 == null)
            return null;
        if (ss.checkAndEat("&")) {
            DieRoll d2 = parseDice(ss);
            return parseDTail(new DiceSum(r1, d2), ss);
        } else {
            return r1;
        }
    }

    private static void test(String s) {
        Vector<DieRoll> v = parseRoll(s);
        int i;
        if (v == null)
            System.out.println("Failure: " + s);
        else {
            System.out.println("Results for " + s + ":");
            for (i = 0; i < v.size(); i++) {
                DieRoll dr = v.get(i);
                System.out.print(v.get(i));
                System.out.print(": ");
                System.out.println(dr.makeRoll());
            }
        }
    }

    // -------------------- Main Method --------------------

    public static void main(String[] args) {
        test("d6");
        test("2d6");
        test("d6+5");
        test("4X3d8-5");
        test("12d10+5 & 4d6+2");
        test("d6 ; 2d4+3");
        test("4d6+3 ; 8d12 -15 ; 9d10 & 3d6 & 4d12 +17");
        test("4d6 + xyzzy");
        test("hi");
        test("4d4d4");

        // New test cases for kh/kl
        test("4d6kh3");
        test("5d10kl2");
        test("3d8+2kh2");
    }
}
