// 主程式：執行並顯示所有投影片內容
public class Assignment1 {
    public static void main(String[] args) {
        System.out.println("--- 檔案名稱: Lecture_6_probability.pdf 作業結果 ---\n");
        
        // 實例化 12 個類別並呼叫顯示方法
        new ProbabilityDefinition().display();
        new SampleSpace().display();
        new Event().display();
        new BasicFormula().display();
        new ComplementaryEvent().display();
        new UnionEvent().display();
        new IntersectionEvent().display();
        new ConditionalProbability().display();
        new IndependentEvents().display();
        new BayesTheorem().display();
        new LawOfTotalProbability().display();
        new SchoolExample().display();
    }
}

// 1. 機率的定義 [cite: 21]
class ProbabilityDefinition {
    void display() {
        System.out.println("1. 機率的定義：表示某件事情發生的可能性 [cite: 22]");
        System.out.println("   公式：P(A) = 事件A發生的個數 / 全部可能情況 [cite: 23]\n");
    }
}

// 2. 樣本空間 [cite: 25]
class SampleSpace {
    void display() {
        System.out.println("2. 樣本空間 (Sample Space)：S 是所有可能結果的集合 [cite: 26]");
        System.out.println("   例如：抽一位學生，所有學生就是樣本空間 [cite: 27]\n");
    }
}

// 3. 事件 [cite: 29]
class Event {
    void display() {
        System.out.println("3. 事件 (Event)：A 是我們關心的事件 [cite: 30]");
        System.out.println("   例如：抽到建中學生 [cite: 30]\n");
    }
}

// 4. 基本公式 [cite: 32]
class BasicFormula {
    void display() {
        System.out.println("4. 基本公式：P(A) = n(A) / n(S) [cite: 33]");
        System.out.println("   口訣：我要的 / 全部的 [cite: 34]\n");
    }
}

// 5. 補事件 [cite: 38]
class ComplementaryEvent {
    void display() {
        System.out.println("5. 補事件：P(A^C) = 1 - P(A) [cite: 36]");
        System.out.println("   例如：不是建中的機率 [cite: 37]\n");
    }
}

// 6. 聯集 [cite: 40]
class UnionEvent {
    void display() {
        System.out.println("6. 聯集 (OR)：P(A ∪ B) = P(A) + P(B) - P(A ∩ B) [cite: 41]");
        System.out.println("   若互斥：P(A ∪ B) = P(A) + P(B) [cite: 41]\n");
    }
}

// 7. 交集 [cite: 51]
class IntersectionEvent {
    void display() {
        System.out.println("7. 交集 (AND)：P(A ∩ B) = P(A)P(B|A) [cite: 52]");
        System.out.println("   也可寫成 P(B)P(A|B) [cite: 52]\n");
    }
}

// 8. 條件機率 [cite: 67]
class ConditionalProbability {
    void display() {
        System.out.println("8. 條件機率：已知 B 發生，A 的機率 [cite: 68]");
        System.out.println("   公式：P(A|B) = P(A ∩ B) / P(B) [cite: 66, 71]\n");
    }
}

// 9. 獨立事件 [cite: 79]
class IndependentEvents {
    void display() {
        System.out.println("9. 獨立事件：若 A、B 獨立 [cite: 77]");
        System.out.println("   則 P(A ∩ B) = P(A)P(B) 且 P(A|B) = P(A) [cite: 78]\n");
    }
}

// 10. 貝氏定理 [cite: 87]
class BayesTheorem {
    void display() {
        System.out.println("10. 貝氏定理：P(A|B) = P(B|A)P(A) / P(B) [cite: 87]");
        System.out.println("    用途：已知結果，反推原因 [cite: 87]\n");
    }
}

// 11. 全機率公式 [cite: 89]
class LawOfTotalProbability {
    void display() {
        System.out.println("11. 全機率公式：P(A) = Σ P(A|Bi)P(Bi) [cite: 89]\n");
    }
}

// 12. 學校例子 [cite: 90]
class SchoolExample {
    void display() {
        System.out.println("12. 學校例子：總人數 N，建中 J 人，北一女 B 人 [cite: 90]");
        System.out.println("    P(建中) = J/N, P(北一女) = B/N [cite: 90]");
        System.out.println("    P(建中 ∪ 北一女) = (J+B)/N (因為兩者互斥) [cite: 91]\n");
    }
}