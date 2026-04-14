class Student {
    String name;
    int score;

    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
}

public class JavaPracticeAll {

    public static void main(String[] args) {
        
        // --- 1. 計算並列印平均分數 ---
        int[] scores = {70, 80, 90};
        int sum1 = 0;
        for (int s : scores) {
            sum1 += s;
        }
        double average = (double) sum1 / scores.length;
        System.out.println("1. 平均分數: " + average);

        // --- 2. 呼叫 findMax 並列印最大值 ---
        int maxVal = findMax(scores);
        System.out.println("2. 最大值: " + maxVal);

        // --- 3. 呼叫 addBonus (每個分數 +5) ---
        int[] bonusScores = {60, 70};
        addBonus(bonusScores);
        System.out.print("3. 加分後的結果: {");
        for (int i = 0; i < bonusScores.length; i++) {
            System.out.print(bonusScores[i] + (i == bonusScores.length - 1 ? "" : ", "));
        }
        System.out.println("}");

        // --- 4. 建立 Tom 物件並列印 ---
        Student tom = new Student("Tom", 85);
        System.out.println("4. " + tom.name + ": " + tom.score);

        // --- 5. 測試 curve 方法 (低於 60 加 10 分) ---
        Student lowScoreStudent = new Student("Alice", 55);
        curve(lowScoreStudent);
        System.out.println("5. Alice 調分後 (55+10): " + lowScoreStudent.score);

        // --- 6. 計算陣列中及格人數 (>= 60) ---
        int[] classScores = {55, 75, 40, 90, 60};
        int passCount = 0;
        for (int s : classScores) {
            if (s >= 60) passCount++;
        }
        System.out.println("6. 及格人數: " + passCount);

        // --- 7. 呼叫 sum 方法回傳總和 ---
        int totalSum = sum(classScores);
        System.out.println("7. 陣列總和: " + totalSum);

        // --- 8. 建立 3 個學生的陣列並印出姓名與分數 ---
        Student[] studentArray = {
            new Student("StudentA", 65),
            new Student("StudentB", 88),
            new Student("StudentC", 52)
        };
        System.out.println("8. 學生清單:");
        for (Student s : studentArray) {
            System.out.println("   - " + s.name + ": " + s.score);
        }

        // --- 9. 測試 updateScore 方法 ---
        updateScore(tom, 95);
        System.out.println("9. Tom 更新後的分數: " + tom.score);

        // --- 10. 找出整數陣列中的最小值並印出 ---
        int[] nums = {45, 12, 88, 5, 67};
        int min = nums[0];
        for (int n : nums) {
            if (n < min) min = n;
        }
        System.out.println("10. 陣列最小值: " + min);
    }

    // --- 以下為各題目要求的方法實作 ---

    // 題目 2: 找最大值
    public static int findMax(int[] arr) {
        int max = arr[0];
        for (int n : arr) {
            if (n > max) max = n;
        }
        return max;
    }

    // 題目 3: 每個元素加 5 分
    public static void addBonus(int[] scores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += 5;
        }
    }

    // 題目 5: 低於 60 加 10 分
    public static void curve(Student s) {
        if (s.score < 60) {
            s.score += 10;
        }
    }

    // 題目 7: 計算總和
    public static int sum(int[] arr) {
        int total = 0;
        for (int n : arr) total += n;
        return total;
    }

    // 題目 9: 更新學生成績
    public static void updateScore(Student s, int newScore) {
        s.score = newScore;
    }
}